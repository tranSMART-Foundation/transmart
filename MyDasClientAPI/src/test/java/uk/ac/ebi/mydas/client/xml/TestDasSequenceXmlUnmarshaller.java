package uk.ac.ebi.mydas.client.xml;

import junit.framework.TestCase;
import uk.ac.ebi.mydas.client.QueryAwareDasSequence;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 23-May-2008
 * Time: 15:27:35
 */
public class TestDasSequenceXmlUnmarshaller extends TestCase {

    private static final String TEST_FILE_NAME = "sequence.xml";

    public void testSequenceUnmarshaller() {
        // Read in the test xml file ('features.xml' in the test/resources folder).
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(TestDasFeatureXmlUnmarshaller.class.getClassLoader().getResourceAsStream(TEST_FILE_NAME)));
            DasSequenceXmlUnmarshaller unmarshaller = new DasSequenceXmlUnmarshaller();
            Collection<QueryAwareDasSequence> segments = unmarshaller.unMarshall(reader);

            // Assertions about the returned segments
            assertNotNull("The returned segments object should not be null", segments);
            assertEquals("Incorrect number of DasAnnotatedSegment objects reported.", 1, segments.size());

            QueryAwareDasSequence sequence = segments.iterator().next();
            assertNotNull("The sequence object in the collection should not be null", sequence);
            assertEquals("Incorrect sequence", "MGDVEKGKKIFIMKCSQCHTVEKGGKHKTGPNLHGLFGRKTGQAPGYSYTAANKNKGIIWGEDTLMEYLENPKKYIPGTKMIFVGIKKKEERADLIAYLKKATNE", sequence.getSequenceString());
            assertEquals("Incorrect version", "2bdff12130aec0c85a14a8e32f97d502", sequence.getVersion());
            assertEquals("Incorrect start", new Integer(1), sequence.getStartCoordinate());
            assertEquals("Incorrect stop", new Integer(105), sequence.getStopCoordinate());
            assertEquals("Incorrect label", "A4_HUMAN", sequence.getLabel());
            assertEquals("Incorrect accession", "P99999", sequence.getSegmentId());
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
                    e.printStackTrace();
                }
            }
        }
    }
}