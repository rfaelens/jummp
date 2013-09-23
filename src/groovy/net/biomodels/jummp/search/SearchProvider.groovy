package net.biomodels.jummp.search

import net.biomodels.jummp.core.events.RevisionCreatedEvent
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.queryParser.MultiFieldQueryParser
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.document.Document

//import org.apache.lucene.search.SearcherManager LUCENE 4.4

/**
 * @short Class written as a spring-bean singleton to execute search queries using lucene
 * 
 * The class maintains a searchermanager linked to an indexwriter maintained by
 * UpdatedRepositoryListener bean. 
 *
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class SearchProvider {

	
	def grailsApplication
	/**
	* Internal method to executes a query 
	*
	* Gets the searchermanager if necessary from the indexwriter, 
	* executes the supplied @param query,
	* filters out duplicates (as we index revisions and return models),
	*
	* @param query The query to be executed
	* @return A set of documents corresponding to search results
	**/
	private Set<Document> search(Query q) {
		Set<Document> docs=new HashSet<Document>()
		IndexSearcher indexSearcher = new IndexSearcher(
				grailsApplication.mainContext.getBean("indexingEventListener").getDirectory());
		/*if (!mgr) { 4.4
			mgr=grailsApplication.mainContext.getBean("indexingEventListener").getSearcherManager()
		}
		IndexSearcher indexSearcher = mgr.acquire();*/
		try {
			TopDocs topDocs = indexSearcher.search(q,1000); //make less arbitrary
			for (int i=0; i<topDocs.totalHits; i++) {
				docs.add(indexSearcher.doc(topDocs.scoreDocs[i].doc))
			}
			
		} finally {
			/*mgr.release(indexSearcher); 4.4
			indexSearcher = null; */
			indexSearcher.close()
		}
		return docs
	}
	
	
	/**
	* Executes a search on the specified fields 
	*
	* Performs search on the @param query, using the supplied @param fields
	*
	* @param query The query to be executed
	* @param fields The ids of the fields in the documents
	* @return A set of documents corresponding to search results
	**/
	public Set<Document> performSearch(String[] fields, String query) {
		QueryParser queryParser=new MultiFieldQueryParser(fields, new StandardAnalyzer())		
		return search(queryParser.parse(query))
	}
	
	/**
	* Executes a search on a single field
	*
	* Performs search on the @param query, using the supplied @param field
	*
	* @param query The query to be executed
	* @param field The ids of the field in the documents
	* @return A set of documents corresponding to search results
	**/
	public Set<Document> performSearch(String field, String query) {
		QueryParser queryParser = new QueryParser(new StandardAnalyzer());
		return search(queryParser.parse(query))
	}
	
	/** LUCENE 4.4
	* Gets the searcher to update its view of the index, if necessary
	*
	public void refreshIndex() {
		if (mgr) {
			mgr.maybeRefresh()
		}
	}
	*/

}
