/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin.adapter;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.SystemApplications;
import com.sun.enterprise.v3.data.ApplicationRegistry;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.v3.services.impl.ApplicationLoaderService;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyOutputBuffer;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.glassfish.api.container.Adapter;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;

/**
 * An HK-2 Service that provides the functionality so that admin console access is handled properly.
 * The general contract of this adapter is as follows:
 * <ol>
 * <li>This adapter is *always* installed as a Grizzly adapter for a particular
 *     URL designated as admin URL in domain.xml. This translates to context-root
 *     of admin console application. </li>
 * <li>When the control comes to the adapter for the first time, user is asked
 *     to confirm if downloading the application is OK. In that case, the admin console
 *     application is downloaded and expanded. While the download and installation
 *     is happening, all the clients or browser refreshes get a status message.
 *     No push from the server side is attempted (yet).
 *     After the application is "installed", ApplicationLoaderService is contacted,
 *     so that the application is loaded by the containers. This application is
 *     available as a <code> system-application </code> and is persisted as
 *     such in the domain.xml. </li>
 * <li>Even after this application is available, we don't load it on server
 *     startup by default. It is always loaded <code> on demand </code>.
 *     Hence, this adapter will always be available to find
 *     out if application is loaded and load it in the container(s) if it is not.
 *     If the application is already loaded, it simply exits.
 * </li>
 * </ol>
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3 (March 2008)
 */
@Service
public final class AdminConsoleAdapter extends GrizzlyAdapter implements Adapter, PostConstruct {

    @Inject 
    ServerEnvironment env;
    
    @Inject 
    AdminService as; //need to take care of injecting the right AdminService
    
    private String contextRoot;
    private final List<URL>  urls = new ArrayList<URL>();
    private File diskLocation;
    private String proxyHost;
    private int proxyPort;
    private long visitorId;
    private AdapterState state      = AdapterState.UNINITIAZED; //handle with care
    private ProgressObject progress = new ProgressObject();
    
    private final ReentrantLock lock = new ReentrantLock();
    
    @Inject
    private Logger log;
    
    @Inject
    ApplicationRegistry appRegistry;
    
    @Inject
    SystemApplications sysapps;

    @Inject
    Habitat habitat;
    
    private String statusHtml;
    private String initHtml;

    //don't change the following without changing the html pages
    
    private static final String PROXY_HOST_PARAM = "proxyHost";
    private static final String PROXY_PORT_PARAM = "proxyPort";
    private static final String OK_PARAM         = "ok";
    //private static final String CANCEL_PARAM     = "cancel";
    private static final String VISITOR_PARAM    = "visitor";
    
    private static final String VISITOR_TOKEN    = "%%%VISITOR%%%";
    private static final String MYURL_TOKEN      = "%%%MYURL%%%";
    private static final String STATUS_TOKEN     = "%%%STATUS%%%";
    
    static final String ADMIN_APP_NAME           = "admingui";
    static final String ADMIN_APP_WAR            = ADMIN_APP_NAME + ".war";
    public AdminConsoleAdapter() throws IOException {
        initHtml   = Utils.packageResource2String("downloadgui.html");
        statusHtml = Utils.packageResource2String("status.html");
    }
    
    public String getContextRoot() {
       return contextRoot; //default is /admin
    }

    public void afterService(GrizzlyRequest req, GrizzlyResponse res) throws Exception {
        
    }

    public void fireAdapterEvent(String type, Object data) {
    }

    public void service(GrizzlyRequest req, GrizzlyResponse res) {
        logRequest(req);
        
        if (state == AdapterState.APPLICATION_NOT_INSTALLED)
            handleNotInstalledState(req, res);
        else if (state == AdapterState.INSTALLING)
            handleInstallingState(req, res);
        else if (state == AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED)
            handleInstalledButNotLoadedState(req, res);
        else          // state has to be APPLICATION_LOADED
            handleLoadedState();
    }
    
    public void postConstruct() {
        lock.lock();
        //set up the environment properly
        init();        
    }
    
