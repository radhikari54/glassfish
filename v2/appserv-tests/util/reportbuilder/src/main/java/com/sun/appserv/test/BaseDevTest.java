package com.sun.appserv.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

import javax.xml.xpath.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class BaseDevTest {
    public final SimpleReporterAdapter stat;
    public PrintWriter writer;
    public static final boolean DEBUG = true;

    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
    public BaseDevTest() {
        if (DEBUG) {
            try {
                writer = new PrintWriter(new FileWriter("test.out", true), true);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        stat = new SimpleReporterAdapter("appserv-tests", getTestName());
        stat.addDescription(getTestDescription());
    }

    protected abstract String getTestName();

    protected abstract String getTestDescription();

    public void report(String step, boolean success) {
        stat.addStatus(step, success ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
    }

    /**
     * Runs the command with the args given
     *
     * @param args
     *
     * @return true if successful
     */
    public boolean asadmin(final String... args) {
        String asadmincmd = isWindows() ? "/bin/asadmin.bat" : "/bin/asadmin";
        List<String> command = new ArrayList<String>();
        command.add(System.getenv().get("S1AS_HOME") + asadmincmd);
        command.addAll(Arrays.asList(antProp("as.props").split(" ")));
        command.addAll(Arrays.asList(args));
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = null;
        boolean success = false;
        try {
            process = builder.start();
            InputStream inStream = process.getInputStream();
            InputStream errStream = process.getErrorStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            try {
                final byte[] buf = new byte[1000];
                int read;
                while ((read = inStream.read(buf)) != -1) {
                    out.write(buf, 0, read);
                }
                while ((read = errStream.read(buf)) != -1) {
                    err.write(buf, 0, read);
                }
            } finally {
                errStream.close();
                inStream.close();
            }
            String outString = new String(out.toByteArray()).trim();
            String errString = new String(err.toByteArray()).trim();
            write(outString);
            write(errString);
            process.waitFor();
            success = process.exitValue() == 0 && !outString.contains(String.format("Command %s failed.", args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (process != null) {
                process.destroy();
            }
            writer.close();
        }
        return success;
    }

    /**
     * Checks if the os is windows
     * @return  true if the os is win
     */
    public boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    public String antProp(final String key) {
        String value = System.getProperty(key);
        if (value == null) {
            try {
                Properties props = new Properties();
                String apsHome = System.getenv("APS_HOME");
                FileReader reader = new FileReader(new File(apsHome, "config.properties"));
                try {
                    props.load(reader);
                } finally {
                    reader.close();

                }
                System.getProperties().putAll(props);
                System.setProperty("as.props", String.format("--user %s --passwordfile %s --host %s --port %s"
                    + " --echo=true --terse=true", antProp("admin.user"), antProp("admin.password.file"),
                    antProp("admin.host"), antProp("admin.port")));
                value = System.getProperty(key);
                int index = -1;
                while((index = value.indexOf("${env.")) != -1) {
                    int end = value.indexOf("}", index);
                    String var = value.substring(index, end + 1);
                    final String name = var.substring(6, var.length() - 1);
                    value = value.replace(var, System.getenv(name));
                    System.setProperty(key, value);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return value;
    }

    public void write(final String out) {
        if (DEBUG) {
            System.out.println(out);
        }
        writer.println(out);
        writer.flush();
    }

    /**
     * Evaluates the Xpath expression
     * @param expr  The expression to evaluate
     * @param f  The file to parse
     * @param ret  The return type of the expression  can be
     *
     * XPathConstants.NODESET
     * XPathConstants.BOOLEAN
     * XPathConstants.NUMBER
     * XPathConstants.STRING
     * XPathConstants.NODE

     * @return  the object after evaluation can be of type
     * number maps to a java.lang.Double
     * string maps to a java.lang.String
     * boolean maps to a java.lang.Boolean
     * node-set maps to an org.w3c.dom.NodeList

     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Object evalXPath(String expr, File f, QName ret)  {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(f);
            if (DEBUG) {
                System.out.println("Parsing"+ f.getAbsolutePath());
            }
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression xexpr = xpath.compile(expr);
            Object result = xexpr.evaluate(doc, ret);
            System.out.println("Evaluating"+ f.getAbsolutePath());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        

    }

     /**
     *  Evaluates the Xpath expression by parsing the DAS's domain.xml
     * @param expr  The Xpath expression to evaluate
     * @return the object after evaluation can be of type
     * number maps to a java.lang.Double
     * string maps to a java.lang.String
     * boolean maps to a java.lang.Boolean
     * node-set maps to an org.w3c.dom.NodeList
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Object evalXPath(String expr,QName ret)  {
        return evalXPath(expr, getDASDomainXML(),ret);

    }

    /**
     * Gets the domains folder for DAS
     * @return GF_HOME/domains/domain1
     */
    public File getDASDomainDir(){
        return new File(new File(getGlassFishHome(),"domains"),"domain1");
    }

    /**
     * Gets the domain.xml for DAS
     * @return GF_HOME/domains/domain1/config/domain.xml
     */
    public File getDASDomainXML(){
        return new File(new File(getDASDomainDir(),"config"),"domain.xml");
    }

    /**
     * Get the Glassfish home from the environment variable S1AS_HOME
     * @return
     */
    public File getGlassFishHome() {
        File glassFishHome;
        String home = System.getenv("S1AS_HOME");

        if(home == null)
            throw new IllegalStateException("No S1AS_HOME set!");

        File f = new File(home);

        try {
            f = f.getCanonicalFile();
        }
        catch(Exception e) {
            f = f.getAbsoluteFile();
        }
        glassFishHome = f;

        if(!glassFishHome.isDirectory())
            throw new IllegalStateException("S1AS_HOME is not poiting at a real directory!");
        return glassFishHome;

    }

    /**
     * Implementations can override this method to do the cleanup
     * for eg deleting instances, deleting clusters etc
     */
    public void cleanup() {
        
    }

}
