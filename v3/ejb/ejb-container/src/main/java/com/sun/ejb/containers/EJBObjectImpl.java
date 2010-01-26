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

package com.sun.ejb.containers;

import java.rmi.RemoteException;
import java.lang.reflect.Method;

import javax.ejb.*;
import com.sun.ejb.portable.*;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.Map;
import java.util.HashMap;

import java.util.logging.*;

/**
 * EJBObjectImpl implements EJBObject methods for EJBs.
 * It is extended by the generated concrete type-specific EJBObject
 * implementation (e.g. Hello_EJBObject).
 * Instances of this class are NEVER given to beans or clients.
 * Beans and clients get only stubs (instances of the stub class
 * generated by rmic).
 *
 */

public abstract class EJBObjectImpl
    extends EJBLocalRemoteObject
    implements EJBObject
{
    private static Class[] NO_PARAMS = new Class[] {};    
    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(EJBObjectImpl.class);
    private static Method REMOVE_METHOD = null;

    static {

        try {
            REMOVE_METHOD = EJBObject.class.getMethod("remove", NO_PARAMS);
        } catch ( NoSuchMethodException e ) {
            _logger.log(Level.FINE, "Exception retrieving remove method", e);
        }
    }

    transient private java.rmi.Remote stub;
    transient private java.rmi.Remote ejbObject;
    transient private Map businessStubs = new HashMap(); 
    transient private Map businessEJBObjects = new HashMap(); 
    transient private boolean beingCreated=false;
    
    // True if this object instance represents the RemoteHomeview
    // False if this object instance represents the RemoteBusiness view
    private boolean isRemoteHomeView;                    

    protected EJBObjectImpl() throws RemoteException {
    }
    
    final void setStub(java.rmi.Remote stub) {
        this.stub = stub;
    }
    
    /**
     * Stubs are keyed by the name of generated RMI-IIOP version of
     * each remote business interface.
     */
    final void setStub(String generatedBusinessInterface, 
                       java.rmi.Remote stub) {
        businessStubs.put(generatedBusinessInterface, stub);
    }
    
    public final java.rmi.Remote getStub() {
        return stub;
    }

    public final java.rmi.Remote getStub(String generatedBusinessInterface) {
        return (java.rmi.Remote) businessStubs.get(generatedBusinessInterface);
    }

    void setIsRemoteHomeView(boolean flag) {
        isRemoteHomeView = flag;
    }

    boolean isRemoteHomeView() {
        return isRemoteHomeView;
    }

    /**
     * Get the Remote object corresponding to an EJBObjectImpl for
     * the RemoteHome view.
     */
    public java.rmi.Remote getEJBObject() {
        return ejbObject;
    }

    /**
     * Get the Remote object corresponding to an EJBObjectImpl for
     * the RemoteBusiness view.
     */
    public java.rmi.Remote getEJBObject(String generatedBusinessInterface) {
        return (java.rmi.Remote) businessEJBObjects.get
            (generatedBusinessInterface);
    }

    public void setEJBObject(java.rmi.Remote ejbObject) {
        this.ejbObject = ejbObject;
    }

    public void setEJBObject(String generatedBusinessInterface, 
                             java.rmi.Remote ejbObject) {
        businessEJBObjects.put(generatedBusinessInterface, ejbObject);
    }

    final void setBeingCreated(boolean b) {
        beingCreated = b;
    }
    
    final boolean isBeingCreated() {
        return beingCreated;
    }
    
    /**************************************************************************
    The following are implementations of EJBObject methods.
     **************************************************************************/
    /**
     */
    public boolean isIdentical(EJBObject ejbo) throws RemoteException {
        container.authorizeRemoteMethod(BaseContainer.EJBObject_isIdentical);
        
        return container.isIdentical(this, ejbo);
    }
    
    
    /**
     */
    public Object getPrimaryKey() throws RemoteException {
        if ( container instanceof EntityContainer ) {
            container.authorizeRemoteMethod(
                BaseContainer.EJBObject_getPrimaryKey);
            
            return primaryKey;
        }
        else {
            throw new RemoteException(localStrings.getLocalString(
                "containers.invalid_operation",
                "Invalid operation for Session EJBs."));
        }
    }
    
    /**
     *
     */
    public final EJBHome getEJBHome() throws RemoteException {
        container.authorizeRemoteMethod(BaseContainer.EJBObject_getEJBHome);
        
        return container.getEJBHomeStub();
    }
    
    /**
     * This is called when the EJB client does ejbref.remove().
     * or EJBHome/LocalHome.remove(primaryKey).
     * Since there is no generated code in the *_EJBObjectImpl class
     * for remove, we need to call preInvoke, postInvoke etc here.
     */
    public final void remove() throws RemoteException, RemoveException {

        // authorization is performed within container

        container.removeBean(this, REMOVE_METHOD, false);
    }
    
    /**
     * This is called when the EJB client does ejbref.getHandle().
     * Return a serializable implementation of javax.ejb.Handle.
     */
    public final Handle getHandle() throws RemoteException {
        container.authorizeRemoteMethod(BaseContainer.EJBObject_getHandle);

        // We can assume the stub an EJBObject since getHandle() is only
        // visible through the RemoteHome view.
        return new HandleImpl((EJBObject)stub);
    }
}
