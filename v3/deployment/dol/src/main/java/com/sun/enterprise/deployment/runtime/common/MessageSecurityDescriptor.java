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

package com.sun.enterprise.deployment.runtime.common;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

import java.util.ArrayList;

public class MessageSecurityDescriptor extends RuntimeDescriptor {
    public static final String MESSAGE = "Message";
    public static final String REQUEST_PROTECTION = "RequestProtection";
    public static final String RESPONSE_PROTECTION = "ResponseProtection";

    private ArrayList messageDescs = new ArrayList();
    private ProtectionDescriptor requestProtectionDesc = null;
    private ProtectionDescriptor responseProtectionDesc = null;

    public MessageSecurityDescriptor() {}

    public void addMessageDescriptor(MessageDescriptor messageDesc) {
       messageDescs.add(messageDesc);
    }
    
    public ArrayList getMessageDescriptors() {
        return messageDescs;
    }

    public ProtectionDescriptor getRequestProtectionDescriptor() {
        return requestProtectionDesc;
    }

    public void setRequestProtectionDescriptor(ProtectionDescriptor proDesc) {
        requestProtectionDesc = proDesc;
    }

    public ProtectionDescriptor getResponseProtectionDescriptor() {
        return responseProtectionDesc;
    }

    public void setResponseProtectionDescriptor(ProtectionDescriptor proDesc) {
        responseProtectionDesc = proDesc;
    }

    // return all the methods defined in the message elements inside this 
    // message-security element
    public ArrayList getAllMessageMethods() {
        //FIXME
        // need to convert operation to message
        // need to union all the methods
        return new ArrayList();
    }

}
