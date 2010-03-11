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
        assertTextPresent("<ERRORSEGMENT id=\"unknown\" />");
    }
    public void test_features_command_one(){
        beginAt("/test/features?segment=one");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<SEGMENT id=\"one\" start=\"1\" stop=\"34\" version=\"Up-to-date\" label=\"one_label\">");
        assertTextPresent("<FEATURE id=\"oneFeatureIdOne\" label=\"one Feature Label One\">");
        assertTextPresent("<TYPE id=\"oneFeatureTypeIdOne\" cvId=\"CV:00001\" category=\"oneFeatureCategoryOne\">one Feature DasType Label One</TYPE>");
        assertTextPresent("<METHOD id=\"oneFeatureMethodIdOne\" cvId=\"ECO:12345\">one Feature Method Label One</METHOD>");
        assertTextPresent("START>5</START>");
        assertTextPresent("<END>10</END>");
        assertTextPresent("<SCORE>123.45</SCORE>");
        assertTextNotPresent("<ORIENTATION>0</ORIENTATION>");
        assertTextNotPresent("<PHASE>-</PHASE>");
        assertTextPresent("<NOTE>This is a note relating to feature one of segment one.</NOTE>");
        assertTextPresent("<LINK href=\"http://code.google.com/p/mydas/\">mydas project home page.</LINK>");
        assertTextPresent("<TARGET id=\"oneTargetId\" start=\"20\" stop=\"30\">oneTargetName</TARGET>");
        assertTextPresent("</FEATURE>");
        assertTextPresent("<FEATURE id=\"oneFeatureIdTwo\" label=\"one Feature Label Two\">");
        assertTextPresent("</SEGMENT>");
    }
    public void test_features_command_one_maxbins(){
        beginAt("/test/features?segment=one;maxbins=1");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<SEGMENT id=\"one\" start=\"1\" stop=\"34\" version=\"Up-to-date\" label=\"one_label\">");
        assertTextPresent("<FEATURE id=\"oneFeatureIdOne\" label=\"one Feature Label One\">");
        assertTextPresent("<TYPE id=\"oneFeatureTypeIdOne\" cvId=\"CV:00001\" category=\"oneFeatureCategoryOne\">one Feature DasType Label One</TYPE>");
        assertTextPresent("<METHOD id=\"oneFeatureMethodIdOne\" cvId=\"ECO:12345\">one Feature Method Label One</METHOD>");
        assertTextPresent("START>5</START>");
        assertTextPresent("<END>10</END>");
        assertTextPresent("<SCORE>123.45</SCORE>");
        assertTextNotPresent("<ORIENTATION>0</ORIENTATION>");
        assertTextNotPresent("<PHASE>-</PHASE>");
        assertTextPresent("<NOTE>This is a note relating to feature one of segment one.</NOTE>");
        assertTextPresent("<LINK href=\"http://code.google.com/p/mydas/\">mydas project home page.</LINK>");
        assertTextPresent("<TARGET id=\"oneTargetId\" start=\"20\" stop=\"30\">oneTargetName</TARGET>");
        assertTextPresent("</FEATURE>");
        assertTextNotPresent("<FEATURE id=\"oneFeatureIdTwo\" label=\"one Feature Label Two\">");
        assertTextPresent("</SEGMENT>");
    }
    public void test_features_command_two_parts(){
        beginAt("/test/features?segment=two;category=component");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<SEGMENT id=\"two\" start=\"1\" stop=\"1000\" version=\"Up-to-date\" label=\"two_label\">");
        assertTextPresent("<FEATURE id=\"two\" label=\"two\">");
        assertTextPresent("<TYPE id=\"ThisSegment\" reference=\"yes\" superparts=\"yes\" subparts=\"yes\" category=\"component\" />");
        assertTextPresent("<METHOD id=\"assembly\" />");
        assertTextPresent("<START>1</START>");
        assertTextPresent("<END>1000</END>");
        assertTextPresent("<TARGET id=\"two\" start=\"1\" stop=\"1000\">two_label</TARGET>");
        assertTextPresent("<PARENT id=\"ParentChromosome\" />");
        assertTextPresent("<PART id=\"Contig:A\" />");
        assertTextPresent("<PART id=\"Contig:B\" />");
        assertTextPresent("<PART id=\"Contig:C\" />");
        assertTextPresent("</FEATURE>");
        assertTextPresent("<FEATURE id=\"Contig:A\" label=\"Contig:A\">");
        assertTextPresent("<TYPE id=\"contig\" cvId=\"SO:0000149\" reference=\"yes\" superparts=\"yes\" subparts=\"no\" category=\"component\" />");
        assertTextPresent("<END>200</END>");
        assertTextPresent("<ORIENTATION>+</ORIENTATION>");
        assertTextPresent("<PHASE>0</PHASE>");
        assertTextPresent("<NOTE>This is a sub-component.</NOTE>");
        assertTextPresent("<TARGET id=\"Contig-A\" start=\"1\" stop=\"200\">Contig A</TARGET>");
        assertTextPresent("<PARENT id=\"two\" />");
        assertTextPresent("<FEATURE id=\"Contig:B\" label=\"Contig:B\">");
        assertTextPresent("<START>400</START>");
        assertTextPresent("<END>1000</END>");
        assertTextPresent("<NOTE>This is a sub-component with different coordinate system.</NOTE>");
        assertTextPresent("<TARGET id=\"Contig-B\" start=\"20\" stop=\"620\">Contig B</TARGET>");
        assertTextPresent("<PARENT id=\"two\" />");
        assertTextPresent("<FEATURE id=\"Contig:C\" label=\"Contig:C\">");
        assertTextPresent("<TYPE id=\"contig\" cvId=\"SO:0000149\" reference=\"yes\" superparts=\"yes\" subparts=\"yes\" category=\"component\" />");
        assertTextPresent("<START>200</START>");
        assertTextPresent("<END>400</END>");
        assertTextPresent("<TARGET id=\"Contig-C\" start=\"80\" stop=\"280\">Contig C</TARGET>");
        assertTextPresent("<PARENT id=\"two\" />");
        assertTextPresent("<PART id=\"Contig:C.1\" />");
