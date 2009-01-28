/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * SourceAppSrvObj.java
 *
 * This class contain data specific to the source domain.
 *
 * Created on December 3, 2007, 3:55 PM
 *
 */

package com.sun.enterprise.tools.upgrade.common;

import java.io.File;

/**
 *
 * @author rebeccas
 */
public class SourceAppSrvObj  extends BaseDomainInfoObj{
	//- appsever login credential of the source domain.
	Credentials credentials = new DomainCredentials();
	private String backupDomainDir = null;
	
	/** Creates a new instance of SourceAppSrvObj */
	public SourceAppSrvObj() {
		super();
	}
	
	public boolean isValidPath(String s){
		boolean flag = false;
		if (s != null){
			// server config file must be available
			File domainXML = new File(s + "/" + super.CONFIG_DOMAIN_XML_FILE);
			if(domainXML.exists() && domainXML.isFile()){
				flag = true;
			}
		}
		return flag;
	}
	
	public String getVersionEdition(){
		if (versionEdition == null){
			VersionExtracter v = new VersionExtracter(installDir,
				CommonInfoModel.getInstance());
			versionEdition = v.extractVersionFromConfigFile(srvConfigFile);
			if (versionEdition != null){
				extractVersionAndEdition(UpgradeConstants.DELIMITER);
			}
		}
		return versionEdition;
	}
	
	//------------------------------------
	public Credentials getDomainCredentials(){
		return credentials;
	}
	
	/**
	 * For an inplace upgrade (only) the location of
	 * the domain in the backup area.
	 */
	public void setBackupDomainDir(String s){
		backupDomainDir = s;
	}
	
	public String getBackupConfigXMLFile(){
		return backupDomainDir + "/" + super.CONFIG_DOMAIN_XML_FILE;
	}
}