    public void ready() {
        lock.unlock();
        if (log != null) 
            log.info("AdminConsoleAdapter is ready ...");
    }
    
    private void init() {
        List<Property> props = as.getProperty();
        for (Property prop : props) {
            setContextRoot(prop);
            setDownloadLocations(prop);
            setLocationOnDisk(prop);
        }
        initState();
    }
    
    private void initState() {
        //it is a given that the application is NOT loaded to begin with
        if(appExistsInConfig())
            state = AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED;
        else
            state = AdapterState.APPLICATION_NOT_INSTALLED;
    }


    
    private boolean appExistsInConfig() {
        return getConfig()!=null;
    }

    private Application getConfig() {
        //no application-ref logic here -- that's on purpose for now
        for(Module m : sysapps.getModules()) {
            if (m instanceof Application) {
                String name = ServerEnvironment.DEFAULT_ADMIN_CONSOLE_APP_NAME;
                if (name.equals(m.getName())) {
                    return (Application) m;
                }
            }
        }
        return ( null );
    }
    private void logRequest(GrizzlyRequest req) {
        log.info("AdminConsoleAdapter's STATE IS: " +  this.state);
        if (log.isLoggable(Level.FINE)) { //Change all INFO to FINE
            log.log(Level.FINE, "Current Thread: " + Thread.currentThread().getName());
            Enumeration names = req.getParameterNames();
            while(names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String values = Arrays.toString(req.getParameterValues(name));
                log.info("Parameter name: " + name + " values: " + values);
            }
        }
    }
    private void setContextRoot(Property prop) {
        if(ServerTags.ADMIN_CONSOLE_CONTEXT_ROOT.equals(prop.getName())) {
            if (prop.getValue() != null && prop.getValue().startsWith("/")) {
                contextRoot = prop.getValue();
                log.info("Admin Console Adapter: context root: " + contextRoot);
            } else {
                log.info("Invalid context root for the admin console application, using default:" + ServerEnvironment.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT);
                contextRoot = ServerEnvironment.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            }
        }        
    }
    private void setDownloadLocations(Property prop) {
        if (ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION.equals(prop.getName())) {
            String value = prop.getValue();
            if (value != null && !"".equals(value)) {
                Pattern sp = Pattern.compile("\\|");
                String[] strings = sp.split(value);
                for (String s : strings) {
                    try {
                        urls.add(new URL(s));
                        logFine(s);
                    } catch(MalformedURLException me) {
                        log.info("Ignored invalid URL format: " + s);
                    }
                }
            } else {
                log.info("The value for: " + prop.getName() + " is invalid");
            }
        }
    }
    private void setLocationOnDisk(Property prop) {
        if (ServerTags.ADMIN_CONSOLE_LOCATION_ON_DISK.equals(prop.getName())) {
            if (prop.getValue() != null) {
                diskLocation = new File(prop.getValue());
                logFine("Admin Console will be downloaded to: " + diskLocation.getAbsolutePath());
                if (!diskLocation.canWrite()) {
                    log.warning(diskLocation.getAbsolutePath() + " can't be written to, download will fail");
                }
            }
        }
    }
    
    private URL getMyUrl(GrizzlyRequest req) {
        try {
            String host = InetAddress.getLocalHost().getHostName(); //for now.
            URL url     = new URL ("http://" + host + ":" + req.getServerPort() + contextRoot);
            return ( url );
        } catch (MalformedURLException me) {
            throw new RuntimeException(me); //can't really do anything at this point.
        } catch (UnknownHostException ue) {
            throw new RuntimeException(ue); //can't really do anything at this point.
        }
    }
    enum InteractionResult {
        OK,
        CANCEL,
        FIRST_TIMER;
    }
    private synchronized void handleNotInstalledState(GrizzlyRequest req, GrizzlyResponse res) {
        //do this quickly as this is going to block the grizzly worker thread!
        //check for returning user?
        InteractionResult ir  = getUserInteractionResult(req);
        if (ir == InteractionResult.OK) {
            state = AdapterState.INSTALLING;
            startThread();
            sendStatusPage(res);
        } else if (ir == InteractionResult.CANCEL) {
            state = AdapterState.APPLICATION_NOT_INSTALLED; //back to square 1
        } else { //first-timer
            state = AdapterState.APPLICATION_NOT_INSTALLED; //hasn't started yet
            sendConsentPage(req, res);
        }
    }

