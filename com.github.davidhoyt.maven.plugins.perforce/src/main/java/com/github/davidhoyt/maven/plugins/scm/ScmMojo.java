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

import com.github.davidhoyt.maven.plugins.StringUtil;
import com.perforce.p4java.core.IChangelist;
import java.io.File;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Performs operations on a perforce repository.
 * 
 * @requiresProject
 * 
 * @author David Hoyt <dhoyt@hoytsoft.org>
 */
public abstract class ScmMojo extends AbstractMojo {
	//<editor-fold defaultstate="collapsed" desc="Variables">
	/**
	 * @parameter expression="${project.scm.connection}"
	 * @readonly
	 */
	private String urlScm;
	
	/**
	 * The username that is used when connecting to the SCM system.
	 * 
	 * @parameter expression="${username}"
	 */
	private String username;
	
	/**
	 * The password that is used when connecting to the SCM system.
	 * 
	 * @parameter expression="${password}"
	 */
	private String password;
	
	/**
	 * The includes that is used to include files with that pattern.
	 * 
	 * @parameter expression="${includes}"
	 */
	private String includes;
	
	/**
	 * The excludes that is used to include files with that pattern.
	 * 
	 * @parameter expression="${excludes}"
	 */
	private String excludes;
	
	/**
	 * Local directory to be used to issue SCM actions.
	 * 
	 * @parameter expression="${basedir}"
	 */
	private File scmDirectory;
	
	/**
	 * @component
	 */
	private ScmManager scmManager;
	
	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 */
	private MavenProject project;
	
	/**
	 * Contains the full list of projects in the reactor.
	 * 
	 * @parameter expression="${reactorProjects}"
	 * @readonly
	 * @since 1.0-beta-3
	 */
	private List reactorProjects;
	
	/**
	 * @parameter expression="${session}"
	 * @readonly
	 * @required
	 */
	private MavenSession session;
	
	private ScmLogDispatcher logger;
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Getters/Setters">
	protected final MavenProject getProject() {
		return project;
	}
	
	protected final MavenSession getSession() {
		return session;
	}
	
	protected final ScmLogger getLogger() {
		if (logger == null) {
			logger = new ScmLogDispatcher();
			ScmLogger log = new DefaultLog();
			logger.addListener(log);
		}
		return logger;
	}
	
	protected final ScmManager getScmManager() {
		return scmManager;
	}
	
	protected final String getUrlScm() {
		return urlScm;
	}
	
	protected final String getUsername() {
		return username;
	}
	
	protected final String getPassword() {
		return password;
	}
	
	protected final String getIncludes() {
		return includes;
	}
	
	protected final String getExcludes() {
		return excludes;
	}
	
	protected final File getScmDirectory() {
		return scmDirectory;
	}
	
	protected final void setScmManager(ScmManager scmManager) {
		this.scmManager = scmManager;
	}
	
	protected final void setUrlScm(String urlScm) {
		this.urlScm = urlScm;
	}
	
	protected final void setUsername(String username) {
		this.username = username;
	}
	
	protected final void setPassword(String password) {
		this.password = password;
	}
	
	protected final void setIncludes(String includes) {
		this.includes = includes;
	}
	
	protected final void setExcludes(String excludes) {
		this.excludes = excludes;
	}
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Utilities">
	protected final ScmRepository createScmRepositoryInstance() throws ScmException {
		ScmRepository repository = null;
		
		if (StringUtil.isNullOrEmpty(urlScm)) {
			error("Please define a connectionUrl parameter.");
			return null;
		}
		
		try {
			repository = scmManager.makeScmRepository(urlScm);
			ScmProviderRepository scmRepo = repository.getProviderRepository();
			
			if (!StringUtil.isNullOrEmpty(username))
				scmRepo.setUser(username);
			
			if (!StringUtil.isNullOrEmpty(password))
				scmRepo.setPassword(password);
		} catch(Throwable t) {
			error("Problem connecting to the repository with the specified connectionUrl parameter.", t);
		}
		
		return repository;
	}
	
	protected final Xpp3Dom findScmPluginConfiguration() {
		Xpp3Dom configuration = null;
		List<Plugin> plugins = project.getBuildPlugins();
		for(Plugin p : plugins) {
			if ("org.apache.maven.plugins".equals(p.getGroupId())) {
				if ("maven-scm-plugin".equals(p.getArtifactId())) {
					configuration = (Xpp3Dom)p.getConfiguration();
				}
			}
		}
		return configuration;
	}
	
	protected final String cleanRepositoryPath(final String path) {
		if (StringUtil.isNullOrEmpty(path))
			return StringUtil.empty;
		return path.replace("\\", "/");
	}
	
