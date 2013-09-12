package net.biomodels.jummp.search

import net.biomodels.jummp.core.events.RevisionCreatedEvent
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
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
	
	private Set<Document> search(Query q) {
		Set<Document> docs=new HashSet<Document>()
		if (!mgr) {
			mgr=grailsApplication.mainContext.getBean("indexingEventListener").getSearcherManager()
		}
		IndexSearcher indexSearcher = mgr.acquire();
		try {
			TopDocs topDocs = indexSearcher.search(q,1000);
			for (int i=0; i<topDocs.totalHits; i++) {
				docs.add(indexSearcher.doc(topDocs.scoreDocs[i].doc))
			}
			
		} finally {
			mgr.release(indexSearcher);
			indexSearcher = null;
		}
		return docs
		
	}
	
	public Set<Document> performSearch(String[] fields, String query) {
		QueryParser queryParser=new MultiFieldQueryParser(Version.LUCENE_44, fields, new StandardAnalyzer(Version.LUCENE_44))		
		return search(queryParser.parse(query))
	}
	
	public Set<Document> performSearch(String field, String query) {
		QueryParser queryParser = new QueryParser(Version.LUCENE_44,field,new StandardAnalyzer(Version.LUCENE_44));
		return search(queryParser.parse(query))
	}
	
	public void refreshIndex() {
		if (mgr) {
			mgr.maybeRefresh()
		}
	}

}
