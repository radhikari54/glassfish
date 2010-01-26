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

package com.sun.enterprise.deployment.node.ejb;

import com.sun.enterprise.deployment.TimeoutValueDescriptor;
import com.sun.enterprise.deployment.Descriptor;


import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.EjbTagNames;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeoutValueNode extends DeploymentDescriptorNode {

    TimeoutValueDescriptor descriptor = null;

    private static final Map<String, TimeUnit> elementToTimeUnit;
    private static final Map<TimeUnit, String> timeUnitToElement;

    static {

        elementToTimeUnit = new HashMap<String, TimeUnit>();
        elementToTimeUnit.put("Days", TimeUnit.DAYS);
        elementToTimeUnit.put("Hours", TimeUnit.HOURS);
        elementToTimeUnit.put("Minutes", TimeUnit.MINUTES);
        elementToTimeUnit.put("Seconds", TimeUnit.SECONDS);
        elementToTimeUnit.put("Milliseconds", TimeUnit.MILLISECONDS);
        elementToTimeUnit.put("Microseconds", TimeUnit.MICROSECONDS);               
        elementToTimeUnit.put("Nanoseconds", TimeUnit.NANOSECONDS);
        
        timeUnitToElement = new HashMap<TimeUnit, String>();
        for(String next : elementToTimeUnit.keySet()) {
            timeUnitToElement.put(elementToTimeUnit.get(next), next);
        }
    }

    /**
     * @return the Descriptor subclass that was populated  by reading
     * the source XML file
     */
    public Object getDescriptor() {
        if (descriptor == null) {
            descriptor = (TimeoutValueDescriptor) new TimeoutValueDescriptor();
        }
        return descriptor;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {

        if (EjbTagNames.TIMEOUT_VALUE.equals(element.getQName())) {
            descriptor.setValue(new Long(value));
        } else if(EjbTagNames.TIMEOUT_UNIT.equals(element.getQName())) {
            descriptor.setUnit(elementToTimeUnit.get(value));
        } else {
            super.setElementValue(element, value);
        }
    }
        
   /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, Descriptor descriptor) {
        if (! (descriptor instanceof TimeoutValueDescriptor)) {
            throw new IllegalArgumentException(getClass() + " cannot handles descriptors of type " + descriptor.getClass());
        }
        TimeoutValueDescriptor desc = (TimeoutValueDescriptor) descriptor;

        Node timeoutNode = super.writeDescriptor(parent, nodeName, descriptor);


        appendTextChild(timeoutNode, EjbTagNames.TIMEOUT_VALUE, Long.toString(desc.getValue()));
        appendTextChild(timeoutNode, EjbTagNames.TIMEOUT_UNIT, timeUnitToElement.get(desc.getUnit()));

        return timeoutNode;     
     }


}
