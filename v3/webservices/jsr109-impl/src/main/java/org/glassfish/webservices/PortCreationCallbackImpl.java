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

package org.glassfish.webservices;

import java.util.Set;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Binding;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.xml.ws.api.client.ServiceInterceptor;
import com.sun.xml.ws.developer.WSBindingProvider;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.logging.LogDomains;

/**
 * This is way port creation calls are going to be intercepted in JAXWS2.1
 */
public class PortCreationCallbackImpl extends ServiceInterceptor {

    private ServiceReferenceDescriptor ref;

    private static Logger logger = LogDomains.getLogger(PortCreationCallbackImpl.class,LogDomains.WEBSERVICES_LOGGER);
    
    public PortCreationCallbackImpl(ServiceReferenceDescriptor svcRef) {
        ref = svcRef;
    }
    
    public void postCreateProxy(WSBindingProvider bp, Class<?> serviceEndpointInterface) {
        
        ServiceRefPortInfo portInfo = ref.getPortInfoBySEI(serviceEndpointInterface.getName());
        if (portInfo!=null) {          
            // Set MTOM for this port
            boolean mtomEnabled = false;
            if(portInfo.getMtomEnabled() != null &&
                (new Boolean(portInfo.getMtomEnabled())).booleanValue()) {
                mtomEnabled = true;
            }
            if (mtomEnabled) {
                Binding bType = bp.getBinding();
                // enable mtom valid only for SOAPBindings
                if(SOAPBinding.class.isAssignableFrom(bType.getClass())) {
                    ((SOAPBinding)bType).setMTOMEnabled(true);
                } else {
                    logger.log(Level.SEVERE,
                            "serviceref.invalidmtom");
                }
            }
            
            // Set stub properties
            Set properties = portInfo.getStubProperties();            
            for(Iterator iter = properties.iterator(); iter.hasNext();) {
                NameValuePairDescriptor next = (NameValuePairDescriptor) 
                    iter.next();
                bp.getRequestContext().put(next.getName(), next.getValue());
                
            }
        }        
    }

    public void postCreateDispatch(WSBindingProvider bp) {}
}
