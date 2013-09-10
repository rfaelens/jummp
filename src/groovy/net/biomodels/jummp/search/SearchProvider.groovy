package net.biomodels.jummp.search

import net.biomodels.jummp.core.events.RevisionCreatedEvent
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.Query
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.TopDocs
import org.apache.lucene.util.Version
import org.apache.lucene.document.Document
/**
 * @short Listener for new revisions and models for indexing
 * 
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class SearchProvider {

	
	def grailsApplication
	
	SearcherManager mgr
	
	public Set<Document> performSearch(String field, String query) {
		Set<Document> docs=new HashSet<Document>()
		if (!mgr) {
			mgr=grailsApplication.mainContext.getBean("indexingEventListener").getSearcherManager()
		}
		System.out.println("Got search query: "+query)
		IndexSearcher indexSearcher = mgr.acquire();
		try {
			QueryParser queryParser = new QueryParser(Version.LUCENE_44,field,new StandardAnalyzer(Version.LUCENE_44));
			Query termQuery = queryParser.parse(query);
			TopDocs topDocs = indexSearcher.search(termQuery,10);
			for (int i=0; i<topDocs.totalHits; i++) {
				docs.add(indexSearcher.doc(topDocs.scoreDocs[i].doc))
			}
			
		} finally {
			mgr.release(indexSearcher);
			indexSearcher = null;
		}
		return docs
	}
	
	public void refreshIndex() {
		if (mgr) {
			mgr.maybeRefresh()
		}
	}

}
