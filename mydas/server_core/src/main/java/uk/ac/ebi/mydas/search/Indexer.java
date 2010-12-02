package uk.ac.ebi.mydas.search;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
				DataSourceConfiguration dsnConfig = config.getDataSourceConfigMap().get(dsn);
				if(dsnConfig.getCapabilities().contains("advanced-search")){
					try {
						if (dsnConfig.getDataSource() instanceof AnnotationDataSource){
							// Fine - process command.
							AnnotationDataSource refDsn = dsnConfig.getDataSource();
							FSDirectory dir = FSDirectory.open(new File(dirPath+"/"+dsn));
							IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30),true, IndexWriter.MaxFieldLength.LIMITED);

							Collection<DasEntryPoint> entryPoints = refDsn.getEntryPoints(1, refDsn.getTotalEntryPoints());
							for (DasEntryPoint entryPoint:entryPoints){
								DasAnnotatedSegment segment = refDsn.getFeatures(entryPoint.getSegmentId(), null);
								for (DasFeature feature:segment.getFeatures()){
									Document doc = new Document();
									String type="",method="",notes="",links="",targets="",parents="",parts="";
									doc.add(new Field("featureId", feature.getFeatureId(),																Field.Store.YES,	Field.Index.ANALYZED));
									if (feature.getFeatureLabel()!=null) doc.add(new Field("featureLabel", feature.getFeatureLabel(),					Field.Store.NO,		Field.Index.ANALYZED));
									if (feature.getType()!=null){
										if (feature.getType().getId()!=null){ 
											doc.add(new Field("typeId", feature.getType().getId(), Field.Store.NO, Field.Index.ANALYZED));
											type +=feature.getType().getId()+" ";
										}
										if (feature.getType().getCvId()!=null){
											doc.add(new Field("typeCvId", feature.getType().getCvId(), Field.Store.NO, Field.Index.ANALYZED));
											type +=feature.getType().getCvId()+" ";
										}
										if (feature.getType().getLabel()!=null){ 
											doc.add(new Field("typeLabel", feature.getType().getLabel(), Field.Store.NO, Field.Index.ANALYZED));
											type +=feature.getType().getLabel()+" ";
										}
										if (feature.getType().getCategory()!=null){
											doc.add(new Field("typeCategory", feature.getType().getCategory(), Field.Store.NO, Field.Index.ANALYZED));
											type +=feature.getType().getCategory()+" ";
										}
										doc.add(new Field("type",type, Field.Store.NO, Field.Index.ANALYZED));
									}
									if (feature.getMethod()!=null){
										method+=feature.getMethod().getId()+" ";
										doc.add(new Field("methodId", feature.getMethod().getId(), Field.Store.NO, Field.Index.ANALYZED));
										if (feature.getMethod().getCvId()!=null){
											method+=feature.getMethod().getCvId()+" ";
											doc.add(new Field("methodCvId", feature.getMethod().getCvId(), Field.Store.NO, Field.Index.ANALYZED));
										}
										if (feature.getMethod().getLabel()!=null){
											method+=feature.getMethod().getLabel()+" ";
											doc.add(new Field("methodLabel", feature.getMethod().getLabel(), Field.Store.NO, Field.Index.ANALYZED));
										}
										doc.add(new Field("method",method, Field.Store.NO, Field.Index.ANALYZED));
									}
									doc.add(new Field("start",""+feature.getStartCoordinate(), Field.Store.NO, Field.Index.ANALYZED));
									doc.add(new Field("stop",""+feature.getStopCoordinate(), Field.Store.NO, Field.Index.ANALYZED));
									
									if (feature.getScore()!=null) doc.add(new Field("score",""+feature.getScore(), Field.Store.NO, Field.Index.ANALYZED));
									if (feature.getOrientation()!=null) doc.add(new Field("orientation",""+feature.getOrientation(), Field.Store.NO, Field.Index.ANALYZED));
									if (feature.getPhase()!=null) doc.add(new Field("phase",""+feature.getPhase(), Field.Store.NO, Field.Index.ANALYZED));
									if (feature.getNotes()!=null) {
										for (String note:feature.getNotes())
											notes+=note+" ";
										doc.add(new Field("note",notes, Field.Store.NO, Field.Index.ANALYZED));
									}
									if (feature.getLinks()!=null) {
										for (URL key:feature.getLinks().keySet())
											links+=feature.getLinks().get(key) +" "+ key+" ";
										doc.add(new Field("link",links, Field.Store.NO, Field.Index.ANALYZED));
									}
									if (feature.getTargets()!=null) {
										for (DasTarget target:feature.getTargets()){
											targets += target.getTargetId();
											targets += " "+target.getStartCoordinate();
											targets += " "+target.getStopCoordinate();
											if (target.getTargetName()!=null )targets += " "+target.getTargetName();
										}
										doc.add(new Field("target",targets, Field.Store.NO, Field.Index.ANALYZED));
									}
									if (feature.getParents()!=null) {
										for(String parent:feature.getParents())
											parents+=parent+" ";
										doc.add(new Field("parent",parents, Field.Store.NO, Field.Index.ANALYZED));
									}
									if (feature.getParts()!=null) {
										for(String part:feature.getParts())
											parts+=part+" ";
										doc.add(new Field("part",parts, Field.Store.NO, Field.Index.ANALYZED));
									}
									doc.add(new Field("all",feature.getFeatureId()+" "+type+" "+method+" "+notes+" "+links+" "+targets+" "+parents+" "+parts, Field.Store.NO, Field.Index.ANALYZED));
									writer.addDocument(doc);
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
					} catch (BadReferenceObjectException e) {
						throw new SearcherException("A reported entry point can't be recevered",e);
					}
				}
			}
		}
	}
}