	protected final void checkResult(final ScmResult result) throws ScmException {
		if (!result.isSuccess()) {
			error("Provider message: " + result.getProviderMessage());
			error("Command output: " + result.getCommandOutput());
			throw new ScmException("Unsuccessful result");
		}
	}
	
	protected final void error(final CharSequence msg) {
		if (getLog().isErrorEnabled())
			getLog().error(msg);
	}
	
	protected final void error(final Throwable t) {
		if (getLog().isErrorEnabled())
			getLog().error(t);
	}
	
	protected final void error(final CharSequence msg, final Throwable t) {
		if (getLog().isErrorEnabled())
			getLog().error(msg, t);
	}
	
	protected final void warn(final CharSequence msg) {
		if (getLog().isWarnEnabled())
			getLog().warn(msg);
	}
	
	protected final void warn(final Throwable t) {
		if (getLog().isWarnEnabled())
			getLog().warn(t);
	}
	
	protected final void warn(final CharSequence msg, final Throwable t) {
		if (getLog().isWarnEnabled())
			getLog().warn(msg, t);
	}
	
	protected final void info(final CharSequence msg) {
		if (getLog().isInfoEnabled())
			getLog().info(msg);
	}
	
	protected final void info(final Throwable t) {
		if (getLog().isInfoEnabled())
			getLog().info(t);
	}
	
	protected final void debug(final CharSequence msg) {
		if (getLog().isDebugEnabled())
			getLog().debug(msg);
	}
	
	protected final void debug(final Throwable t) {
		if (getLog().isDebugEnabled())
			getLog().debug(t);
	}
	
	public static int parseChangelist(final String changelist) {
		if (StringUtil.isNullOrEmpty(changelist))
			return IChangelist.UNKNOWN;
		if ("default".equalsIgnoreCase(changelist.trim())) 
			return IChangelist.DEFAULT;
		try {
			int changelistID = Integer.parseInt(changelist);
			if (changelistID < 0)
				return IChangelist.UNKNOWN;
			else
				return changelistID;
		} catch(Throwable t) {
			return IChangelist.UNKNOWN;
		}
	}
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Abstract Methods">
	protected abstract ScmResult executeScm(
		  final String urlScm
		, final String username
		, final String password
		, final String includes
		, final String excludes
		, final File scmDirectory
		, final ScmManager scmManager
		, final List reactorProjects
		, final MavenSession session
		, final MavenProject project
		, final ScmLogger logger
	) throws Throwable;
	//</editor-fold>
	
	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Xpp3Dom configuration = findScmPluginConfiguration();
		
			//Retrieve username/password from the maven-scm-plugin configuration if it's present.
			if (StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
				if (configuration != null) {
					if (StringUtil.isNullOrEmpty(username) && configuration.getChild("username") != null)
						username = configuration.getChild("username").getValue();
					if (StringUtil.isNullOrEmpty(password) && configuration.getChild("password") != null)
						password = configuration.getChild("password").getValue();
				}
			}

			//Get includes/excludes file patterns.
			if (StringUtil.isNullOrEmpty(includes) || StringUtil.isNullOrEmpty(excludes)) {
				if (configuration != null) {
					if (StringUtil.isNullOrEmpty(includes) && configuration.getChild("includes") != null)
						includes = configuration.getChild("includes").getValue();
					if (StringUtil.isNullOrEmpty(excludes) && configuration.getChild("excludes") != null)
						excludes = configuration.getChild("excludes").getValue();
				}
			}

			//Get base directory.
			if (configuration != null) {
				if (configuration.getChild("basedir") != null && !StringUtil.isNullOrEmpty(configuration.getChild("basedir").getValue()))
					scmDirectory = new File(configuration.getChild("basedir").getValue());
			}

			if (StringUtil.isNullOrEmpty(username))
				username = StringUtil.empty;

			if (StringUtil.isNullOrEmpty(password))
				password = StringUtil.empty;
			
			ScmResult result = executeScm(urlScm, username, password, includes, excludes, scmDirectory, scmManager, reactorProjects, session, project, getLogger());
			if (result != null) {
				if (result.isSuccess())
					info("Success: " + result.getProviderMessage());
				else
					error("Failed: " + result.getProviderMessage());
			}
		} catch(Throwable t) {
			error(t);
			
			if (t instanceof MojoExecutionException)
				throw (MojoExecutionException)t;
			else if (t instanceof MojoFailureException)
				throw (MojoFailureException)t;
			else
				throw new MojoFailureException("Error while executing scm maven plugin", t);
		}
	}
}
