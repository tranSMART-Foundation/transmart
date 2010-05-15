package uk.ac.ebi.mydas.proxy;

import uk.ac.ebi.mydas.client.QueryAwareDasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 26-Jun-2008
 * Time: 17:12:13
 * To test the AbstractProxyDataSource - this simple implementation just reports all of the features, one after another
 * in an unintelligent way - should not be used for production purposes really!
 */
public class ReportAllProxyDasServer extends AbstractProxyDataSource {
    /**
     * This method must be implemented by a concrete subclass that will determine how the data source merges (or not!)
     * features from different data sources.
     * <p/>
     * This naive implementation just takes the first DasAnnotatedSegment and uses it as the basis for coalesced
     * DasAnnotatedSegment object, adding the features from all of the other DasAnnotatedSegments to it.
     *
     * @param annotatedSegments being all of the DasAnnotatedSegments that contribute to the final result
     * @return a single DasAnnotatedSegment comprising all of the features returned from multiple DAS sources.
     */
    public QueryAwareDasAnnotatedSegment coalesceDasAnnotatedSegments(Collection<QueryAwareDasAnnotatedSegment> annotatedSegments) {
        Iterator<QueryAwareDasAnnotatedSegment> segmentIterator = annotatedSegments.iterator();
        QueryAwareDasAnnotatedSegment coalesced = segmentIterator.next();
        while (segmentIterator.hasNext()) {
            DasAnnotatedSegment current = segmentIterator.next();
            for (DasFeature feature : current.getFeatures()) {
                coalesced.getFeatures().add(feature);
            }
        }
        return coalesced;
    }
}
