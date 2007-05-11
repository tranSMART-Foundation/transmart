package uk.ac.ebi.simpledas.datasource;

import uk.ac.ebi.simpledas.exceptions.SegmentNotFoundException;
import uk.ac.ebi.simpledas.exceptions.DataSourceException;
import uk.ac.ebi.simpledas.model.DasSequence;
import uk.ac.ebi.simpledas.model.DasFeature;

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
 * the SimpleDasServlet will handle restricting the sequence returned to
 * the start / stop coordinates in the request and you will only need to
 * implement the <code>getSequence (String segmentReference) : String</code>
 * method.
 *
 * If you also implement this interface, this will allow you to take control
 * of filtering by start / stop coordinates in your ReferenceDataSource.
 */
public interface RangeHandlingReferenceDataSource extends ReferenceDataSource{

    /**
     *
     * If you also implement this interface, this will allow you to take control
     * of filtering the sequence returned by start / stop coordinates in your ReferenceDataSource.
     * @param segmentReference
     * @param start
     * @param stop
     * @return
     * @throws uk.ac.ebi.simpledas.exceptions.SegmentNotFoundException
     * @throws uk.ac.ebi.simpledas.exceptions.DataSourceException
     */
    public DasSequence getSequence (String segmentReference, int start, int stop) throws SegmentNotFoundException, DataSourceException;

    /**
     * Implement this method to allow you to take control
     * of filtering by start / stop coordinates in your AnnotationDataSource.
     *
     * Useful if your DAS source includes massive segments.
     * @param segmentReference
     * @param start
     * @param stop
     * @return
     * @throws SegmentNotFoundException
     * @throws DataSourceException
     */
    public List<DasFeature> getFeatures(String segmentReference, int start, int stop) throws SegmentNotFoundException, DataSourceException;
}
