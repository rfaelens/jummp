package net.biomodels.jummp.search

import net.biomodels.jummp.core.events.RevisionCreatedEvent
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.Query
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.TopDocs
import org.apache.lucene.util.Version
/**
 * @short Listener for new revisions and models for indexing
 * 
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class SearchProvider {

	
	def grailsApplication
	
	SearcherManager mgr
	
	public void performSearch(String field, String query) {
		if (!mgr) {
			mgr=grailsApplication.mainContext.getBean("indexingEventListener").getSearcherManager()
		}
		System.out.println("Got search query: "+query)
		IndexSearcher indexSearcher = mgr.acquire();
		System.out.println("index size: "+indexSearcher.getIndexReader().numDocs())
		System.out.println("isSearcherCurrent: "+mgr.isSearcherCurrent())
		try {
			QueryParser queryParser = new QueryParser(Version.LUCENE_44,field,new StandardAnalyzer(Version.LUCENE_44));
			Query termQuery = queryParser.parse(query);
			TopDocs topDocs = indexSearcher.search(termQuery,10);
			System.out.println(topDocs.scoreDocs.getProperties())
			System.out.println("GOT: ${topDocs.totalHits}")
			for (int i=0; i<topDocs.totalHits; i++) {
				System.out.println(indexSearcher.doc(topDocs.scoreDocs[i].doc).inspect())
			}
			
		} finally {
			mgr.release(indexSearcher);
			indexSearcher = null;
		}
	}
	
	public void refreshIndex() {
		if (mgr) {
			mgr.maybeRefresh()
		}
	}

}
