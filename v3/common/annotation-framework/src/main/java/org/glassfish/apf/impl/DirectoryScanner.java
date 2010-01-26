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

package org.glassfish.apf.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashSet;
import java.net.URL;
import java.net.URLClassLoader;

import org.glassfish.apf.Scanner;

/**
 * Implementation of the Scanner interface for a directory
 *
 * @author Jerome Dochez
 */
public class DirectoryScanner extends JavaEEScanner implements Scanner {
    
    File directory;
    Set<String> entries = new HashSet<String>();
    ClassLoader classLoader = null;

    public void process(File directory, Object bundleDesc, ClassLoader classLoader)
            throws IOException {
        AnnotationUtils.getLogger().finer("dir is " + directory);
        AnnotationUtils.getLogger().finer("classLoader is " + classLoader);
        this.directory = directory;
        this.classLoader = classLoader;
        init(directory);
    }       
    
    private void init(File directory) throws java.io.IOException {
        init(directory, directory);
    } 
    
    private void init(File top, File directory) throws java.io.IOException {
        
        File[] dirFiles = directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getAbsolutePath().endsWith(".class");
                }
        });
        for (File file : dirFiles) {
            entries.add(file.getPath().substring(top.getPath().length()+1));
        }
        
        File[] subDirs = directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
        });
        for (File subDir : subDirs) {
            init(top, subDir);
        }
    }
    
    protected Set<String> getEntries() {
        return entries;
    }

    public ClassLoader getClassLoader() {
        if (classLoader==null) {
            URL[] urls = new URL[1];
            try {
                urls[0] = directory.getAbsoluteFile().toURL();
                classLoader = new URLClassLoader(urls);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return classLoader;
    }

    public Set<Class> getElements() {
        
        
        Set<Class> elements = new HashSet<Class>();
        if (getClassLoader()==null) {
            AnnotationUtils.getLogger().severe("Class loader null");
            return elements;
        }        
        for (String fileName : entries) {
            // convert to a class name...
            String className = fileName.replace(File.separatorChar, '.');
            className = className.substring(0, className.length()-6);
            System.out.println("Getting " + className);
            try {                
                elements.add(classLoader.loadClass(className));
                
            } catch(Throwable cnfe) {
                AnnotationUtils.getLogger().severe("cannot load " + className + " reason : " + cnfe.getMessage());
            }
        }
        return elements;
    }
}
