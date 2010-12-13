package uk.ac.ebi.mydas.model;

import uk.ac.ebi.mydas.controller.SegmentQuery;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class ErrorSegment extends DasAnnotatedSegment {
	private static final long serialVersionUID = 1L;
	private SegmentQuery segmentQuery;
	public ErrorSegment(String segmentId,Integer startCoordinate, Integer stopCoordinate,
			 String version,String label) throws DataSourceException {
		super(segmentId, startCoordinate, stopCoordinate, version, label, null);
	}
	public ErrorSegment(SegmentQuery segmentQuery) throws DataSourceException{
		this(segmentQuery.getSegmentId(),segmentQuery.getStartCoordinate(), segmentQuery.getStopCoordinate(), "error", "error");
		this.segmentQuery=segmentQuery;
	}
	public SegmentQuery getSegmentQuery(){
		return segmentQuery;
	}
}
