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

package com.sun.enterprise.deployment.annotation.context;

import com.sun.enterprise.deployment.EjbDescriptor;
import org.glassfish.apf.context.AnnotationContext;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;


/**
 * This provides a context for a collection of Ejbs with the ejb class name.
 *
 * @Author Shing Wai Chan
 */
public class EjbsContext extends AnnotationContext 
        implements ComponentContext {
    
    private EjbContext[] ejbContexts;
    private String componentClassName;

    public EjbsContext(EjbDescriptor[] ejbDescs, Class ejbClass) {
        ejbContexts = new EjbContext[ejbDescs.length];
        for (int i = 0; i < ejbDescs.length ; i++) {
            ejbContexts[i] = new EjbContext(ejbDescs[i], ejbClass);
        }
        this.componentClassName = ejbClass.getName();
    }
   
    /**
     * Create a new instance of EjbContext.
     * Note that, for performance, we don't make a safe copy of array here.
     */
    public EjbsContext(EjbContext[] ejbContexts) {
        this.ejbContexts = ejbContexts;
        this.componentClassName = ejbContexts[0].getComponentClassName();
    }

    /**
     * Note that, for performance, we don't make a safe copy of array here.
     */
    public EjbContext[] getEjbContexts() {
        return ejbContexts;
    }
    
    public void endElement(ElementType type, AnnotatedElement element) {
        
        if (ElementType.TYPE.equals(type)) {
            // done with processing this class, let's pop this context
            getProcessingContext().popHandler();
        }
    }

    public String getComponentClassName() {
        return componentClassName;
    }
      
}
