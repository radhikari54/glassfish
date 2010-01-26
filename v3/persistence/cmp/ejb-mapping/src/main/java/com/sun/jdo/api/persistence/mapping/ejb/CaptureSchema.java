/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * CaptureSchema.java
 *
 * Created on March 15, 2002, 4:02 PM
 */

package com.sun.jdo.api.persistence.mapping.ejb;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.jdbcimpl.*;
import org.netbeans.modules.dbschema.util.*;

import java.io.*;

import java.util.ResourceBundle;

import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import java.text.MessageFormat;

/**
 *
 * @author  vkraemer
 * @version 1.0
 */
public class CaptureSchema {
    /** Required filename extension. */
    private static final String OUTPUTFILE_EXTENSION = ".dbschema"; // NOI18N
    
    /** Creates new CaptureSchema */
    public CaptureSchema() {
    }
    
    public static void main(String args[]) {
        int help = 0;
        ResourceBundle bundle =
            ResourceBundle.getBundle("com.sun.jdo.api.persistence.mapping.ejb.Bundle"); // NOI18N
        String driver=bundle.getString("STRING_ORACLE_DRIVER_NOI18N"); //NOI18N
        String username=bundle.getString("STRING_IASCTS_NOI18N"); //NOI18N
        String password=bundle.getString("STRING_IASCTS_NOI18N"); //NOI18N
        String dburl=null;
        String dbschemaname = null;
        String outfile = null;
        LinkedList tableList = new LinkedList();
        LinkedList vList = new LinkedList();
        try {
            for (int i=0; i<args.length; i++) {
                if (args[i].equals(bundle.getString("CMD_FLAG_DRIVER"))) { //NOI18N
                    driver = args[++i];
                    help++;
                }
                else if (args[i].equals(bundle.getString("CMD_FLAG_SCHEMA_NAME"))) { //NOI18N
                    dbschemaname = args[++i];
                    help++;
                    
                }
                else if (args[i].equals(bundle.getString("CMD_FLAG_USERNAME"))) { //NOI18N
                    username = args[++i];
                    help++;
                    
                }
                else	if (args[i].equals(bundle.getString("CMD_FLAG_PASSWORD"))) { //NOI18N
                    password = args[++i];
                    help++;
                }
                else	if (args[i].equals(bundle.getString("CMD_FLAG_DBURL"))) { //NOI18N
                    dburl = args[++i];
                    help++;
                }
                else	if (args[i].equals(bundle.getString("CMD_FLAG_TABLE"))) { //NOI18N
                    tableList.add(args[++i]);
                    //vList.add(args[i]);
                    help++;
                }
                else	if (args[i].equals(bundle.getString("CMD_FLAG_OUTPUT"))) { //NOI18N
                    outfile = args[++i];
                    help++;
                }
                
            }
        }
        catch(Exception e) {
            help = 0;
        }
        
        if (help < 1 || null == outfile) {
            System.err.println(bundle.getString("HELP_USAGE_LABEL")); //NOI18N
            System.err.println(bundle.getString("HELP_USAGE")); //NOI18N
            System.err.println(bundle.getString("HELP_WHERE")); //NOI18N
            System.err.println(bundle.getString("HELP_USERNAME_ARG")); //NOI18N
            System.err.println(bundle.getString("HELP_PASSWORD_ARG")); //NOI18N
            System.err.println(bundle.getString("HELP_URL_ARG")); //NOI18N
            System.err.println(bundle.getString("HELP_DRIVER_ARG")); //NOI18N
            System.err.println(bundle.getString("HELP_OUTPUT_ARG")); //NOI18N
            System.err.println(bundle.getString("HELP_SCHEMA_ARG")); //NOI18N
            System.err.println(bundle.getString("HELP_TABLE_ARG")); //NOI18N
            System.err.println(bundle.getString("HELP_OPTIONAL_INFO")); //NOI18N
            System.exit(0);
        }

        // Ensure that outfile ends with OUTPUTFILE_EXTENSION, warning user if
        // we change their given name.
        if (!outfile.endsWith(OUTPUTFILE_EXTENSION)) {
            System.err.println(bundle.getString("MESSAGE_CHANGING_OUTFILENAME") // NOI18N
                               + OUTPUTFILE_EXTENSION);
            outfile += OUTPUTFILE_EXTENSION;
        }
        
        ConnectionProvider cp = null;
        try {
            System.err.println(bundle.getString("MESSAGE_USING_URL")+ dburl); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_USERNAME")+ username); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_PASSWORD")+ password); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_DRIVER")+ driver); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_SCHEMANAME")+ dbschemaname); //NOI18N
            System.err.println(bundle.getString("MESSAGE_USING_OUTFILENAME")+ outfile); //NOI18N
            
            // prepare the connection
            cp = new ConnectionProvider(driver, dburl,username, password);
            if (null != dbschemaname)
                cp.setSchema(dbschemaname); //schema in the database you want to capture; needs to be improved and set in constructor probably
            
            System.err.println(bundle.getString("MESSAGE_CAPTURING_SCHEMA") + dbschemaname);//NOI18N

            SchemaElementImpl outSchemaImpl = new SchemaElementImpl(cp);
            SchemaElement se = new SchemaElement(outSchemaImpl);
            
            if (null != dbschemaname)
                se.setName(DBIdentifier.create(bundle.getString("STRING_SCHEMAS_SLASH_NOI18N") + dbschemaname));//NOI18N
            else 
                se.setName(DBIdentifier.create("")); //NOI18N
            
            if (dburl.indexOf(bundle.getString("STRING_ORACLE_JDBC_URL_PREFIX_NOI18N")) > -1 && //NOI18N
            (null == dbschemaname || dbschemaname.length() == 0)) {
                // this argument combo has problems. print an error message and exit
                System.err.println(bundle.getString("ERR_ORACLE_ARGUMENTS")); //NOI18N
                return;
            }
            
            if (tableList.size() == 0) {
                outSchemaImpl.initTables(cp);
            }
            else {
                pruneTableList(tableList, cp,bundle);
                if (tableList.size() > 0)
                    outSchemaImpl.initTables(cp, tableList,vList,false);
                else {
                    System.err.println(bundle.getString("MESSAGE_NO_VALID_TABLES")); //NOI18N
                    return;
                }
            }
            System.err.println(bundle.getString("MESSAGE_SCHEMA_CAPTURED")); //NOI18N
            
            System.err.println(bundle.getString("MESSAGE_SAVING_SCHEMA")); //NOI18N
            OutputStream outstream = new FileOutputStream(outfile);
            se.save(outstream);
        }
        catch (java.lang.ClassNotFoundException cnfe) {
            System.err.println(bundle.getString("ERR_CHECK_CLASSPATH")); //NOI18N
            cnfe.printStackTrace(System.err); //NOI18N
        }
        catch (Exception exc) {
            exc.printStackTrace(System.err); //NOI18N
        }
        finally {
            if (cp != null) {
                cp.closeConnection();
            }
        }
    }
    
    private static void pruneTableList(List tableList, ConnectionProvider cp, 
            ResourceBundle bundle) throws java.sql.SQLException {
        ResultSet rs;
        DatabaseMetaData dmd = cp.getDatabaseMetaData();
        Map tables = new HashMap();
        String catalog = cp.getConnection().getCatalog();
        String user = cp.getSchema();

        rs = dmd.getTables(catalog, user, "%", new String[] {"TABLE"}); //NOI18N
        if (rs != null) {
            while (rs.next()) {
                String tn = rs.getString("TABLE_NAME").trim(); //NOI18N
                tables.put(tn,tn);
            }
            rs.close();
        }
        Iterator iter = tableList.iterator();
        String [] args = new String[1];
        while (iter.hasNext()) {
            String s = (String) iter.next();
            if (null == tables.get(s.trim())) {
                iter.remove();
                args[0] = s;
                System.err.println(MessageFormat.format(bundle.getString("ERR_INVALID_TABLE_GIVEN"), (Object []) args)); //NOI18N
            }
        }
    }
           
    static class PropChangeReport implements java.beans.PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent pce) {
            //System.err.println(pce); //NOI18N
        }        
    }
}
