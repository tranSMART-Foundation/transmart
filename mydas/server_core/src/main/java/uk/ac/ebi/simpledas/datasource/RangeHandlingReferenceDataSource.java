package uk.ac.ebi.simpledas.datasource;

import uk.ac.ebi.simpledas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.simpledas.exceptions.DataSourceException;
import uk.ac.ebi.simpledas.exceptions.CoordinateErrorException;
import uk.ac.ebi.simpledas.model.DasSequence;
import uk.ac.ebi.simpledas.model.DasFeature;
import uk.ac.ebi.simpledas.model.DasRestrictedSequence;

import java.util.List;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 15:12:06
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 *
 * If your datasource implements only the {@link uk.ac.ebi.simpledas.datasource.ReferenceDataSource} interface,
 * the SimpleDasServlet will handle restricting the sequenceString returned to
 * the start / stop coordinates in the request and you will only need to
 * implement the <code>getSequenceString (String segmentReference) : String</code>
 * method.
 *
 * If you also implement this interface, this will allow you to take control
 * of filtering by start / stop coordinates in your ReferenceDataSource.
 */
public interface RangeHandlingReferenceDataSource extends ReferenceDataSource{

    /**
     * TODO Detailed documentation required here.
     * If you also implement this interface, this will allow you to take control
     * of filtering the sequenceString returned by start / stop coordinates in your ReferenceDataSource.
     * @param segmentReference
     * @param start
     * @param stop
     * @return a DasSequence object for the reference, or null if it is not found.
     * @throws uk.ac.ebi.simpledas.exceptions.BadReferenceObjectException
     * @throws uk.ac.ebi.simpledas.exceptions.DataSourceException
     * @throws uk.ac.ebi.simpledas.exceptions.CoordinateErrorException
     */
    public DasRestrictedSequence getSequence (String segmentReference, int start, int stop) throws CoordinateErrorException, BadReferenceObjectException, DataSourceException;

    /**
     * Implement this method to allow you to take control
     * of filtering by start / stop coordinates in your AnnotationDataSource.
     *
     * Useful if your DAS source includes massive segments.
     * @param segmentReference
     * @param start
     * @param stop
     * @return
     * @throws BadReferenceObjectException
     * @throws DataSourceException
     */
    public List<DasFeature> getFeatures(String segmentReference, int start, int stop) throws BadReferenceObjectException, DataSourceException;
}
