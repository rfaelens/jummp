/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.model.ModelVersionTransportCommand

import org.apache.log4j.Logger
import org.springframework.beans.factory.InitializingBean
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

	/**
	 * List containing moves; key: current (higher) revision, value: previous (lower) revision
	 */
	List moves = []
	/**
	 * List containing inserts; key: current (higher) revision, value: previous (lower) revision
	 * value may contain nothing more than the type of the element
	 */
	List inserts = []
	/**
	 * List containing deletes; key: current (higher) revision, value: previous (lower) revision
	 * key may contain nothing more than the type of the element
	 */
	List deletes = []
	/**
	 * List containing updates; key: current (higher) revision, value: previous (lower) revision
	 */
	List updates = []
	// models
	private ModelVersionTransportCommand currRev
	private ModelVersionTransportCommand prevRev
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
     * Dependency Injection of grailsApplication
     */
    def grailsApplication
    def diffDataService
	/**
	 * The logger for this class
	 */
	Logger log = Logger.getLogger(getClass())

	@Override
	public void afterPropertiesSet() throws Exception {
		repoMan = new JummpRepositoryManager(diffDataService.diffDirectory())
	}

	/**
	 * Retrieves all the change types stored in the {@link Diff} file, storing them
	 * in the the maps <code>moves</code>, <code>inserts</code>, <code>deletes</code>,
	 * and <code>updates</code>.
	 * @param modelId the id of the corresponding model
	 * @param previousVersion the number of a previous model revision
	 * @param recentVersion a successor revision (in relation to the previous revision)
	 * @return <code>true</code> if the {@link Diff} information was successfully retrieved, <code>false</code> otherwise
	 */
	public boolean getDiffInformation(long modelId, int previousVersion,  int recentVersion) {
		File diffFile = repoMan.getDiffFile(modelId, previousVersion, recentVersion)
		Diff diff = null;
		if(diffFile != null && diffFile.exists()) {
			diff = repoMan.getDiff(repoMan.getDiffFile(modelId, previousVersion, recentVersion))
			// get models
			currRev = modelDelegateService.getVersion(modelId, recentVersion)
			prevRev = modelDelegateService.getVersion(modelId, previousVersion)
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
	 * Returns the JSBML object for the provided xpath of an element
	 * @param xpath the XPath of the XML element
	 * @param revision the {@link ModelVersionTransportCommand} resp. the Model
	 * @return a {@link Map} containing the type of the element and its JSBML representation
	 */
	private Map<Map, String> getPathObject(String xpath, ModelVersionTransportCommand version) {
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
						sbmlNode = sbmlService."get${elementName}"(version, id)
						sbmlNode.type = elementName
						break
					}
				}
			// we are looking at an element without an id, so we have to improvise ;)
			} else {
				// TODO this part may have to be implemented
			}
			return sbmlNode
		} catch (Exception e) {
			log.fatal(e.getMessage())
		}
		return [:]
	}
}
