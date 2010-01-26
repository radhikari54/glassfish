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

package org.glassfish.connectors.admin.cli;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

/**
 * Delete RA Config command
 *
 */
@Service(name="delete-resource-adapter-config")
@Scoped(PerLookup.class)
@I18n("delete.resource.adapter.config")
public class DeleteResourceAdapterConfig implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteResourceAdapterConfig.class);

    @Param(name="raname", primary=true)
    String raName;

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Inject
    Resources resources;

    @Inject
    ResourceAdapterConfig[] raConfigs;

    @Inject
    Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        Server targetServer = domain.getServerNamed(target);
        if (raName== null) {
            report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.noRARName",
                            "No RAR name defined for resource adapter config."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we already have this resource
        if (!isResourceExists(resources, raName)) {
            report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.notfound",
                    "Resource-Adapter-Config for {0} does not exist.", raName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            // delete resource-adapter-config
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    for (ResourceAdapterConfig resource : raConfigs) {
                        if (resource.getResourceAdapterName().equals(raName)) {
                            return param.getResources().remove(resource);
                        }
                    }
                    // not found
                    return null;
                }
            }, resources) == null) {
                report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.fail",
                                "Unable to delete resource adapter config {0}", raName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            // delete resource-ref
            targetServer.deleteResourceRef(raName);

        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.fail",
                            "Unable to delete resource adapter config {0}", raName)
                            + " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        //report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.success",
        //        "Resource adapter config {0} deleted", raName));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private boolean isResourceExists(Resources resources, String raName) {

        for (Resource resource : resources.getResources()) {
            if (resource instanceof ResourceAdapterConfig) {
                if (((ResourceAdapterConfig) resource).getResourceAdapterName().equals(raName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
