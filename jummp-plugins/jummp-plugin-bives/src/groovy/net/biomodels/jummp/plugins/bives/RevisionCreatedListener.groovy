package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.events.RevisionCreatedEvent
import net.biomodels.jummp.core.model.RevisionTransportCommand

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener

import de.unirostock.bives.diff.model.Diff
import de.unirostock.bives.fwk.diff.DiffGeneratorManager


/**
 * @short Listener for new revisions
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 20.06.2011
 * @year 2011
 */
class RevisionCreatedListener implements ApplicationListener {
	/**
	 * Dependency Injection of ModelDelegateService
	 */
	def modelDelegateService
	/**
	 * The logger for this class
	 */
	Logger log = Logger.getLogger(getClass())

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof RevisionCreatedEvent) {
			try {
				// initiate variables
				DiffGeneratorManager diffMan = new DiffGeneratorManager()
				// first, map event...
				RevisionTransportCommand command = ((RevisionCreatedEvent) event).revision
				File testFile = ((RevisionCreatedEvent) event).file
				// get previous revision
				File refFile = File.createTempFile("referenceFile", ".xml")
				refFile.write(new String(modelDelegateService.retrieveModelFile(modelDelegateService.getRevision(command.model.id, command.revisionNumber - 1))))
				// create diff, initialize required variables
				Diff diff = diffMan.generateDiff(refFile, testFile, true)
				// debug
				int prevRev = command.revisionNumber - 1
				int currRev = command.revisionNumber
				diff.setModelId(command.model.id as String)
				diff.setOriginId(prevRev)
				diff.setSuccessorId(currRev)
				long modelId = command.model.id
				String diffDir = ConfigurationHolder.config.jummp.plugins.bives.diffdir as String
				JummpRepositoryManager repoMan = new JummpRepositoryManager()
				repoMan.createNewRepository(diffDir)
				repoMan.uploadDiff(diff, modelId, prevRev, currRev)
				// TODO database entries -> later

				// TODO optional: Component extension for diffs
				refFile.delete();
			} catch (Exception e) {
				e.printStackTrace()
			}
		}
	}
}
