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

package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.PropertyBag;

import org.glassfish.quality.ToDo;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface JmsAvailability extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the availabilityEnabled property.
     *
     * This boolean flag controls whether the MQ cluster associated with the
     * application server cluster is HA enabled or not. If this attribute is
     * "false", then the MQ cluster pointed to by the jms-service element is
     * considered non-HA. JMS Messages are not persisted to a highly available
     * store. If this attribute is "true" the MQ cluster pointed to by the
     * jms-service element is a HA cluster and the MQ cluster uses the database
     * pointed to by mq-store-pool-name to save persistent JMS messages and
     * other broker cluster configuration information. Individual applications
     * will not be able to control or override MQ cluster availability levels.
     * They inherit the availability attribute defined in this element.
     * If this attribute is missing, availability is turned off by default
     * [i.e. the MQ cluster associated with the AS cluster would behave as a
     * non-HA cluster]
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    String getAvailabilityEnabled();

    /**
     * Sets the value of the availabilityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAvailabilityEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mqStorePoolName property.
     *
     * This is the jndi-name for the JDBC Connection Pool used by the MQ broker
     * cluster for use in saving persistent JMS messages and other broker
     * cluster configuration information.  It will default to value of
     * store-pool-name under availability-service (ultimately "jdbc/hastore")
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getMqStorePoolName();

    /**
     * Sets the value of the mqStorePoolName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMqStorePoolName(String value) throws PropertyVetoException;
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
