/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

package uk.ac.ebi.mydas.example;

import net.sourceforge.jwebunit.junit.WebTestCase;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Created Using IntelliJ IDEA.
 * Date: 15-May-2007
 * Time: 09:52:21
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Performs a comprehensive test of the mydas API, using the TESTDataSource
 * which is an implementation of the ReferenceDataSource interface.
 *
 * TODO: Add tests of additional data sources that implement the remaining three available interfaces
 *
 */
public class WebIntegrationTest extends WebTestCase {

    private Server server;

    protected void setUp() throws Exception {
        // Port 0 means "assign arbitrarily port number"
        server = new Server(0);
        server.addHandler(new WebAppContext("./src/main/webapp", "/"));
        server.start();

        // getLocalPort returns the port that was actually assigned
        int actualPort = server.getConnectors()[0].getLocalPort();
        getTestContext().setBaseUrl("http://localhost:" + actualPort + "/das");
    }

    public void testDsnRequest() {
        // Check XML response
        beginAt("/dsn");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASDSN SYSTEM \"http://www.biodas.org/dtd/dasdsn.dtd\">");
        assertTextPresent("<DASDSN>");
        assertTextPresent("<DSN>");
        assertTextPresent("<SOURCE id=\"test\" version=\"test\">test</SOURCE>");
        assertTextPresent("<MAPMASTER>http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle</MAPMASTER>");
        assertTextPresent("<DESCRIPTION>Test Data Source</DESCRIPTION>");
        assertTextPresent("</DSN>");
        assertTextPresent("</DASDSN>");
    }

    /*
     * Changed to test the DAS 1.6 format
     */
    public void test_entry_points_request(){
        beginAt("/test/entry_points");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<DASEP>");
        assertTextPresent("<ENTRY_POINTS href=\"http://localhost:8080/das/test/entry_points\" version=\"Version 1.1\" total=\"2\">");
        assertTextPresent("<SEGMENT id=\"one\" start=\"1\" stop=\"34\" version=\"1\" type=\"Protein\" orientation=\"+\">Its a protein!</SEGMENT>");
        assertTextPresent("<SEGMENT id=\"two\" start=\"1\" stop=\"48\" type=\"DNA\" orientation=\"+\" subparts=\"yes\">Its a chromosome!</SEGMENT>");
        assertTextPresent("</ENTRY_POINTS>");
        assertTextPresent("</DASEP>");
    }

    /*
     * Changed to test the DAS 1.6 format
     */
    public void test_entry_points_request_with_rows(){
        beginAt("/test/entry_points?rows=2-2");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<DASEP>");
        assertTextPresent("<ENTRY_POINTS href=\"http://localhost:8080/das/test/entry_points?rows=2-2\" version=\"Version 1.1\" total=\"2\" start=\"2\" end=\"2\">");
        assertTextNotPresent("<SEGMENT id=\"one\" start=\"1\" stop=\"34\" version=\"1\" type=\"Protein\" orientation=\"+\">Its a protein!</SEGMENT>");
        assertTextPresent("<SEGMENT id=\"two\" start=\"1\" stop=\"48\" type=\"DNA\" orientation=\"+\" subparts=\"yes\">Its a chromosome!</SEGMENT>");
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
        assertTextPresent("<DASSEQUENCE>");
        assertTextPresent("<SEQUENCE id=\"one\" start=\"1\" stop=\"34\" version=\"Up-to-date\">FFDYASTDFYASDFAUFDYFVSHCVYTDASVCYT</SEQUENCE>");
        assertTextPresent("</DASSEQUENCE>");
    }

    public void test_sequence_command_complex(){
        beginAt("/test/sequence?segment=one:3,8;segment=two");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<DASSEQUENCE>");
        assertTextPresent("<SEQUENCE id=\"one\" start=\"3\" stop=\"8\" version=\"Up-to-date\">DYASTD</SEQUENCE>");
        assertTextPresent("<SEQUENCE id=\"two\" start=\"1\" stop=\"48\" version=\"Up-to-date\" label=\"label_two\">cgatcatcagctacgtacgatcagtccgtacgatcgatcagcatcaca</SEQUENCE>");
        assertTextPresent("</DASSEQUENCE>");
    }
    public void test_types_command_filter(){
        beginAt("/test/types?segment=one");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<DASTYPES>");
        assertTextPresent("<SEGMENT id=\"one\" start=\"1\" stop=\"34\" version=\"Up-to-date\" label=\"one_label\">");
        assertTextPresent("<TYPE id=\"oneFeatureTypeIdOne\" cvId=\"CV:00001\" category=\"oneFeatureCategoryOne\">1</TYPE>");
        assertTextPresent("<TYPE id=\"oneFeatureTypeIdTwo\" cvId=\"CV:00002\" category=\"oneFeatureCategoryTwo\">1</TYPE>");
        assertTextPresent("<TYPE id=\"twoFeatureTypeIdOne\" cvId=\"CV:00003\" category=\"twoFeatureCategoryOne\">0</TYPE>");
        assertTextPresent("<TYPE id=\"Chromosome\" category=\"Chromosome\">0</TYPE>");
        assertTextPresent("<TYPE id=\"Contig\" category=\"Contig\">0</TYPE>");
        assertTextPresent("</SEGMENT>");
        assertTextPresent("</DASTYPES>");
    }
    public void test_types_command_all(){
        beginAt("/test/types");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<DASTYPES>");
        assertTextPresent("<SEGMENT version=\"test\" label=\"Complete datasource summary\">");
        assertTextPresent("<TYPE id=\"oneFeatureTypeIdOne\" cvId=\"CV:00001\" category=\"oneFeatureCategoryOne\">1</TYPE>");
        assertTextPresent("<TYPE id=\"oneFeatureTypeIdTwo\" cvId=\"CV:00002\" category=\"oneFeatureCategoryTwo\">1</TYPE>");
        assertTextPresent("<TYPE id=\"twoFeatureTypeIdOne\" cvId=\"CV:00003\" category=\"twoFeatureCategoryOne\">1</TYPE>");
        assertTextPresent("<TYPE id=\"Chromosome\" category=\"Chromosome\">1</TYPE>");
        assertTextPresent("<TYPE id=\"Contig\" category=\"Contig\">3</TYPE>");
        assertTextPresent("</SEGMENT>");
        assertTextPresent("</DASTYPES>");
    }
    public void test_features_command_unknown(){
        beginAt("/test/features?segment=unknown");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASGFF SYSTEM \"http://www.biodas.org/dtd/dasgff.dtd\">");
        assertTextPresent("<ERRORSEGMENT id=\"unknown\" />");
    }
    public void test_features_command_one(){
        beginAt("/test/features?segment=one");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<!DOCTYPE DASGFF SYSTEM \"http://www.biodas.org/dtd/dasgff.dtd\">");
        assertTextPresent("<SEGMENT id=\"one\" start=\"1\" stop=\"34\" version=\"Up-to-date\" label=\"one_label\">");
        assertTextPresent("<FEATURE id=\"oneFeatureIdOne\" label=\"one Feature Label One\">");
        assertTextPresent("<TYPE id=\"oneFeatureTypeIdOne\" category=\"oneFeatureCategoryOne\">one Feature DasType Label One</TYPE>");
        assertTextPresent("<METHOD id=\"CV:00001\">one Feature Method Label One</METHOD>");
        assertTextPresent("START>5</START>");
        assertTextPresent("<END>10</END>");
        assertTextPresent("<SCORE>123.45</SCORE>");
        assertTextPresent("<ORIENTATION>0</ORIENTATION>");
        assertTextPresent("<PHASE>-</PHASE>");
        assertTextPresent("<NOTE>This is a note relating to feature one of segment one.</NOTE>");
        assertTextPresent("<LINK href=\"http://code.google.com/p/mydas/\">mydas project home page.</LINK>");
        assertTextPresent("<TARGET id=\"oneTargetId\" start=\"20\" stop=\"30\">oneTargetName</TARGET>");
        assertTextPresent("<GROUP id=\"oneGroupId\" label=\"one Group Label\" type=\"onegrouptype\">");
        assertTextPresent("<NOTE>A note on the group for reference one.</NOTE>");
        assertTextPresent("<LINK href=\"http://code.google.com/p/mydas/\">mydas project home page.</LINK>");
        assertTextPresent("<TARGET id=\"oneTargetId\" start=\"20\" stop=\"30\">oneTargetName</TARGET>");
        assertTextPresent("</GROUP>");
        assertTextPresent("</FEATURE>");
        assertTextPresent("<FEATURE id=\"oneFeatureIdTwo\" label=\"one Feature Label Two\">");
        assertTextPresent("</SEGMENT>");
    }
    
    
    public void testSourceRequest() {
        // Check XML response
        beginAt("/source");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<SOURCES>");
        assertTextPresent("<SOURCE uri=\"test\" doc_href=\"http://www.ebi.ac.uk/~pjones\" title=\"test\" description=\"Test Data Source\">");
        assertTextPresent("<MAINTAINER email=\"test@ebi.ac.uk\" />");
        assertTextPresent("<VERSION uri=\"test\" created=\"2010-03-01\">");
        assertTextPresent("<COORDINATES uri=\"http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle\" source=\"Protein Sequence\" authority=\"UniProt\" test_range=\"P00280\">UniProt,Protein Sequence</COORDINATES>");
        assertTextPresent("<CAPABILITY type=\"das1:features\" query_uri=\"http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/features\" />");
        assertTextPresent("</VERSION>");
        assertTextPresent("</SOURCE>");
        assertTextPresent("</SOURCES>");
    }

}
