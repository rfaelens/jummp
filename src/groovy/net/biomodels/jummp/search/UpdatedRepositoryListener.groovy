/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Lucene, Spring Framework, Grails (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Lucene, Spring Framework, Grails used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.search
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import net.biomodels.jummp.core.events.RevisionCreatedEvent
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.events.ModelCreatedEvent
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.Directory
import org.apache.lucene.index.IndexWriter
import org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper
import grails.util.Environment
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.Analyzer
import grails.util.Holders
import grails.util.Environment

/* Lucene 4.4 imports
import org.apache.lucene.index.IndexDeletionPolicy
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.SearcherFactory
import org.apache.lucene.search.IndexSearcher
*/

/**
 * @short Listener for new revisions and models for indexing
 *
 * This class is meant to be executed as a (singleton) spring bean
 * which listens for events generated by the model service (new models/new revisions).
 * An indexwriter is kept open (maintaining an exclusive lock on the search index)
 * and new models/revisions are indexed as they arrive. The class is also used to
 * get a searchermanager, utilising the Lucene Near Realtime Search mechanism.
 *
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class UpdatedRepositoryListener implements ApplicationListener {

	def modelDelegateService
	def grailsApplication = Holders.grailsApplication
	Directory fsDirectory
	File location
	/**
	* Creates/Opens a lucene index based on the config properties (unless test, 
	* otherwise a default location is used, to avoid corrupting the index). 
	*/
	public UpdatedRepositoryListener() {
		String path=grailsApplication.config.jummp.search.index
		if (Environment.current == Environment.TEST) {
			path = "target/search/index"
			File deleteMe=new File(path)
			deleteMe.deleteDir()
		}
		location=new File(path)
		location.mkdirs()
		//Create instance of Directory where index files will be stored
		fsDirectory =  FSDirectory.getDirectory(location)
		/* Create instance of analyzer, which will be used to tokenize
		the input data */
		
		/*
		LUCENE 4.4 CODE.
		
		//Create the instance of deletion policy
		Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_44)
		IndexDeletionPolicy deletionPolicy = new KeepOnlyLastCommitDeletionPolicy() 
		
		IndexWriterConfig conf=new IndexWriterConfig(Version.LUCENE_44,standardAnalyzer)
		conf.setIndexDeletionPolicy(deletionPolicy)
		conf.setOpenMode(OpenMode.CREATE_OR_APPEND)
		indexWriter =new IndexWriter(fsDirectory,conf)
		*/
		
	}
	
	/**
	* Responds to model creation/update events
	*
	* Responds to the @param event, if it is a model create/update, it is indexed
	* in the lucene index.
	* @param event The event, to be handled if it a model create/update event
	**/
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof RevisionCreatedEvent) {
			updateIndex((event as RevisionCreatedEvent).revision)
		}
		if (event instanceof ModelCreatedEvent) {
			ModelCreatedEvent modEvent=event as ModelCreatedEvent
			System.out.println("EVENT: "+modEvent.model.getProperties())
			System.out.println("EVENT: "+modEvent.model.inspect())
			updateIndex(modelDelegateService.
						getLatestRevision(modEvent.model.id))
													
		}
	}
	
	/*
		Clears the index. Handle with care.
	*/
	public void clearIndex() {
		/*Analyzer standardAnalyzer = new StandardAnalyzer()
		IndexWriter indexWriter = new IndexWriter(fsDirectory, standardAnalyzer)
		indexWriter.deleteAll()
		indexWriter.optimize()
		indexWriter.close()*/
		location.deleteDir()
		location.mkdirs()
		fsDirectory =  FSDirectory.getDirectory(location)
	}
	
	/**
	* Adds a revision to the index
	*
	* Adds the specified @param revision to the lucene index
	* @param revision The revision to be indexed
	**/
	public void updateIndex(RevisionTransportCommand revision) {
		
		Analyzer standardAnalyzer = new StandardAnalyzer()
		IndexWriter indexWriter = new IndexWriter(fsDirectory, standardAnalyzer)
		indexWriter.setMaxFieldLength(25000)
		
		String name = revision.name ?: ""
		String description = revision.description ?: ""
		String content = modelDelegateService.getSearchIndexingContent(revision) ?: ""
		Document doc = new Document()
		
		/*
		*	Indexed fields
		*/
		Field nameField =
			new Field("name",name,Field.Store.YES,Field.Index.ANALYZED)
		Field descriptionField = 
			new Field("description",description,Field.Store.NO,Field.Index.ANALYZED) 
		Field formatField = 
			new Field("modelFormat",""+revision.format.name,Field.Store.YES,Field.Index.ANALYZED)
		Field levelVersionField = 
			new Field("levelVersion",""+revision.format.formatVersion,Field.Store.NO,Field.Index.ANALYZED)
		Field submitterField = 
			new Field("submitter",""+revision.owner,Field.Store.YES,Field.Index.ANALYZED)
		Field contentField = 
			new Field("content",content,Field.Store.NO,Field.Index.ANALYZED) 
		
		doc.add(nameField)
		doc.add(descriptionField)
		doc.add(formatField)
		doc.add(levelVersionField)
		doc.add(submitterField)
		doc.add(contentField)
			
		/*
		*	Stored fields. Hopefully will be used to display the search results one day
		*	instead of going to the database for each model. When we find a solution to needing to
		*	look in the database to figure out if the user has access to a model. 
		*/
		Field idField = 
			new Field("model_id",""+revision.model.id,Field.Store.YES,Field.Index.NO) 
		Field versionField = 
			new Field("versionNumber",""+revision.revisionNumber,Field.Store.YES,Field.Index.NO)
		Field submittedField = 
			new Field("submissionDate",""+revision.model.submissionDate,Field.Store.YES,Field.Index.NO)
		doc.add(idField)
		doc.add(versionField)
		doc.add(submittedField)
		
		indexWriter.addDocument(doc)
		//indexWriter.commit() // To do: investigate a more optimised commit mechanism (4.4)
		indexWriter.optimize()
		indexWriter.close()
	}
	
	
	public Directory getDirectory() {
		return fsDirectory
	}
	
	/**
	* Gets a searchermanager linked to the indexwriter (4.4)
	*
	* @returns A searchermanager linked to the indexwriter, so that changes made in the writer will be
	* reflected in the searcher.
	public SearcherManager getSearcherManager() {
		boolean applyAllDeletes = true
		SearcherManager mgr = new SearcherManager(indexWriter, true, new SearcherFactory())
                return mgr
	}
	*/
	
	
}
