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
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.IServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * Performs operations on a perforce repository.
 * 
 * @goal write-changelist-properties
 * 
 * @author David Hoyt <dhoyt@hoytsoft.org>
 */
public class PerforceWriteChangelistPropertiesMojo extends PerforceScmServerFileSetMojo {
	//<editor-fold defaultstate="collapsed" desc="Constants">
	public static final String 
		DEFAULT_PROPERTY_NAME = "P4_CHANGELIST"
	;
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Variables">
	/**
	 * The file to write to.
	 * 
	 * @parameter expression="${outputFile}"
	 * @required
	 */
	private File outputFile;
	
	/**
	 * The name of the property to write to.
	 * 
	 * @parameter expression="${propertyName}" default-value="P4_CHANGELIST"
	 */
	private String propertyName;
	
	/**
	 * True if you want to overwrite the file if the operation is successful.
	 * 
	 * @parameter expression="${overwrite}" default-value=false
	 */
	private Boolean overwrite;
	//</editor-fold>
	
	@Override
	protected boolean executePerforceScmServerFileSet(final IServer server, final List<IFileSpec> fileSpecs, final ScmProviderRepository repo, final String repositoryPath, final String username, final String password, final ScmLogger logger) throws Throwable {
		//<editor-fold defaultstate="collapsed" desc="Validate arguments">
		if (outputFile == null)
			throw new MojoFailureException("Missing the output file.");
		if (outputFile.isDirectory())
			throw new MojoFailureException("Cannot output to a directory: " + outputFile);
		boolean should_overwrite = false;
		if (overwrite != null)
			should_overwrite = overwrite.booleanValue();
		String propName = propertyName;
		if (StringUtil.isNullOrEmpty(propName))
			propName = DEFAULT_PROPERTY_NAME;
		//</editor-fold>
		
		String changelist_as_string = server.getCounter("change");
		
		Properties props = new Properties();
		if (!should_overwrite && outputFile.exists()) {
			info("Loading property file from " + outputFile);
			
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(outputFile);
				props.load(fis);
			} finally {
				if (fis != null)
					fis.close();
			}
		}
		
		info("Setting the " + propName + " property to " + changelist_as_string);
		props.setProperty(propName, changelist_as_string);
		
		info("Writing property file to " + outputFile);
		
		//Make all sub directories.
		File parent = outputFile.getParentFile();
		if (parent != null)
			parent.mkdirs();
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputFile);
			props.store(fos, null);
		} finally {
			if (fos != null)
				fos.close();
		}

		info("Changelist successfully written to property file.");
		return true;
	}
}
