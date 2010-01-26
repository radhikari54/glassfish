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

package org.glassfish.config.support;

import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Habitat;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ExecutorService;
import java.io.IOException;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.appserv.server.util.Version;

/**
 * plug our Dom implementation
 *
 * @author Jerome Dochez
 * 
 */
public class GlassFishDocument extends DomDocument {

    Logger logger = LogDomains.getLogger(GlassFishDocument.class, LogDomains.CORE_LOGGER);

    public GlassFishDocument(final Habitat habitat, final ExecutorService executor) {
        super(habitat);

        // todo, move this to an init service (Globals?)
        ExistingSingletonInhabitant<ExecutorService> executorInhab =
                new ExistingSingletonInhabitant<ExecutorService>(executor);
        
        habitat.addIndex(executorInhab, ExecutorService.class.getName(), "transactions-executor");
        habitat.addIndex(new ExistingSingletonInhabitant<DomDocument>(this), DomDocument.class.getName(), null);

        final DomDocument doc = this;
        
        habitat.getComponent(Transactions.class).addTransactionsListener(new TransactionListener() {
            public void transactionCommited(List<PropertyChangeEvent> changes) {
                for (ConfigurationPersistence pers : habitat.getAllByContract(ConfigurationPersistence.class)) {
                    try {
                        if (doc.getRoot().getProxyType().equals(Domain.class)) {
                            Dom domainRoot = doc.getRoot();
                            domainRoot.attribute("version", Version.getBuildVersion());
                        }
                        pers.save(doc);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "GlassFishDocument.IOException",
                                new String[] { e.getMessage() });
                        logger.log(Level.FINE, e.getMessage(), e);
                    } catch (XMLStreamException e) {
                        logger.log(Level.SEVERE, "GlassFishDocument.XMLException",
                                new String[] { e.getMessage() });
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }

            public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {

            }
        });
    }

    public Dom make(final Habitat habitat, XMLStreamReader xmlStreamReader, Dom dom, ConfigModel configModel) {
        // by default, people get the translated view.
        return new GlassFishConfigBean(habitat,this, dom, configModel, xmlStreamReader);
    }
}
