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
import org.codehaus.groovy.grails.commons.ApplicationHolder
/**
 * @short Listener for new revisions and models for indexing
 * 
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class UpdatedRepositoryListener implements ApplicationListener {

	IndexWriter indexWriter
	def modelDelegateService
	def grailsApplication = ApplicationHolder.application
	
	public UpdatedRepositoryListener() {
		File location=new File(grailsApplication.config.jummp.search.index)
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
		String content=modelDelegateService.getSearchIndexingContent(revision)
		Document doc = new Document();
		
		/*
		*	Indexed fields
		*/
		Field nameField =
			new Field("name",name,Field.Store.YES,Field.Index.ANALYZED);
		Field descriptionField = 
			new Field("description",description,Field.Store.NO,Field.Index.ANALYZED); 
		Field formatField = 
			new Field("format",""+revision.format.name,Field.Store.YES,Field.Index.NOT_ANALYZED);
		Field contentField = 
			new Field("content",content,Field.Store.NO,Field.Index.ANALYZED); 
		
		doc.add(nameField);
		doc.add(descriptionField);
		doc.add(formatField)
		doc.add(contentField)
			
			
		/*
		*	Stored fields. Hopefully will be used to display the search results one day
		*	instead of going to the database for each model. When we find a solution to needing to
		*	look in the database to figure out if the user has access to a model. 
		*/
		Field idField = 
			new Field("model_id",""+revision.model.id,Field.Store.YES,Field.Index.NO); 
		Field versionField = 
			new Field("versionNumber",""+revision.revisionNumber,Field.Store.YES,Field.Index.NO);
		Field submittedField = 
			new Field("submissionDate",""+revision.model.submissionDate,Field.Store.YES,Field.Index.NO);
		Field submitterField = 
			new Field("submitter",""+revision.model.submitter,Field.Store.YES,Field.Index.NOT_ANALYZED);
		doc.add(idField);
		doc.add(versionField);
		doc.add(submittedField)
		doc.add(submitterField)
		
		indexWriter.addDocument(doc);
		indexWriter.commit();
	}
	
	public SearcherManager getSearcherManager() {
		boolean applyAllDeletes = true;
		SearcherManager mgr = new SearcherManager(indexWriter, true, new SearcherFactory());
                return mgr
	}
	
	
}
