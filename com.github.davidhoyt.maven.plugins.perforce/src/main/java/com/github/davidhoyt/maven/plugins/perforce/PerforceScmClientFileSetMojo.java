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

import com.github.davidhoyt.maven.plugins.StringUtil;
import com.github.davidhoyt.maven.plugins.scm.ScmClientFileSetMojo;
import com.perforce.maven.scm.provider.p4.command.P4Executor;
import com.perforce.maven.scm.provider.p4.repository.P4ScmProviderRepository;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;

/**
 * 
 * @author David Hoyt <dhoyt@hoytsoft.org>
 */
public abstract class PerforceScmClientFileSetMojo extends ScmClientFileSetMojo {
	//<editor-fold defaultstate="collapsed" desc="Abstract Methods">
	protected abstract boolean executePerforceScmClientFileSet(
		  final IServer server
		, final List<IFileSpec> fileSpecs
		, final ScmProviderRepository repo
		, final ScmFileSet files
		, final String username
		, final String password 
		, final ScmLogger logger
	) throws Throwable;
	//</editor-fold>

	@Override
	protected final ScmResult executeScmClientFileSet(final ScmProviderRepository repo, final ScmFileSet files, final String includes, final String excludes, final String username, final String password, final ScmLogger logger) throws Throwable {
		P4ScmProviderRepository p4Repo;
		
		if (repo instanceof P4ScmProviderRepository) {
			p4Repo = (P4ScmProviderRepository)repo;
		} else if (repo instanceof PerforceScmProviderRepository) {
			PerforceScmProviderRepository pScm = (PerforceScmProviderRepository)repo;
			p4Repo = new P4ScmProviderRepository(pScm.getHost(), pScm.getPort(), pScm.getPath(), username, password);
		} else {
			throw new ScmException("Unknown perforce provider repository: " + repo.getClass().getName());
		}
		
		boolean shouldRun = true;
		List<IFileSpec> fileSpecs = null;
		
		if (!P4Executor.isEmpty(includes) || !P4Executor.isEmpty(excludes)) {
			List<String> filePaths = new ArrayList<String>(128);
			List<File> fileList = files.getFileList();
			if (fileList != null) {
				for(File f : fileList) 
					if (!".".contentEquals(f.getName()))
						filePaths.add(P4Executor.encodeWildcards(f.getAbsolutePath()));
				if (!filePaths.isEmpty())
					fileSpecs = FileSpecBuilder.makeFileSpecList(filePaths.toArray(new String[filePaths.size()]));
			}
		}
		
		if (fileSpecs == null || fileSpecs.isEmpty())
			if (!P4Executor.isEmpty(includes) || !P4Executor.isEmpty(excludes))
				shouldRun = false;
		
		if (shouldRun) {
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
						result = executePerforceScmClientFileSet(server, fileSpecs, repo, files, username, password, logger);
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
		
		return new ScmResult("perforce command", "unable to run command", StringUtil.empty, false);
	}
}
