package uk.ac.ebi.mydas.extendedmodel;

import java.util.Collection;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasSequence;

@SuppressWarnings("serial")
public class DasUnknownFeatureSegment extends DasAnnotatedSegment {

	public DasUnknownFeatureSegment(String segmentId, Integer startCoordinate,
			Integer stopCoordinate, String version, String segmentLabel,
			Collection<DasFeature> features) throws DataSourceException {
		super(segmentId, startCoordinate, stopCoordinate, version,
				segmentLabel, features);
		// TODO Auto-generated constructor stub
	}

	public DasUnknownFeatureSegment(DasSequence sequence, String segmentLabel,
			Collection<DasFeature> features) throws DataSourceException {
		super(sequence, segmentLabel, features);
		// TODO Auto-generated constructor stub
	}
	public DasUnknownFeatureSegment(String segmentId) throws DataSourceException{
		super(segmentId, 0, 0, "_",null, null);
	}

}
