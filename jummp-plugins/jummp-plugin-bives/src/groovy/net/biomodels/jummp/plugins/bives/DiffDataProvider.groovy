/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.plugins.sbml.SbmlService

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.InitializingBean

import de.unirostock.bives.diff.model.AttributeType
import de.unirostock.bives.diff.model.Diff
import de.unirostock.bives.diff.model.ElementType
import de.unirostock.bives.diff.model.ValueType

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 28.06.2011
 * @year 2011
 */
class DiffDataProvider implements InitializingBean {

	// key: element from current revision, value: element from previous revision
	Map moves = [:]
	// key: element from current revision, value: element from previous revision
	Map inserts = [:]
	// key: element from previous revision, value: element from current revision
	Map deletes = [:]
	// key: element from current revision, value: element from previous revision
	Map updates = [:]
	// models
	private RevisionTransportCommand currRev
	private RevisionTransportCommand prevRev
	private JummpRepositoryManager repoMan
	private static final String ID = "@id=";

	/**
	 * Dependency Injection of ModelDelegateService
	 */
	def modelDelegateService

	/**
	 * Dependency Injection of SbmlService
	 */
	def sbmlService
	/**
	 * The logger for this class
	 */
	Logger log = Logger.getLogger(getClass())

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		String diffDir = ConfigurationHolder.config.jummp.plugins.bives.diffdir as String
		repoMan = new JummpRepositoryManager(diffDir)
	}

	/**
	 * 
	 * @param originId
	 * @param predecessorId
	 */
	public void getDiffInformation(long modelId, int predecessorRevision,  int originRevision) {
		File diffFile = repoMan.getDiffFile(modelId, predecessorRevision, originRevision)
		Diff diff = null;
		if(diffFile.exists()) {
			diff = repoMan.getDiff(repoMan.getDiffFile(modelId, predecessorRevision, originRevision))
		}
		if(diff == null) {
			println("<<< diff is null, can not return diff information >>>")
		} else {
			// get models
			currRev = modelDelegateService.getRevision(modelId, originRevision)
			prevRev = modelDelegateService.getRevision(modelId, predecessorRevision)

			// TODO get model files for both revisions and store them
			// moves
			for(ElementType element : diff.getMoves().getElement()) {
				moves.put(getPathObject(element.getOldpath(), currRev),
						getPathObject(element.getPath(), prevRev))
			}
			//inserts
			for(ElementType element : diff.getInserts().getElement()) {
				inserts.put(getPathObject(element.getPath(), currRev), null)
			}
			for(AttributeType attribute : diff.getInserts().getAttribute()) {
				inserts.put(getPathObject(attribute.getPath(), currRev),
						getPathObject(attribute.getPath(), prevRev))
			}
			for(ValueType value : diff.getInserts().getValue()) {
				inserts.put(getPathObject(value.getPath(), currRev),
						getPathObject(value.getPath(), prevRev))
			}
			// deletes
			for(ElementType element : diff.getInserts().getElement()) {
				deletes.put(getPathObject(element.getPath(), prevRev), null)
			}
			for(AttributeType attribute : diff.getInserts().getAttribute()) {
				deletes.put(getPathObject(attribute.getPath(), prevRev),
						getPathObject(attribute.getPath(), currRev))
			}
			for(ValueType value : diff.getInserts().getValue()) {
				deletes.put(getPathObject(value.getPath(), prevRev),
						getPathObject(value.getPath(), currRev))
			}
			// updates
			for(AttributeType attribute : diff.getUpdates().getAttribute()) {
				updates.put(getPathObject(attribute.getPath(), currRev),
						getPathObject(attribute.getPath(), prevRev))
			}
			for(ValueType value : diff.getUpdates().getValue()) {
				updates.put(getPathObject(attribute.getPath(), currRev),
						getPathObject(attribute.getPath(), prevRev))
			}
		}
	}

	/**
	 * 
	 */
	private Object getPathObject(String xpath, RevisionTransportCommand revision) {
		try {
			String[] nodes = xpath.split("/")
			boolean resolved = false
			Object sbmlNode = null
			// if the xpath has more than 1 id in it
			if(xpath.count(ID) > 1) {
				for(int i = nodes.size() - 1; i >= 0; i--) {
					// do some kind of backtracking in this loop until first node with @id is found
					if(nodes[i].contains(ID)) {
						String elementName = nodes[i].subSequence(nodes[i].indexOf(":") + 1, nodes[i].indexOf("[")).capitalize()
						String id = nodes[i].subSequence(nodes[i].indexOf("'") + 1, nodes[i].lastIndexOf("'"))
						println("Element name: " + elementName)
						println("Element id: " + id)
						sbmlNode = sbmlService."get${elementName}"(revision, id)
						break
					}
				}
			// we are looking at an element without an id, so we have to improvise ;)
			} else {
				
			}
			return sbmlNode
		} catch (Exception e) {
			log.fatal(e.getMessage())
		}
		return null
	}

}