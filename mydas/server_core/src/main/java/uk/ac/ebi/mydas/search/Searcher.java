package uk.ac.ebi.mydas.search;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import uk.ac.ebi.mydas.exceptions.SearcherException;


public class Searcher {
	private String dirPath, dataSourceName;
	
	public Searcher(String dirPath, String dataSourceName){
		this.dirPath = dirPath;
		this.dataSourceName = dataSourceName;
	}
	
	public String[] search(String query) throws SearcherException{
		try {
			query = URLDecoder.decode(query,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SearcherException("Error trying to URLdecode the query",e);
		}
		
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

		Directory fsDir=null;
		try {
			fsDir = FSDirectory.open(new File(dirPath+"/"+dataSourceName));
		} catch (IOException e) {
			throw new SearcherException("Error trying to open the index file",e);
		}
		IndexSearcher searcher=null;
		try {
			searcher = new IndexSearcher(fsDir);
		} catch (CorruptIndexException e) {
			throw new SearcherException("The index file is corrupt",e);
		} catch (IOException e) {
			throw new SearcherException("Error trying to open the index file.",e);
		}


		Query q=null;
		try {
			q = new QueryParser(Version.LUCENE_30, "title", analyzer).parse(query);
		} catch (ParseException e) {
			throw new SearcherException("Error parsing the query.",e);
		}

		int hitsPerPage = 100;
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		try {
			searcher.search(q, collector);
		} catch (IOException e) {
			throw new SearcherException("Error in I/O operations while searching.",e);
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		String[] fids=new String[hits.length];
		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			Document d=null;
			try {
				d = searcher.doc(docId);
			} catch (CorruptIndexException e) {
				throw new SearcherException("Error recovering one of the result docs.",e);
			} catch (IOException e) {
				throw new SearcherException("I/O Error while recovering one of the result docs.",e);
			}
			fids[i]=d.get("featureId");
		}

		try {
			searcher.close();
		} catch (IOException e) {
			throw new SearcherException("Error closing the searcher.",e);
		}

		return fids;
	}
}
