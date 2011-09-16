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

	private int previousRevision, recentRevision
	/**
	 * The Authentication of the user who uploaded the Revision
	 */
	private Authentication authentication

	@Override
	public void run() {
		try {
			// set the Authentication in the Thread's SecurityContext
			SecurityContextHolder.context.setAuthentication(authentication)
			DiffDataProvider diffData = grailsApplication.mainContext.getBean("diffDataProvider") as DiffDataProvider
			if(!diffData.getDiffInformation(modelId, previousRevision, recentRevision)) {
				DiffGeneratorManager diffMan = new DiffGeneratorManager()
				String previous = new String (modelDelegateService.retrieveModelFile(modelDelegateService.getRevision(modelId,
						previousRevision)))
				String recent = new String(modelDelegateService.retrieveModelFile(modelDelegateService.getRevision(modelId,
						recentRevision)))
				Diff diff = diffMan.generateDiff(previous, recent, true)
				diff.setModelId(modelId as String)
				diff.setOriginId(previousRevision)
				diff.setSuccessorId(recentRevision)
				JummpRepositoryManager repoMan = new JummpRepositoryManager()
				repoMan.createNewRepository(diffDataService.diffDirectory())
				repoMan.uploadDiff(diff, modelId, previousRevision, recentRevision)
				diffDataService.unqueueDiff(modelId, previousRevision, recentRevision)
			}
		} catch (Exception e) {
			e.printStackTrace()
		} finally {
			diffDataService.unqueueDiff(modelId, previousRevision, recentRevision)
			SecurityContextHolder.clearContext()
		}
	}

	/**
	 * Returns a new instance of this class
	 * @param modelId the id of the corresponding model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return
	 */
	static public CreateDiffThread getInstance(Long modelId, int previousRevision, int recentRevision) {
		CreateDiffThread thread = new CreateDiffThread()
		thread.authentication = SecurityContextHolder.context.authentication
		thread.modelId = modelId
		thread.previousRevision = previousRevision
		thread.recentRevision = recentRevision
		return thread
	}

}
