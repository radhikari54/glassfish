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

import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.xml.ws.api.server.WSEndpoint;
import javax.servlet.ServletContext;
import com.sun.xml.ws.transport.http.servlet.ServletModule;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

public class JAXWSContainer extends Container {
    
    private final ServletContext servletContext;
    private final WebServiceEndpoint endpoint;
    private final JAXWSServletModule module;

    public JAXWSContainer(ServletContext servletContext,
                    WebServiceEndpoint ep) {
        this.servletContext = servletContext;
        this.endpoint = ep;
        
        if (servletContext != null) {
            this.module = JAXWSServletModule
                .getServletModule(servletContext.getContextPath());
        } else {
            this.module = null;
        }
       
    }
    
    public void addEndpoint( ServletAdapter adapter) {
  
        if (module != null) {
            module.addEndpoint(endpoint.getEndpointAddressUri(), 
                                    adapter);
        }

    }

    public <T> T getSPI(Class<T> spiType) {
        if (spiType == ServletContext.class) {
            return (T)servletContext;
        }
        if((spiType == com.sun.xml.ws.api.server.ServerPipelineHook.class) ||
           (spiType == com.sun.xml.ws.assembler.ServerPipelineHook.class)){
            Habitat h = Globals.getDefaultHabitat();
            ServerPipeCreator s = h.getByContract(ServerPipeCreator.class);
            s.init(endpoint);
            return((T)s);
        }
        if(spiType == ResourceInjector.class) {
            // Give control of injection time only for servlet endpoints
            if(endpoint.implementedByWebComponent()) {
                return (T) new ResourceInjectorImpl(endpoint);
            }
        }
        if (spiType.isAssignableFrom(ServletModule.class)) {
            
            if (module != null) {
                return ((T)spiType.cast(module));
            }
        }
        return null;
    }
}
