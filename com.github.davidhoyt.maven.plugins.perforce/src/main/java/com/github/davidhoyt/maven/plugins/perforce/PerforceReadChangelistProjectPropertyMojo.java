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
import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.IServer;
import java.util.List;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * Performs operations on a perforce repository.
 * 
 * @goal read-changelist-project-property
 * 
 * @author David Hoyt <dhoyt@hoytsoft.org>
 */
public class PerforceReadChangelistProjectPropertyMojo extends PerforceScmServerFileSetMojo {
	//<editor-fold defaultstate="collapsed" desc="Constants">
	public static final String 
		DEFAULT_PROPERTY_NAME = "p4.changelist"
	;
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Variables">
	/**
	 * The name of the property to write the changelist number to.
	 * 
	 * @parameter expression="${propertyName}" default-value="p4.changelist"
	 */
	private String propertyName;
	//</editor-fold>
	
	@Override
	protected boolean executePerforceScmServerFileSet(final IServer server, final List<IFileSpec> fileSpecs, final ScmProviderRepository repo, final String repositoryPath, final String username, final String password, final ScmLogger logger) throws Throwable {
		//<editor-fold defaultstate="collapsed" desc="Validate arguments">
		String propName = propertyName;
		if (StringUtil.isNullOrEmpty(propName))
			propName = DEFAULT_PROPERTY_NAME;
		//</editor-fold>
		
		String changelist_as_string = server.getCounter("change");
		
		info("Setting the \"" + propName + "\" project property to \"" + changelist_as_string + "\"");
		final MavenProject project = getProject();
		final Properties props = project.getProperties();
		props.setProperty(propName, changelist_as_string);

		info("Changelist successfully read as a project property.");
		return true;
	}
}
