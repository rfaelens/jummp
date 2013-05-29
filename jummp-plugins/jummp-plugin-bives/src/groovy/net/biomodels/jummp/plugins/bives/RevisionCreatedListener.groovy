package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.events.RevisionCreatedEvent
import net.biomodels.jummp.core.model.RevisionTransportCommand

import grails.util.Environment 
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper

import de.unirostock.bives.diff.model.Diff
import de.unirostock.bives.fwk.diff.DiffGeneratorManager


/**
 * @short Listener for new revisions
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @author Mihai Glonț <mglont@ebi.ac.uk>
 * @date   7/04/2013
 */
class RevisionCreatedListener implements ApplicationListener {
	/**
	 * Dependency Injection of ModelDelegateService
	 */
	def modelDelegateService
    /**
     * Dependency Injection of DiffDataService
     */
    def diffDataService

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof RevisionCreatedEvent) {
			try {
				// initiate variables
				DiffGeneratorManager diffMan = new DiffGeneratorManager()
				// first, map event...
				RevisionTransportCommand command = ((RevisionCreatedEvent) event).revision
				File testFile = ((RevisionCreatedEvent) event).file
                ConfigObject config = ConfigurationHelper.loadConfigFromClasspath(Environment.getCurrent().getName())
                final File location
                if (config.containsKey("jummp.plugins.bives.diffdir")) {
                    location = new File(config.getProperty("jummp.plugins.bives.diffdir"))
                }
                else {
                    location = new File(System.getProperty("java.io.tmpdir"))
                }
				// get previous revision
				File refFile = File.createTempFile("referenceFile", ".xml", location) 
				refFile.write(new String(modelDelegateService.retrieveModelFiles(
                        modelDelegateService.getRevision(command.model.id, command.revisionNumber - 1))))
				// create diff, initialize required variables
				Diff diff = diffMan.generateDiff(refFile, testFile, true)
				// debug
				int prevRev = command.revisionNumber - 1
				int currRev = command.revisionNumber
				diff.setModelId(command.model.id as String)
				diff.setOriginId(prevRev)
				diff.setSuccessorId(currRev)
				long modelId = command.model.id
				JummpRepositoryManager repoMan = new JummpRepositoryManager()
				repoMan.createNewRepository(diffDataService.diffDirectory())
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
