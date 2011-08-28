/*
 * Copyright (c) 2011 David Hoyt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *  
 * The names of any contributors may not be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.github.davidhoyt.maven.plugins.perforce;

import com.github.davidhoyt.maven.plugins.scm.ScmServerFileSetMojo;
import com.github.davidhoyt.maven.plugins.StringUtil;
import com.perforce.maven.scm.provider.p4.command.P4Executor;
import com.perforce.maven.scm.provider.p4.repository.P4ScmProviderRepository;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;
import java.util.List;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;

/**
 * 
 * @author David Hoyt <dhoyt@hoytsoft.org>
 */
public abstract class PerforceScmServerFileSetMojo extends ScmServerFileSetMojo {
	//<editor-fold defaultstate="collapsed" desc="Constants">
	public static final String 
		DEFAULT_REPOSITORY_PATH = "//depot/..."
	;
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Abstract Methods">
	protected abstract boolean executePerforceScmServerFileSet(
		  final IServer server
		, final List<IFileSpec> fileSpecs
		, final ScmProviderRepository repo
		, final String repositoryPath
		, final String username
		, final String password 
		, final ScmLogger logger
	) throws Throwable;
	//</editor-fold>
	
	@Override
	protected final String provideDefaultRepositoryPath(final ScmProviderRepository repo, final String proposedRepositoryPath, final String includes, final String excludes, final String username, final String password, final ScmLogger logger) {
		return DEFAULT_REPOSITORY_PATH;
	}

	@Override
	protected final ScmResult executeScmServerFileSet(final ScmProviderRepository repo, final String repositoryPath, final String includes, final String excludes, final String username, final String password, final ScmLogger logger) throws Throwable {
		P4ScmProviderRepository p4Repo;
		
		if (repo instanceof P4ScmProviderRepository) {
			p4Repo = (P4ScmProviderRepository)repo;
		} else if (repo instanceof PerforceScmProviderRepository) {
			PerforceScmProviderRepository pScm = (PerforceScmProviderRepository)repo;
			p4Repo = new P4ScmProviderRepository(pScm.getHost(), pScm.getPort(), pScm.getPath(), username, password);
		} else {
			throw new ScmException("Unknown perforce provider repository: " + repo.getClass().getName());
		}
		
		try {
			boolean loggedIn = false;
			boolean result = false;
			IServer server = ServerFactory.getServer(ServerFactory.DEFAULT_PROTOCOL_NAME + "://" + p4Repo.getHost() + ":" + p4Repo.getPort(), null);

			try {
				server.connect();
				server.setUserName(p4Repo.getUser());
				server.login(p4Repo.getPassword());

				loggedIn = true;
			} catch(Throwable t) {
				loggedIn = false;

				try { server.logout();     } catch(Throwable t2) { }
				try { server.disconnect(); } catch(Throwable t2) { }

				return new ScmResult("perforce command", t.getMessage(), "unable to run command", false);
			}

			if (loggedIn) {
				try {
					String path = cleanRepositoryPath(repositoryPath);
					path = path.endsWith("/") || path.endsWith("/...") ? P4Executor.getCanonicalRepoPath(path) : path;
					List<IFileSpec> fileSpecs = FileSpecBuilder.makeFileSpecList(path);

					if (fileSpecs != null && !fileSpecs.isEmpty())
						result = executePerforceScmServerFileSet(server, fileSpecs, repo, path, username, password, logger);
					else
						result = true;
				} finally {
					try { server.logout();     } catch(Throwable t) { }
					try { server.disconnect(); } catch(Throwable t) { }
				}
			}

			return new ScmResult("perforce command", result ? "command successful" : "unable to run command", StringUtil.empty, result);
		} catch(Throwable t) {
			if (t instanceof P4JavaException)
				throw new ScmException(t.getLocalizedMessage(), t);
			else
				throw t;
		}
	}
}
