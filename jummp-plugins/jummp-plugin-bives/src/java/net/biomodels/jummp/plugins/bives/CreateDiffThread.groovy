package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.plugins.bives.DiffDataProvider

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import de.unirostock.bives.diff.model.Diff
import de.unirostock.bives.fwk.diff.DiffGeneratorManager

/**
 * A thread for the generation of a {@link Diff} file between two revisions of a model.
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 14.07.2011
 * @year 2011
 */
class CreateDiffThread implements Runnable {

	/**
	 * Dependency Injection of ModelDelegateService
	 */
	def modelDelegateService
    /**
     * Dependency Injection of Grails Application
     */
    def grailsApplication

	/**
	 * Dependency Injection of DiffDataService
	 */
	def diffDataService

	private long modelId

	private int previousVersion, recentVersion
	/**
	 * The Authentication of the user who uploaded the ModelVersion
	 */
	private Authentication authentication

	@Override
	public void run() {
		try {
			// set the Authentication in the Thread's SecurityContext
			SecurityContextHolder.context.setAuthentication(authentication)
			DiffDataProvider diffData = grailsApplication.mainContext.getBean("diffDataProvider") as DiffDataProvider
			if(!diffData.getDiffInformation(modelId, previousVersion, recentVersion)) {
				DiffGeneratorManager diffMan = new DiffGeneratorManager()
				String previous = new String (modelDelegateService.retrieveModelFile(modelDelegateService.getVersion(modelId,
						previousVersion)))
				String recent = new String(modelDelegateService.retrieveModelFile(modelDelegateService.getVersion(modelId,
						recentVersion)))
				Diff diff = diffMan.generateDiff(previous, recent, true)
				diff.setModelId(modelId as String)
				diff.setOriginId(previousVersion)
				diff.setSuccessorId(recentVersion)
				JummpRepositoryManager repoMan = new JummpRepositoryManager()
				repoMan.createNewRepository(diffDataService.diffDirectory())
				repoMan.uploadDiff(diff, modelId, previousVersion, recentVersion)
				diffDataService.unqueueDiff(modelId, previousVersion, recentVersion)
			}
		} catch (Exception e) {
			e.printStackTrace()
		} finally {
			diffDataService.unqueueDiff(modelId, previousVersion, recentVersion)
			SecurityContextHolder.clearContext()
		}
	}

	/**
	 * Returns a new instance of this class
	 * @param modelId the id of the corresponding model
	 * @param previousVersion the number of a previous model revision
	 * @param recentVersion a successor revision (in relation to the previous revision)
	 * @return
	 */
	static public CreateDiffThread getInstance(Long modelId, int previousVersion, int recentVersion) {
		CreateDiffThread thread = new CreateDiffThread()
		thread.authentication = SecurityContextHolder.context.authentication
		thread.modelId = modelId
		thread.previousVersion = previousVersion
		thread.recentVersion = recentVersion
		return thread
	}

}
