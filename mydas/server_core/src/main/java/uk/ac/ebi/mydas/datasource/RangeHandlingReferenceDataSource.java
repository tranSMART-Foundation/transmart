package uk.ac.ebi.mydas.datasource;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasRestrictedSequence;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;

import java.util.List;
import java.util.Collection;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 15:12:06
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 *
 * If your datasource implements only the {@link uk.ac.ebi.mydas.datasource.ReferenceDataSource} interface,
 * the MydasServlet will handle restricting the sequenceString returned to
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
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     * @throws uk.ac.ebi.mydas.exceptions.CoordinateErrorException
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
    public Collection<DasAnnotatedSegment> getFeatures(String segmentReference, int start, int stop) throws CoordinateErrorException, BadReferenceObjectException, DataSourceException;
}
