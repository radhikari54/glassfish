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

package com.sun.enterprise.deployment.node.web;

import java.util.Map;

import com.sun.enterprise.deployment.AbsoluteOrderingDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.WebTagNames;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This node is responsible for handling the absolute-ordering xml tree
 *
 * @author  Shing Wai Chan
 * @version 
 */
public class AbsoluteOrderingNode extends DeploymentDescriptorNode {

   public final static XMLElement tag = new XMLElement(WebTagNames.ABSOLUTE_ORDERING);

   protected AbsoluteOrderingDescriptor descriptor = null;

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public AbsoluteOrderingDescriptor getDescriptor() {
        if (descriptor==null) {
            descriptor = (AbsoluteOrderingDescriptor) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return descriptor;
    }  

   /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebTagNames.COMMON_NAME, "addName");
        return table;
    }

    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    public void startElement(XMLElement element, Attributes attributes) {
        super.startElement(element, attributes);

        if (WebTagNames.OTHERS.equals(element.getQName())) {
            descriptor.addOthers();
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree 
     * @param node name for the root element of this xml fragment      
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, AbsoluteOrderingDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        for (Object obj : descriptor.getOrdering()) {
            if (obj instanceof String) {
                appendTextChild(myNode, WebTagNames.COMMON_NAME, (String)obj);
            } else { // others
                appendChild(myNode, WebTagNames.OTHERS);
            }
        }
        return myNode;
    }
}
