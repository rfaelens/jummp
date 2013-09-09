package net.biomodels.jummp.search
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import net.biomodels.jummp.core.events.RevisionCreatedEvent
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.events.ModelCreatedEvent
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.Directory
import org.apache.lucene.index.IndexDeletionPolicy
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper
import grails.util.Environment
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.SearcherFactory
import org.apache.lucene.search.IndexSearcher

/**
 * @short Listener for new revisions and models for indexing
 * 
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class UpdatedRepositoryListener implements ApplicationListener {

	IndexWriter indexWriter
	def modelDelegateService
	def grailsApplication
	
	public UpdatedRepositoryListener() {
		File location=new File("/home/raza/reps/search")
		location.mkdirs()
		System.out.println("USING ${location} for directory!")
		//Create instance of Directory where index files will be stored
		Directory fsDirectory =  FSDirectory.open(location);
		/* Create instance of analyzer, which will be used to tokenize
		the input data */
		Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_44);
		//Create the instance of deletion policy
		IndexDeletionPolicy deletionPolicy = new KeepOnlyLastCommitDeletionPolicy(); 
		
		IndexWriterConfig conf=new IndexWriterConfig(Version.LUCENE_44,standardAnalyzer)
		conf.setIndexDeletionPolicy(deletionPolicy)
		conf.setOpenMode(OpenMode.CREATE_OR_APPEND)
		indexWriter =new IndexWriter(fsDirectory,conf);
	}
	
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof RevisionCreatedEvent) {
			updateIndex((event as RevisionCreatedEvent).revision)
		}
		if (event instanceof ModelCreatedEvent) {
			updateIndex(modelDelegateService.
						getLatestRevision((event as ModelCreatedEvent).model.id))
													
		}
	}
	
	public void updateIndex(RevisionTransportCommand revision) {
		
		String name = revision.name
		String description = revision.description


		Field nameField =
			new Field("name",name,Field.Store.YES,Field.Index.NOT_ANALYZED);
		Field descriptionField = 
			new Field("description",description,Field.Store.NO,Field.Index.ANALYZED); 
		Field idField = 
			new Field("id",""+revision.id,Field.Store.YES,Field.Index.NO); 

		Document doc = new Document();
		// Add these fields to a Lucene Document
		doc.add(idField);
		doc.add(nameField);
		doc.add(descriptionField);

		//Step 3: Add this document to Lucene Index.
		indexWriter.addDocument(doc);
		indexWriter.commit();
	}
	
	public SearcherManager getSearcherManager() {
		boolean applyAllDeletes = true;
		SearcherManager mgr = new SearcherManager(indexWriter, true, new SearcherFactory());
                return mgr
	}
	
	
}
