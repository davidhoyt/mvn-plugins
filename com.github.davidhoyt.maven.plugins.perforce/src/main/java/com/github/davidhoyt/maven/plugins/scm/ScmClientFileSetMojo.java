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

package com.github.davidhoyt.maven.plugins.scm;

import java.io.File;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * 
 * @author David Hoyt <dhoyt@hoytsoft.org>
 */
public abstract class ScmClientFileSetMojo extends ScmMojo {
	//<editor-fold defaultstate="collapsed" desc="Abstract Methods">
	protected abstract ScmResult executeScmClientFileSet(
		  final ScmProviderRepository repo
		, final ScmFileSet files
		, final String includes
		, final String excludes
		, final String username 
		, final String password
		, final ScmLogger logger
	) throws Throwable;
	//</editor-fold>

	@Override
	protected final ScmResult executeScm(final String urlScm, final String username, final String password, final String includes, final String excludes, final File scmDirectory, final ScmManager scmManager, final List reactorProjects, final MavenSession session, final MavenProject project, final ScmLogger logger) throws Throwable {
		ScmRepository repository = createScmRepositoryInstance();
		ScmProviderRepository providerRepository = repository.getProviderRepository();
		//ScmProvider scmProvider = scmManager.getProviderByRepository(repository);
		ScmFileSet fileSet = new ScmFileSet(scmDirectory, includes, excludes);
		
		return executeScmClientFileSet(providerRepository, fileSet, includes, excludes, username, password, logger);
	}
}
