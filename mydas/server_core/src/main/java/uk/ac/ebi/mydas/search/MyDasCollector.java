package uk.ac.ebi.mydas.search;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.ScoreDoc;

/**
 * A {@link Collector} implementation that collects the top-scoring hits,
 * returning them as a {@link TopDocs}. This is used by {@link IndexSearcher} to
 * implement {@link TopDocs}-based search. Hits are sorted by score descending
 * and then (when the scores are tied) docID ascending. When you create an
 * instance of this collector you should know in advance whether documents are
 * going to be collected in doc Id order or not.
 *
 * <p><b>NOTE</b>: The values {@link Float#NaN} and
 * {Float#NEGATIVE_INFINITY} are not valid scores.  This
 * collector will not properly collect hits with such
 * scores.
 */
public abstract class MyDasCollector extends TopDocsCollector<ScoreDoc> {

	private static Map<String,Integer> hitsPerSegment;
	private static IndexSearcher searcher;
	// Assumes docs are scored in order.
	private static class InOrderTopScoreDocCollector extends MyDasCollector {
		private InOrderTopScoreDocCollector(int numHits) {
			super(numHits);
		}

		@Override
		public void collect(int doc) throws IOException {
			float score = scorer.score();

			// This collector cannot handle these scores:
			assert score != Float.NEGATIVE_INFINITY;
			assert !Float.isNaN(score);

			totalHits++;
			if (score <= pqTop.score) {
				// Since docs are returned in-order (i.e., increasing doc Id), a document
				// with equal score to pqTop.score cannot compete since HitQueue favors
				// documents with lower doc Ids. Therefore reject those docs too.
				return;
			}
			pqTop.doc = doc + docBase;
			pqTop.score = score;
			pqTop = pq.updateTop();
			String segmentId=searcher.doc(doc).get("segmentId");
			Integer current=hitsPerSegment.get(segmentId);
			if(null==current)
				hitsPerSegment.put(segmentId, 1);
			else
				hitsPerSegment.put(segmentId, current+1);
		}

		@Override
		public boolean acceptsDocsOutOfOrder() {
			return false;
		}
	}

	// Assumes docs are scored out of order.
	private static class OutOfOrderTopScoreDocCollector extends MyDasCollector {
		private OutOfOrderTopScoreDocCollector(int numHits) {
			super(numHits);
		}

		@Override
		public void collect(int doc) throws IOException {
			float score = scorer.score();

			// This collector cannot handle NaN
			assert !Float.isNaN(score);

			totalHits++;
			doc += docBase;
			if (score < pqTop.score || (score == pqTop.score && doc > pqTop.doc)) {
				return;
			}
			pqTop.doc = doc;
			pqTop.score = score;
			pqTop = pq.updateTop();
			String segmentId=searcher.doc(doc).get("segmentId");
			Integer current=hitsPerSegment.get(segmentId);
			if(null==current)
				hitsPerSegment.put(segmentId, 1);
			else
				hitsPerSegment.put(segmentId, current+1);
		}

		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}
	}

	/**
	 * Creates a new {@link TopScoreDocCollector} given the number of hits to
	 * collect and whether documents are scored in order by the input
	 * {@link Scorer} to {@link #setScorer(Scorer)}.
	 *
	 * <p><b>NOTE</b>: The instances returned by this method
	 * pre-allocate a full array of length
	 * <code>numHits</code>, and fill the array with sentinel
	 * objects.
	 * @param searcher 
	 */
	public static MyDasCollector create(int numHits, boolean docsScoredInOrder, IndexSearcher indexSearcher) {
		hitsPerSegment = new HashMap<String,Integer>();
		searcher=indexSearcher;
		if (docsScoredInOrder) {
			return new InOrderTopScoreDocCollector(numHits);
		} else {
			return new OutOfOrderTopScoreDocCollector(numHits);
		}

	}

	ScoreDoc pqTop;
	int docBase = 0;
	Scorer scorer;

	// prevents instantiation
	private MyDasCollector(int numHits) {
		super(new HitQueue(numHits, true));
		// HitQueue implements getSentinelObject to return a ScoreDoc, so we know
		// that at this point top() is already initialized.
		pqTop = pq.top();
	}

	@Override
	protected TopDocs newTopDocs(ScoreDoc[] results, int start) {
		if (results == null) {
			return EMPTY_TOPDOCS;
		}

		// We need to compute maxScore in order to set it in TopDocs. If start == 0,
		// it means the largest element is already in results, use its score as
		// maxScore. Otherwise pop everything else, until the largest element is
		// extracted and use its score as maxScore.
		float maxScore = Float.NaN;
		if (start == 0) {
			maxScore = results[0].score;
		} else {
			for (int i = pq.size(); i > 1; i--) { pq.pop(); }
			maxScore = pq.pop().score;
		}

		return new TopDocs(totalHits, results, maxScore);
	}

	@Override
	public void setNextReader(IndexReader reader, int base) {
		docBase = base;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}
	
	public Integer getSizePerSegment(String segmentId){
		return hitsPerSegment.get(segmentId);
	}
	
}
