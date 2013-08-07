package uk.ac.ebi.mydas.search;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.ServerConfiguration;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.SearcherException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasTarget;

public class Indexer {
	private String dirPath;
	private ServerConfiguration config;
	private static final Logger logger = Logger.getLogger(Indexer.class);

	public Indexer(String dirPath, ServerConfiguration config){
		this.dirPath = dirPath;
		this.config=config;
	}

	public void generateIndexes() throws SearcherException{
		List<String> dsns = config.getDsnNames();
		if (dsns == null || dsns.size() == 0){
			logger.error("No DSNs");
			throw new SearcherException("No datasources to query");
		} else{
			// At least one dsn is OK.
			for (String dsn : dsns){
				DataSourceConfiguration dsnConfig = config.getDataSourceConfig(dsn);
                if (dsnConfig.isMatchedDynamic()) {
                    continue;
                }
				String capabilities =dsnConfig.getCapabilities();
				if(capabilities.contains("advanced-search")){
					if(capabilities.contains("entry_points") && capabilities.contains("feature-by-id")){
						try {
							if (dsnConfig.getDataSource() instanceof AnnotationDataSource){
								
								// Fine - process command.
								AnnotationDataSource refDsn = dsnConfig.getDataSource();
								FSDirectory dir = FSDirectory.open(new File(dirPath+"/"+dsn));
								IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30),true, IndexWriter.MaxFieldLength.LIMITED);
								
								Integer max =dsnConfig.getMaxEntryPoints();
								int total = refDsn.getTotalEntryPoints();
								if (max ==null)
									max=total;
								for (int i=0;i<=total;i+=max){
									Collection<DasEntryPoint> entryPoints = refDsn.getEntryPoints(i+1, i+max);
									if (entryPoints==null)
										throw  new SearcherException("Entry points is null,The entry-point capability is not well implemented.");
									Collection <DasEntryPoint> ignored = new ArrayList<DasEntryPoint>();
									for (DasEntryPoint entryPoint:entryPoints){
										try {
											this.processEntryPoint(entryPoint,refDsn,writer);
										} catch (BadReferenceObjectException e) {
											ignored.add(entryPoint);
										}
									}
									for (DasEntryPoint entryPoint:ignored){
										try {
											this.processEntryPoint(entryPoint,refDsn,writer);
										} catch (BadReferenceObjectException e) {
											logger.error("The entry point was ignored:"+entryPoint.getSegmentId());
										}
									}
								}
								writer.optimize();
								writer.close();
							}
						} catch (DataSourceException e) {
							throw new SearcherException("Error trying to query information of a data source",e);
						} catch (UnimplementedFeatureException e) {
							throw new SearcherException("The Entry-Point capability is a requirenment for the searching functions",e);
						} catch (IOException e) {
							throw new SearcherException("Error trying to write the index file ",e);
//						} catch (BadReferenceObjectException e) {
//							throw new SearcherException("A reported entry point can't be recovered",e);
						}
					}else{
						throw new SearcherException("The capabilities 'entry-points' and 'feature-by-id' are required to be able to index");
					}
				}
			}
		}
	}

	private void processEntryPoint(DasEntryPoint entryPoint, AnnotationDataSource refDsn, IndexWriter writer) throws BadReferenceObjectException, DataSourceException, CorruptIndexException, IOException {

		DasAnnotatedSegment segment;
			segment = refDsn.getFeatures(entryPoint.getSegmentId(), null);
		for (DasFeature feature:segment.getFeatures()){
			Document doc = new Document();
			String type="",method="",notes="",links="",targets="",parents="",parts="",segmentS="";

			doc.add(new Field("segmentId", segment.getSegmentId(),																Field.Store.YES,	Field.Index.ANALYZED));
			segmentS +=segment.getSegmentId();
			if (segment.getSegmentLabel()!=null) {
				doc.add(new Field("segmentLabel", segment.getSegmentLabel(),																Field.Store.YES,	Field.Index.ANALYZED));
				segmentS += " "+segment.getSegmentLabel();
			}
			if (segment.getVersion()!=null) {
				doc.add(new Field("segmentVersion", segment.getVersion(),																Field.Store.YES,	Field.Index.ANALYZED));
				segmentS += " "+segment.getVersion();
			}
			if (segment.getStartCoordinate()!=null) {
				doc.add(new Field("segmentStart", ""+segment.getStartCoordinate(),																Field.Store.YES,	Field.Index.ANALYZED));
				segmentS += " "+segment.getStartCoordinate();
			}
			if (segment.getStopCoordinate()!=null) {
				doc.add(new Field("segmentStop", ""+segment.getStopCoordinate(),																Field.Store.YES,	Field.Index.ANALYZED));
				segmentS += " "+segment.getStopCoordinate();
			}

			
			doc.add(new Field("featureId", feature.getFeatureId(),		Field.Store.YES,	Field.Index.ANALYZED));
			if (feature.getFeatureLabel()!=null) doc.add(new Field("featureLabel", feature.getFeatureLabel(), Field.Store.YES, Field.Index.ANALYZED));
			if (feature.getType()!=null){
				if (feature.getType().getId()!=null){ 
					doc.add(new Field("typeId", feature.getType().getId(), Field.Store.YES, Field.Index.ANALYZED));
					type +=feature.getType().getId()+" ";
				}
				if (feature.getType().getCvId()!=null){
					doc.add(new Field("typeCvId", feature.getType().getCvId(), Field.Store.YES, Field.Index.ANALYZED));
					type +=feature.getType().getCvId()+" ";
				}
				if (feature.getType().getLabel()!=null){ 
					doc.add(new Field("typeLabel", feature.getType().getLabel(), Field.Store.YES, Field.Index.ANALYZED));
					type +=feature.getType().getLabel()+" ";
				}
				if (feature.getType().getCategory()!=null){
					doc.add(new Field("typeCategory", feature.getType().getCategory(), Field.Store.YES, Field.Index.ANALYZED));
					type +=feature.getType().getCategory()+" ";
				}
				doc.add(new Field("type",type, Field.Store.NO, Field.Index.ANALYZED));
			}
			if (feature.getMethod()!=null){
				method+=feature.getMethod().getId()+" ";
				doc.add(new Field("methodId", feature.getMethod().getId(), Field.Store.YES, Field.Index.ANALYZED));
				if (feature.getMethod().getCvId()!=null){
					method+=feature.getMethod().getCvId()+" ";
					doc.add(new Field("methodCvId", feature.getMethod().getCvId(), Field.Store.YES, Field.Index.ANALYZED));
				}
				if (feature.getMethod().getLabel()!=null){
					method+=feature.getMethod().getLabel()+" ";
					doc.add(new Field("methodLabel", feature.getMethod().getLabel(), Field.Store.YES, Field.Index.ANALYZED));
				}
				doc.add(new Field("method",method, Field.Store.NO, Field.Index.ANALYZED));
			}
			doc.add(new Field("start",""+feature.getStartCoordinate(), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("stop",""+feature.getStopCoordinate(), Field.Store.YES, Field.Index.ANALYZED));

			if (feature.getScore()!=null) doc.add(new Field("score",""+feature.getScore(), Field.Store.YES, Field.Index.ANALYZED));
			if (feature.getOrientation()!=null) doc.add(new Field("orientation",""+feature.getOrientation(), Field.Store.YES, Field.Index.ANALYZED));
			if (feature.getPhase()!=null) doc.add(new Field("phase",""+feature.getPhase(), Field.Store.YES, Field.Index.ANALYZED));
			if (feature.getNotes()!=null) {
				String sep ="";
				for (String note:feature.getNotes()){
					notes+=sep+note;
					sep =" ==NOTE== ";
				}
				doc.add(new Field("notes",notes, Field.Store.YES, Field.Index.ANALYZED));
			}
			if (feature.getLinks()!=null) {
				String sep ="";
				for (URL key:feature.getLinks().keySet()){
					links+=sep+feature.getLinks().get(key) +" _-_ "+ key;
					sep =" ==LINK== ";
				}
				doc.add(new Field("links",links, Field.Store.YES, Field.Index.ANALYZED));
			}
			if (feature.getTargets()!=null) {
				String sep="";
				for (DasTarget target:feature.getTargets()){
					targets += sep+target.getTargetId();
					targets += " _-_ "+target.getStartCoordinate();
					targets += " _-_ "+target.getStopCoordinate();
					if (target.getTargetName()!=null )targets += " _-_ "+target.getTargetName();
					sep=" ==TARGET== ";
				}
				doc.add(new Field("targets",targets, Field.Store.YES, Field.Index.ANALYZED));
			}
			if (feature.getParents()!=null) {
				String sep="";
				for(String parent:feature.getParents()){
					parents+=sep+parent;
					sep=" ==PARENT== ";
				}
				doc.add(new Field("parents",parents, Field.Store.YES, Field.Index.ANALYZED));
			}
			if (feature.getParts()!=null) {
				String sep="";
				for(String part:feature.getParts()){
					parts+=sep+part;
					sep=" ==PART== ";
				}
				doc.add(new Field("parts",parts, Field.Store.YES, Field.Index.ANALYZED));
			}
			doc.add(new Field("all",segmentS+" "+feature.getFeatureId()+" "+type+" "+method+" "+notes+" "+links+" "+targets+" "+parents+" "+parts, Field.Store.NO, Field.Index.ANALYZED));
			writer.addDocument(doc);
		}		
	}
}
