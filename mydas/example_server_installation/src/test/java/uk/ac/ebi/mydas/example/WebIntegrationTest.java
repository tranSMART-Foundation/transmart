package uk.ac.ebi.mydas.example;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Created Using IntelliJ IDEA.
 * Date: 15-May-2007
 * Time: 09:52:21
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Performs a complete test of the mydas API, using the TESTDataSource.
 *
 */
public class WebIntegrationTest extends net.sourceforge.jwebunit.WebTestCase {

    private Server server;

    protected void setUp() throws Exception {
        // Port 0 means "assign arbitrarily port number"
        server = new Server(0);
        server.addHandler(new WebAppContext("src/main/webapp", "/das"));
        server.start();

        // getLocalPort returns the port that was actually assigned
        int actualPort = server.getConnectors()[0].getLocalPort();
        getTestContext().setBaseUrl("http://localhost:" + actualPort + "/das");
    }

    public void test_DSN_request() {
        System.out.println("Testing XML returned for DSN request");
        // Check XML response
        beginAt("/dsn");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASDSN SYSTEM \"http://www.biodas.org/dtd/dasdsn.dtd\">");
        assertTextPresent("<DASDSN>");
        assertTextPresent("<DSN>");
        assertTextPresent("<SOURCE id=\"test\" version=\"0.1\">test</SOURCE>");
        assertTextPresent("<MAPMASTER>http://localhost:8080/das/test</MAPMASTER>");
        assertTextPresent("<DESCRIPTION>Test Data Source</DESCRIPTION>");
        assertTextPresent("</DSN>");
        assertTextPresent("</DASDSN>");
    }

    public void test_entry_points_request(){
        beginAt("/test/entry_points");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASEP SYSTEM \"http://www.biodas.org/dtd/dasep.dtd\">");
        assertTextPresent("<DASEP>");
        assertTextPresent("<ENTRY_POINTS href=\"http://localhost:8080/das/test/entry_points\" version=\"Version 1.1\">");
        assertTextPresent("<SEGMENT id=\"one\" start=\"1\" stop=\"34\" type=\"Protein\" orientation=\"+\">Its a protein!</SEGMENT>");
        assertTextPresent("<SEGMENT id=\"two\" start=\"1\" stop=\"48\" type=\"DNA\" orientation=\"+\">Its a chromosome!</SEGMENT>");
        assertTextPresent("</ENTRY_POINTS>");
        assertTextPresent("</DASEP>");
    }

    public void test_dna_command_simple(){
        beginAt("/test/dna?segment=one");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASDNA SYSTEM \"http://www.biodas.org/dtd/dasdna.dtd\">");
        assertTextPresent("<DASDNA>");
        assertTextPresent("<SEQUENCE id=\"one\" start=\"1\" stop=\"34\" version=\"Up-to-date\">");
        assertTextPresent("<DNA length=\"34\">FFDYASTDFYASDFAUFDYFVSHCVYTDASVCYT</DNA>");
        assertTextPresent("</SEQUENCE>");
        assertTextPresent("</DASDNA>");
    }

    public void test_dna_command_complex(){
        beginAt("/test/dna?segment=one:3,8;segment=two");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASDNA SYSTEM \"http://www.biodas.org/dtd/dasdna.dtd\">");
        assertTextPresent("<DASDNA>");
        assertTextPresent("<SEQUENCE id=\"one\" start=\"3\" stop=\"8\" version=\"Up-to-date\">");
        assertTextPresent("<DNA length=\"6\">DYASTD</DNA>");
        assertTextPresent("</SEQUENCE>");
        assertTextPresent("<SEQUENCE id=\"two\" start=\"1\" stop=\"48\" version=\"Up-to-date\">");
        assertTextPresent("<DNA length=\"48\">cgatcatcagctacgtacgatcagtccgtacgatcgatcagcatcaca</DNA>");
        assertTextPresent("</SEQUENCE>");
        assertTextPresent("</DASDNA>");
    }

    public void test_sequence_command_simple(){
        beginAt("/test/sequence?segment=one");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASSEQUENCE SYSTEM \"http://www.biodas.org/dtd/dassequence.dtd\">");
        assertTextPresent("<DASSEQUENCE>");
        assertTextPresent("<SEQUENCE id=\"one\" start=\"1\" stop=\"34\" moltype=\"Protein\" version=\"Up-to-date\">FFDYASTDFYASDFAUFDYFVSHCVYTDASVCYT</SEQUENCE>");
        assertTextPresent("</DASSEQUENCE>");
    }

    public void test_sequence_command_complex(){
        beginAt("/test/sequence?segment=one:3,8;segment=two");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASSEQUENCE SYSTEM \"http://www.biodas.org/dtd/dassequence.dtd\">");
        assertTextPresent("<DASSEQUENCE>");
        assertTextPresent("<SEQUENCE id=\"one\" start=\"3\" stop=\"8\" moltype=\"Protein\" version=\"Up-to-date\">DYASTD</SEQUENCE>");
        assertTextPresent("<SEQUENCE id=\"two\" start=\"1\" stop=\"48\" moltype=\"DNA\" version=\"Up-to-date\">cgatcatcagctacgtacgatcagtccgtacgatcgatcagcatcaca</SEQUENCE>");
        assertTextPresent("</DASSEQUENCE>");
    }
}