//        assertTextPresent("<FEATURE id=\"Contig:C.1\" label=\"Contig:C.1\">");
//        assertTextPresent("<START>200</START>");
//        assertTextPresent("<END>400</END>");
//        assertTextPresent("<TARGET id=\"Contig-C.1\" start=\"80\" stop=\"280\">Contig C.1</TARGET>");
//        assertTextPresent("<PARENT id=\"Contig:C\" />");
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

    public void testStylesheet() {
        beginAt("/test/stylesheet");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"yes\"?>");
        assertTextPresent("<DASSTYLE>");
        assertTextPresent("<STYLESHEET>");
        assertTextPresent("<CATEGORY id=\"default\">");
    	
    }
    
    public void testStructure() {
        beginAt("/test/structure?query=2ii9");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<dasstructure>");
        assertTextPresent("<object dbAccessionId=\"2ii9\" objectVersion=\"20-MAR-07\" dbSource=\"PDB\" dbVersion=\"20070116\" dbCoordSys=\"PDBresnum,Protein Structure\" />");
        assertTextPresent("<chain id=\"A\">");
        assertTextPresent("<group name=\"SER\" type=\"amino\" groupID=\"1\">");
        assertTextPresent("<atom atomID=\"1\" atomName=\"N\" x=\"44.18\" y=\"5.327\" z=\"31.168\" />");
        assertTextPresent("<atom atomID=\"2\" atomName=\"CA\" x=\"43.672\" y=\"5.068\" z=\"29.781\" />");
        assertTextPresent("<atom atomID=\"3\" atomName=\"C\" x=\"42.728\" y=\"6.217\" z=\"29.365\" />");
        assertTextPresent("<atom atomID=\"4\" atomName=\"O\" x=\"42.328\" y=\"7.024\" z=\"30.23\" />");
        assertTextPresent("<atom atomID=\"5\" atomName=\"CB\" x=\"42.965\" y=\"3.707\" z=\"29.74\" />");
        assertTextPresent("<atom atomID=\"6\" atomName=\"OG\" x=\"42.754\" y=\"3.284\" z=\"28.41\" />");
        assertTextPresent("</group>");
        assertTextPresent("</chain>");
        assertTextPresent("</dasstructure>");
    }
    public void testAlignment() {
        beginAt("/test/alignment?query=PF03344");
        assertTextPresent("<?xml version=\"1.0\" standalone=\"no\"?>");
        assertTextPresent("<dasalignment>");
        assertTextPresent("<alignment alignType=\"Pfam Full Alignment\" name=\"PF03344\" position=\"1\" max=\"84\">");
        assertTextPresent("<alignObject dbAccessionId=\"PF03344\" objectVersion=\"93d32837b9b401f3bac6ef3d21f9193c\" intObjectId=\"A2V6V1\" type=\"PROTEIN\" dbSource=\"Pfam\" dbVersion=\"24.0\" dbCoordSys=\"UniProt\">");
        assertTextPresent("<sequence>TPSSVEMDISSSRKQSEEPFTTVLENGAGMVSSTSFNGGVSPHNWGDSGPPCKKSRKEKKQTGSGPLGNSYVERQRSVHEK</sequence>");
        assertTextPresent("</alignObject>");
        assertTextPresent("<alignObject dbAccessionId=\"PF03344\" objectVersion=\"28a2bfce7453560927cd37b695907c5e\" intObjectId=\"Q4R3H3\" type=\"PROTEIN\" dbSource=\"Pfam\" dbVersion=\"24.0\" dbCoordSys=\"UniProt\">");
        assertTextPresent("<sequence>MAQDAFRDVGIRLQERRHLDLIYNFGCHLTDDYRPGIDPALSDPVLARRLRENRSLAMSRLDEVISKYAMLQDKSEEGERKKRRARLQGTSSHSEDTPASLDSGEGPSGMASQGCPSASKAETDDEEDEESDEEEEEEEDEEEEEEEEEEATDSEEEEDLEQMQEGQEDDEEEEEEEEAAGKDGDGSPMSSPQISTEKNLEPGKQISRSSGEQQNKVSPLLLSEEPLAPSSIDAESNGEQPEELTLEEESPVSQLFELEIEALPLDTPSSVEMDISSSRKQSEEPFTTVLENGAGMVSSTSFNGGVSPHNWGDSGPPCKKSRKEKKQTGSGPLGNSYVERQRSVHEKNGKKICTLPSPPSPLASLAPVADSSTRVDSPSHGLVTSSLCIPSPAQLSQTPQSQPPRPSTYKTSVATQCDPEEIIVLSDSD</sequence>");
        assertTextPresent("<block blockOrder=\"1\">");
        assertTextPresent("<segment intObjectId=\"A2V6V1\" start=\"1\" end=\"81\">");
        assertTextPresent("<cigar>78I4M11DI23D6I8DI38D2IDI3DI7DI8D4I28DI16D3I4D3I5DIDI7D9I11D2I4D67I10D12I11D3ID3I45D28I26DI13D25I115DI5DI4DIDI4DI7D6I15D2I12D3I3DI5D4I8D22I19D2I5D5I6D7IDIDI5D3I7D37I5D2I4DI7D2I2D2IDI10D5I5D7I4D3I6DI8DI17D7I3M2I9MI14MI29M10I13M6I3M6DI20D6I12D9I11D3I20D6M167I</cigar>");
        assertTextPresent("</segment>");
        assertTextPresent("<segment intObjectId=\"Q4R3H3\" start=\"1\" end=\"429\">");
        assertTextPresent("<cigar>82I11DI23D6I8DI38D2IDI3DI7DI8D4I28DI16D3I4D3I5DIDI7D9I11D2I4D67I10D12I11D3ID3I45D28I26DI13D25I42D73MI5MI4MIMI4MID6M6ID14MI13M3I3MI5M4IM2D5M22I19M2I5M5I6M7IMIMI5M3I16M18I15M2I12M2I2M2IMI4M3D3M5I5M7I4M3I6MI8MI20MI6M2I9MI14MI29M10I49M6I55M173I</cigar>");
        assertTextPresent("</block>");
        assertTextPresent("</alignment>");
        assertTextPresent("</dasalignment>");
    }
}
