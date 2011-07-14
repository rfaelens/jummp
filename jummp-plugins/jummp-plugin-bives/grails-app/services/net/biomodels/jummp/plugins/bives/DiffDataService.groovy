package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.plugins.bives.DiffDataProvider

import org.codehaus.groovy.grails.commons.ApplicationHolder

import de.unirostock.bives.diff.model.Diff
import de.unirostock.bives.fwk.diff.DiffGeneratorManager

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
public class DiffDataService {

	/**
	* Dependency Injection of ModelDelegateService
	*/
	def modelDelegateService
	
    static transactional = true

    Map generateDiffData(long modelId, int previousRevision, int recentRevision) {
		DiffDataProvider diffData = ApplicationHolder.application.mainContext.getBean("diffDataProvider") as DiffDataProvider
		if(!diffData.getDiffInformation(modelId, previousRevision, recentRevision)) {
			DiffGeneratorManager diffMan = new DiffGeneratorManager()
			println("diff does not exist... generating...")
			InputStream prevIn = new FileInputStream(modelDelegateService.retrieveModelFile(modelDelegateService.getRevision(modelId,
				previousRevision)))
			InputStream refIn = new FileInputStream(modelDelegateService.retrieveModelFile(modelDelegateService.getRevision(modelId,
				recentRevision)))
			
			Diff diff = diffMan.generateDiff(refIn, prevIn, true)
			diffData = ApplicationHolder.application.mainContext.getBean("diffDataProvider") as DiffDataProvider
		}
		return [moves: diffData.moves, updates: diffData.updates, inserts: diffData.inserts, deletes: diffData.deletes]
    }
}
