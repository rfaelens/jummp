/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.model.RevisionTransportCommand

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.InitializingBean

import net.biomodels.jummp.plugins.bives.JummpRepositoryManager
import de.unirostock.bives.diff.model.Diff
import de.unirostock.bives.diff.model.ElementType
import de.unirostock.bives.diff.model.AttributeType
import de.unirostock.bives.diff.model.ValueType

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 28.06.2011
 * @year 2011
 */
class DiffDataProvider implements InitializingBean {

	// key: element from current revision, value: element from previous revision
	List moves = []
	// key: element from current revision, value: element from previous revision
	List inserts = []
	// key: element from previous revision, value: element from current revision
	List deletes = []
	// key: element from current revision, value: element from previous revision
	List updates = []
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
	public boolean getDiffInformation(long modelId, int predecessorRevision,  int recentRevision) {
		File diffFile = repoMan.getDiffFile(modelId, predecessorRevision, recentRevision)
		Diff diff = null;
		if(diffFile.exists()) {
			diff = repoMan.getDiff(repoMan.getDiffFile(modelId, predecessorRevision, recentRevision))
			// get models
			currRev = modelDelegateService.getRevision(modelId, recentRevision)
			prevRev = modelDelegateService.getRevision(modelId, predecessorRevision)
			// for filtering duplicate moves
			String currentPath = "";
			// moves
			for(ElementType element : diff.getMoves().getElement()) {
				if(currentPath.equals(element.getOldPath())) {
					moves << [current: getPathObject(element.getPath(), currRev),
							previous: getPathObject(element.getOldPath(), prevRev)]
				}
				currentPath = element.getPath();
			}
			//inserts
			for(ElementType element : diff.getInserts().getElement()) {
				inserts << [current: getPathObject(element.getPath(), currRev),
						previous: getPathObject(element.getPath(), prevRev)]
			}
			for(AttributeType attribute : diff.getInserts().getAttribute()) {
				inserts << [current: getPathObject(attribute.getPath(), currRev),
						previous: getPathObject(attribute.getPath(), prevRev)]
			}
			for(ValueType value : diff.getInserts().getValue()) {
				inserts << [current: getPathObject(value.getPath(), currRev),
						previous: getPathObject(value.getPath(), prevRev)]
			}
			// deletes
			for(ElementType element : diff.getDeletes().getElement()) {
				deletes << [current: getPathObject(element.getPath(), currRev),
						previous: getPathObject(element.getPath(), prevRev)]
			}
			for(AttributeType attribute : diff.getDeletes().getAttribute()) {
				deletes << [current: getPathObject(attribute.getPath(), currRev),
						previous: getPathObject(attribute.getPath(), prevRev)]
			}
			for(ValueType value : diff.getDeletes().getValue()) {
				deletes << [current: getPathObject(value.getPath(), currRev),
						previous: getPathObject(value.getPath(), prevRev)]
			}
			// updates
			for(AttributeType attribute : diff.getUpdates().getAttribute()) {
				updates << [current: getPathObject(attribute.getPath(), currRev),
						previous: getPathObject(attribute.getPath(), prevRev)]
			}
			for(ValueType value : diff.getUpdates().getValue()) {
				updates << [current: getPathObject(value.getPth(), currRev),
						previous: getPathObject(value.getPath(), prevRev)]
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param xpath
	 * @param revision
	 * @return
	 */
	private Map<Map, String> getPathObject(String xpath, RevisionTransportCommand revision) {
		try {
			String[] nodes = xpath.split("/")
			boolean resolved = false
			Map sbmlNode = [:]
			// if the xpath has more than 1 id in it
			if(xpath.count(ID) > 1) {
				for(int i = nodes.size() - 1; i >= 0; i--) {
					// do some kind of backtracking in this loop until first node with @id is found
					if(nodes[i].contains(ID)) {
						String elementName = nodes[i].subSequence(nodes[i].indexOf(":") + 1, nodes[i].indexOf("[")).capitalize()
						String id = nodes[i].subSequence(nodes[i].indexOf("'") + 1, nodes[i].lastIndexOf("'"))
						sbmlNode = sbmlService."get${elementName}"(revision, id)
						sbmlNode.type = elementName
						break
					}
				}
			// we are looking at an element without an id, so we have to improvise ;)
			} else {
				// TODO
			}
			return sbmlNode
		} catch (Exception e) {
			log.fatal(e.getMessage())
		}
		return [:]
	}

}