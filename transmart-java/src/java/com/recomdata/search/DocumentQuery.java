/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/  

package com.recomdata.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentQuery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final static int MAX_HITS = 51200;
    public final static int MAX_CLAUSE_COUNT = 8192;
    private File index;
    public DocumentQuery(String index) {

        this.index = new File(index);		BooleanQuery.setMaxClauseCount(MAX_CLAUSE_COUNT);

    }

    public int searchCount(Map<String, List<String>> searchTerms, Map<String, List<String>> filterTerms) {

        Query query = buildQuery(searchTerms);
        Filter filter = buildFilter(filterTerms);
        IndexReader reader = null;
        Searcher searcher = null;

        try {
            reader = IndexReader.open(index);
            searcher = new IndexSearcher(reader);
            TopDocCollector collector = new TopDocCollector(MAX_HITS);
            if (filter != null) {
                searcher.search(query, filter, collector);
            }
            else {
                searcher.search(query, collector);
            }
            return collector.topDocs().scoreDocs.length;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (searcher != null) {
                    searcher.close();
                }
            }
            catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
                return 0;
            }
        }
    }

    public DocumentHit[] search(Map<String, List<String>> searchTerms, Map<String, List<String>> filterTerms, int max, int offset) {

        Query query = buildQuery(searchTerms);
        Filter filter = buildFilter(filterTerms);

        DocumentHit[] documents = null;
        try {
            IndexReader reader = IndexReader.open(index);
            Searcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            TopDocCollector collector = new TopDocCollector(offset + max);
            if (filter != null) {
                searcher.search(query, filter, collector);
            }
            else {
                searcher.search(query, collector);
            }
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int size = hits.length - offset < max ? hits.length - offset : max;
            documents = new DocumentHit[size];
            for (int i = offset; i < offset + max && i < hits.length; i++) {
                query.rewrite(reader);
                documents[i - offset] = new DocumentHit(searcher.doc(hits[i].doc), hits[i].doc, hits[i].score, query, analyzer);
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return documents;

    }

    private Query buildQuery(Map<String, List<String>> searchTerms) {

        BooleanQuery andQuery = new BooleanQuery();

        for (String key : searchTerms.keySet()) {
            List<String> list = searchTerms.get(key);
            List<Query> queries = new ArrayList<>();
            for (String value : list) {
                if (!value.contains(" ")) {
                    queries.add(new TermQuery(new Term("contents", value.toLowerCase())));
                }
                else {
                    String[] values = value.split(" ");
                    PhraseQuery phraseQuery = new PhraseQuery();
                    for (String v : values) {
                        phraseQuery.add(new Term("contents", v.toLowerCase()));
                    }
                    queries.add(phraseQuery);
                }
            }
            addQueries(andQuery, queries);
        }
        return andQuery;

    }

    private Filter buildFilter(Map<String, List<String>> filterTerms) {

        BooleanQuery andQuery = new BooleanQuery();
		
        if (filterTerms.containsKey("REPOSITORY")) {
            // The repository field is stored as non-analyzed, so matches need to be exact.
            List<String> list = filterTerms.get("REPOSITORY");
            List<Query> queries = new ArrayList<>();
            for (String value : list) {
                queries.add(new TermQuery(new Term("repository", value)));
            }
            addQueries(andQuery, queries);
        }

        if (filterTerms.containsKey("PATH")) {
            // The path field is stored as analyzed, so the search terms also need to be analyzed in order to get a match.
            try {
                List<String> list = filterTerms.get("PATH");
                if (list.size() > 0) {
                    StringReader reader = new StringReader(list.get(0));
                    StandardAnalyzer analyzer = new StandardAnalyzer();
                    TokenStream tokenizer = analyzer.tokenStream("path", reader);
                    PhraseQuery phraseQuery = new PhraseQuery();
                    Token token = new Token();
                    for (token = tokenizer.next(token); token != null; token = tokenizer.next(token)) {
                        phraseQuery.add(new Term("path", token.term()));
                    }
                    andQuery.add(phraseQuery, BooleanClause.Occur.MUST);
                }
            }
            catch (IOException ignored) {
                // do nothing
            }
        }

        if (filterTerms.containsKey("EXTENSION")) {
            List<String> list = filterTerms.get("EXTENSION");
            List<Query> queries = new ArrayList<>();
            for (String value : list) {
                queries.add(new TermQuery(new Term("extension", value.toLowerCase())));
            }
            addQueries(andQuery, queries);
        }

        if (filterTerms.containsKey("NOTEXTENSION")) {
            List<String> list = filterTerms.get("NOTEXTENSION");
            for (String value : list) {
                andQuery.add(
                    new TermQuery(new Term("extension", value.toLowerCase())),
                    BooleanClause.Occur.MUST_NOT);
            }
        }

        if (andQuery.clauses().size() > 0) {
            return new QueryWrapperFilter(andQuery);
        }
        
        return null;
    }
	
    private void addQueries(BooleanQuery andQuery, List<Query> queries) {
        if (queries.size() == 1) {
            andQuery.add(queries.get(0), BooleanClause.Occur.MUST);
        }
        else if (queries.size() > 1) {
            BooleanQuery orQuery = new BooleanQuery();
            for (Query query : queries) {
                orQuery.add(query, BooleanClause.Occur.SHOULD);
            }
            andQuery.add(orQuery, BooleanClause.Occur.MUST);
        }
    }
}