    private void startThread() {
        File toFile = new File (diskLocation, "admingui.war");
        new InstallerThread(urls, toFile, proxyHost, proxyPort, 
                progress, sysapps, contextRoot).start();
    }
    
    private synchronized InteractionResult getUserInteractionResult(GrizzlyRequest req) {
        String v = visitorId + "";
        if (req.getParameter(VISITOR_PARAM) != null &&
            (v.equals(req.getParameter(VISITOR_PARAM)))) {
            if (req.getParameter(OK_PARAM) != null) {
                proxyHost = req.getParameter(PROXY_HOST_PARAM);
                if (proxyHost != null) {
                    String ps = req.getParameter(PROXY_PORT_PARAM);
                    try {
                        proxyPort = Integer.parseInt(ps);
                    } catch(NumberFormatException nfe) {
                        //ignore
                    }
                }
                return ( InteractionResult.OK );
            }
            else { //canceled
                return (InteractionResult.CANCEL);
            }
        }
        // this is the first-timer
        return InteractionResult.FIRST_TIMER;
    }
    private synchronized void sendConsentPage(GrizzlyRequest req, GrizzlyResponse res) { //should have only one caller
        GrizzlyOutputBuffer ob = res.getOutputBuffer();
        res.setStatus(200);
        res.setContentType("text/html");
        byte[] bytes;
        try {
            try {
                String hp = req.getHeader("Host");
                if (hp == null)
                    hp = this.getMyUrl(req).toString();
                else 
                    hp = "http://" + hp.replace("/", "") + contextRoot;
                visitorId = System.currentTimeMillis(); //sufficiently unique
                bytes = initHtml.replace(MYURL_TOKEN, hp).replace(VISITOR_TOKEN, visitorId+"").getBytes();           
            } catch(Exception e) {
                bytes = ("Catastrophe:" + e.getMessage()).getBytes();
            }
            res.setContentLength(bytes.length);
            ob.write(bytes, 0, bytes.length);
            ob.flush();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void sendStatusPage(GrizzlyResponse res) {
        GrizzlyOutputBuffer ob = res.getOutputBuffer();
        res.setStatus(200);
        res.setContentType("text/html");
        byte[] bytes;
        try {
            String status = "";
            synchronized(progress) {
                status = progress.getMessage();
                if (progress.isDone()) {
                    if (progress.getAdapterState() == AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED) {
                        //thread is done, and application was installed
                        this.state = AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED;
                    } else {
                        this.state = AdapterState.APPLICATION_NOT_INSTALLED;
                    }
                }
            }
            bytes = statusHtml.replace(STATUS_TOKEN, status).getBytes();            
            res.setContentLength(bytes.length);
            ob.write(bytes, 0, bytes.length);
            ob.flush();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }        
    }
    private void handleInstallingState(GrizzlyRequest req, GrizzlyResponse res) { // NOT synchronized
        //communicate with the background thread here ...
        sendStatusPage(res);
    }
    
    private synchronized void handleInstalledButNotLoadedState(GrizzlyRequest req, GrizzlyResponse res) {
        //hook for Jerome
        Application config = getConfig();
        if (config==null) {
            throw new IllegalStateException("handleInstalledButNotLoadedState called with no system app entry");
        }
        habitat.getComponent(ApplicationLoaderService.class).processApplication(config ,null, logger);
    }
    
    private void handleLoadedState() {
        // do nothing
        statusHtml = null;
        initHtml   = null;
    }
    
    private void logFine(String s) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, s);
    }
}
