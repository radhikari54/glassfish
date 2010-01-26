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

package org.glassfish.ejb.deployment.annotation.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import javax.ejb.Stateless;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import org.glassfish.ejb.deployment.annotation.handlers.AbstractEjbHandler;

import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the javax.ejb.Stateless
 *
 * @author Shing Wai Chan
 */
@Service
public class StatelessHandler extends AbstractEjbHandler {
    
    /** Creates a new instance of StatelessHandler */
    public StatelessHandler() {
    }
    
    /**
     * @return the annoation type this annotation handler is handling
     */
    public Class<? extends Annotation> getAnnotationType() {
        return Stateless.class;
    }

    /**
     * Return the name attribute of given annotation.
     * @param annotation
     * @return name
     */
    protected String getAnnotatedName(Annotation annotation) {
        Stateless slAn = (Stateless)annotation;
        return slAn.name();
    }

    /**
     * Check if the given EjbDescriptor matches the given Annotation.
     * @param ejbDesc
     * @param annotation
     * @return boolean check for validity of EjbDescriptor
     */
    protected boolean isValidEjbDescriptor(EjbDescriptor ejbDesc,
            Annotation annotation) {
        boolean isValid = EjbSessionDescriptor.TYPE.equals(ejbDesc.getType());

        if( isValid ) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            // Only check specific session-bean type if it's set in the descriptor.
            // Otherwise it was probably populated with a sparse ejb-jar.xml and
            // we'll set the type later.
            if( sessionDesc.isSessionTypeSet() && !sessionDesc.isStateless() ) {
                isValid = false;
            }
        }

        return  isValid;
    }

    /**
     * Create a new EjbDescriptor for a given elementName and AnnotationInfo.
     * @param elementName
     * @param ainfo
     * @return a new EjbDescriptor
     */
    protected EjbDescriptor createEjbDescriptor(String elementName,
            AnnotationInfo ainfo) throws AnnotationProcessorException {

        AnnotatedElement ae = ainfo.getAnnotatedElement();
        Class ejbClass = (Class)ae;
        EjbSessionDescriptor newDescriptor = new EjbSessionDescriptor();
        newDescriptor.setName(elementName);
        newDescriptor.setEjbClassName(ejbClass.getName());
        newDescriptor.setSessionType(EjbSessionDescriptor.STATELESS);
        return newDescriptor;
    }

    /**
     * Set Annotation information to Descriptor.
     * This method will also be invoked for an existing descriptor with
     * annotation as user may not specific a complete xml.
     * @param ejbDesc
     * @param ainfo
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult setEjbDescriptorInfo(
            EjbDescriptor ejbDesc, AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        EjbSessionDescriptor ejbSessionDesc = (EjbSessionDescriptor)ejbDesc;

         // set session bean type in case it wasn't set in a sparse ejb-jar.xml.
        if( !ejbSessionDesc.isSessionTypeSet() ) {
            ejbSessionDesc.setSessionType(EjbSessionDescriptor.STATELESS);
        }

        Stateless sless = (Stateless) ainfo.getAnnotation();

        doDescriptionProcessing(sless.description(), ejbDesc);
        doMappedNameProcessing(sless.mappedName(), ejbDesc);

        return setBusinessAndHomeInterfaces(ejbDesc, ainfo);
    }
}
