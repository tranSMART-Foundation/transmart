package uk.ac.ebi.mydas.client.xml;

import junit.framework.TestCase;
import uk.ac.ebi.mydas.client.QueryAwareDasAnnotatedSegment;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 23-May-2008
 * Time: 15:27:35
 */
public class TestDasFeatureXmlUnmarshaller extends TestCase {

    private static final String TEST_FILE_NAME = "features.xml";

    public void testUnmarshaller() {
        // Read in the test xml file ('features.xml' in the test/resources folder).
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(TestDasFeatureXmlUnmarshaller.class.getClassLoader().getResourceAsStream(TEST_FILE_NAME)));
            DasFeatureXmlUnmarshaller unmarshaller = new DasFeatureXmlUnmarshaller();
            Collection<QueryAwareDasAnnotatedSegment> segments = unmarshaller.unMarshall(reader);

            // Assertions about the returned segments
            assertNotNull("The returned segments object should not be null", segments);
            assertEquals("Incorrect number of DasAnnotatedSegment objects reported.", 2, segments.size());

            boolean foundSegmentQ12345 = false;
            boolean foundSegmentA4_HUMAN = false;
            Set<String> featureIdsNotSeen = new HashSet<String>(Arrays.asList(
                    "Q12345", "4932", "Q12345_KEYWORD_Complete proteome",
                    "Q12345_KEYWORD_Direct protein sequencing", "Q12345_KEYWORD_Nucleus",
                    "Q12345_KEYWORD_Transcription", "Q12345_KEYWORD_Transcription regulation",
                    "Q12345_CHAIN_1_250", "pubmed:9169871", "pubmed:12887900", "pubmed:14562095",
                    "pubmed:14562106", "P05067", "9606", "P05067_KEYWORD_3D-structure",
                    "P05067_KEYWORD_Alternative splicing"
            ));
            Set<String> typeIdsNotSeen = new HashSet<String>(Arrays.asList(
                    "description", "dbxref", "KEYWORD", "SO:0000419",
                    "reference"
            ));
            Set<String> methodIdsNotSeen = new HashSet<String>(Arrays.asList(
                    "Standard", "TAX-ID", "UniProt", "journal article"
            ));
            // Iterate over the two segments and check that they are correct.
            for (DasAnnotatedSegment segment : segments) {
                String segmentID = segment.getSegmentId();
                assertTrue("This segment is not recognised.  Its ID is '" + segmentID + "'", "Q12345".equals(segmentID) || "A4_HUMAN".equals(segmentID));
                if ("Q12345".equals(segmentID)) {
                    foundSegmentQ12345 = true;
                    assertEquals("There should be 12 features for Q12345", 12, segment.getFeatures().size());
                    for (DasFeature feature : segment.getFeatures()) {
                        featureIdsNotSeen.remove(feature.getFeatureId());
                        typeIdsNotSeen.remove(feature.getType().getId());
                        methodIdsNotSeen.remove(feature.getMethod().getId());
                    }
                } else if ("A4_HUMAN".equals(segmentID)) {
                    foundSegmentA4_HUMAN = true;
                    assertEquals("There should be 311 features for A4_HUMAN", 311, segment.getFeatures().size());
                    for (DasFeature feature : segment.getFeatures()) {
                        featureIdsNotSeen.remove(feature.getFeatureId());
                        typeIdsNotSeen.remove(feature.getType().getId());
                        methodIdsNotSeen.remove(feature.getMethod().getId());
                    }
                }
            }
            assertTrue("Both Q12345 and A4_HUMAN should have been found in the test XML.", foundSegmentA4_HUMAN && foundSegmentQ12345);
            assertTrue("Not all of the expected featuretIds have been seen: " + featureIdsNotSeen, featureIdsNotSeen.isEmpty());
            assertTrue("Not all of the expected typeIds have been seen: " + typeIdsNotSeen, typeIdsNotSeen.isEmpty());
            assertTrue("Not all of the expected methodIds have been seen: " + methodIdsNotSeen, methodIdsNotSeen.isEmpty());
        }
        catch (DataSourceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
