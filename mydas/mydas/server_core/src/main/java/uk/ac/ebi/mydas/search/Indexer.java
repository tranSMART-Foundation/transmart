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
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
								FSDirectory dir = FSDirectory.open(new File(dirPath+"/"+dsn).toPath());
								IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig());
								
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

			doc.add(new StringField("segmentId", segment.getSegmentId(), Field.Store.YES));
			segmentS +=segment.getSegmentId();
			if (segment.getSegmentLabel()!=null) {
				doc.add(new StringField("segmentLabel", segment.getSegmentLabel(), Field.Store.YES));
				segmentS += " "+segment.getSegmentLabel();
			}
			if (segment.getVersion()!=null) {
				doc.add(new StringField("segmentVersion", segment.getVersion(), Field.Store.YES));
				segmentS += " "+segment.getVersion();
			}
			if (segment.getStartCoordinate()!=null) {
                                doc.add(new StringField("segmentStart", ""+segment.getStartCoordinate(), Field.Store.YES)); // as a String
//				doc.add(new IntPoint("segmentStart", ""+segment.getStartCoordinate()));
//				doc.add(new StoredField("segmentStart", ""+segment.getStartCoordinate()));
				segmentS += " "+segment.getStartCoordinate();
			}
			if (segment.getStopCoordinate()!=null) {
                                doc.add(new StringField("segmentStop", ""+segment.getStopCoordinate(), Field.Store.YES)); // as a String
//				doc.add(new IntPoint("segmentStop", ""+segment.getStopCoordinate()));
//				doc.add(new StoredField("segmentStop", ""+segment.getStopCoordinate()));
				segmentS += " "+segment.getStopCoordinate();
			}

			
			doc.add(new StringField("featureId", feature.getFeatureId(), Field.Store.YES));
			if (feature.getFeatureLabel()!=null) doc.add(new StringField("featureLabel", feature.getFeatureLabel(), Field.Store.YES));
			if (feature.getType()!=null){
				if (feature.getType().getId()!=null){ 
					doc.add(new StringField("typeId", feature.getType().getId(), Field.Store.YES));
					type +=feature.getType().getId()+" ";
				}
				if (feature.getType().getCvId()!=null){
					doc.add(new StringField("typeCvId", feature.getType().getCvId(), Field.Store.YES));
					type +=feature.getType().getCvId()+" ";
				}
				if (feature.getType().getLabel()!=null){ 
					doc.add(new StringField("typeLabel", feature.getType().getLabel(), Field.Store.YES));
					type +=feature.getType().getLabel()+" ";
				}
				if (feature.getType().getCategory()!=null){
					doc.add(new StringField("typeCategory", feature.getType().getCategory(), Field.Store.YES));
					type +=feature.getType().getCategory()+" ";
				}
				doc.add(new StringField("type",type, Field.Store.NO));
			}
			if (feature.getMethod()!=null){
				method+=feature.getMethod().getId()+" ";
				doc.add(new StringField("methodId", feature.getMethod().getId(), Field.Store.YES));
				if (feature.getMethod().getCvId()!=null){
					method+=feature.getMethod().getCvId()+" ";
					doc.add(new StringField("methodCvId", feature.getMethod().getCvId(), Field.Store.YES));
				}
				if (feature.getMethod().getLabel()!=null){
					method+=feature.getMethod().getLabel()+" ";
					doc.add(new StringField("methodLabel", feature.getMethod().getLabel(), Field.Store.YES));
				}
				doc.add(new StringField("method",method, Field.Store.NO));
			}
			doc.add(new StringField("start",""+feature.getStartCoordinate(), Field.Store.YES));
			doc.add(new StringField("stop",""+feature.getStopCoordinate(), Field.Store.YES));

			if (feature.getScore()!=null) doc.add(new StringField("score",""+feature.getScore(), Field.Store.YES));
			if (feature.getOrientation()!=null) doc.add(new StringField("orientation",""+feature.getOrientation(), Field.Store.YES));
			if (feature.getPhase()!=null) doc.add(new StringField("phase",""+feature.getPhase(), Field.Store.YES));
			if (feature.getNotes()!=null) {
				String sep ="";
				for (String note:feature.getNotes()){
					notes+=sep+note;
					sep =" ==NOTE== ";
				}
				doc.add(new StringField("notes",notes, Field.Store.YES));
			}
			if (feature.getLinks()!=null) {
				String sep ="";
				for (URL key:feature.getLinks().keySet()){
					links+=sep+feature.getLinks().get(key) +" _-_ "+ key;
					sep =" ==LINK== ";
				}
				doc.add(new StringField("links",links, Field.Store.YES));
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
				doc.add(new StringField("targets",targets, Field.Store.YES));
			}
			if (feature.getParents()!=null) {
				String sep="";
				for(String parent:feature.getParents()){
					parents+=sep+parent;
					sep=" ==PARENT== ";
				}
				doc.add(new StringField("parents",parents, Field.Store.YES));
			}
			if (feature.getParts()!=null) {
				String sep="";
				for(String part:feature.getParts()){
					parts+=sep+part;
					sep=" ==PART== ";
				}
				doc.add(new StringField("parts",parts, Field.Store.YES));
			}
			doc.add(new StringField("all",segmentS+" "+feature.getFeatureId()+" "+type+" "+method+" "+notes+" "+links+" "+targets+" "+parents+" "+parts, Field.Store.NO));
			writer.addDocument(doc);
		}		
	}
}
