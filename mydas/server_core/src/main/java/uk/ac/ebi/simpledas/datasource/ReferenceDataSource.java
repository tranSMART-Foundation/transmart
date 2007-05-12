package uk.ac.ebi.simpledas.datasource;

import uk.ac.ebi.simpledas.exceptions.DataSourceException;
import uk.ac.ebi.simpledas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.simpledas.model.DasEntryPoint;
import uk.ac.ebi.simpledas.model.DasSequence;

import java.util.Collection;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 15:09:43
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public interface ReferenceDataSource extends AnnotationDataSource{

    /**
     * Extends the ReferenceDataSource inteface to allow the creation of an Annotation
     * data source.  The only significant difference is that a Reference data source can also
     * serve the sequenceString of the requested segment.
     * @param segmentReference being the name of the sequenceString being requested.
     * @return a DasSequence object, holding the sequenceString and start / end coordinates of the sequenceString
     * or null if the reference is not found.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the SimpleDasServlet to return a decent error header to the client.
     */
    public DasSequence getSequence (String segmentReference) throws BadReferenceObjectException, DataSourceException;

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@version attribute.
     *
     * This is a <b>mandatory</b> value so you must ensure that this method does not
     * return null or an empty String. (The SimpleDasServlet will return an error to the
     * client if you do).
     * @return a non-null, non-zero length String, being the version number of the
     * entry points / datasource.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the SimpleDasServlet to return a decent error header to the client.
     */
    public String getEntryPointVersion () throws DataSourceException;

    /**
     * Returns a Collection of DasEntryPoint objects to implement the entry_point command.
     * @return a Collection of DasEntryPoint objects
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the SimpleDasServlet to return a decent error header to the client.
     */
    public Collection<DasEntryPoint> getEntryPoints() throws DataSourceException;
}
