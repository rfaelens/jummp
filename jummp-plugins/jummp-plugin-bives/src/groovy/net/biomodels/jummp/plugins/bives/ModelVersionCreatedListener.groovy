package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.events.ModelVersionCreatedEvent
import net.biomodels.jummp.core.model.ModelVersionTransportCommand

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
class ModelVersionCreatedListener implements ApplicationListener {
	/**
	 * Dependency Injection of ModelDelegateService
	 */
	def modelDelegateService
    /**
     * Dependency Injection of DiffDataService
     */
    def diffDataService

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ModelVersionCreatedEvent) {
			try {
				// initiate variables
				DiffGeneratorManager diffMan = new DiffGeneratorManager()
				// first, map event...
				ModelVersionTransportCommand command = ((ModelVersionCreatedEvent) event).version
				File testFile = ((ModelVersionCreatedEvent) event).file
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
				refFile.write(new String(modelDelegateService.retrieveModelFile(
                        modelDelegateService.getVersion(command.model.id, command.versionNumber - 1))))
				// create diff, initialize required variables
				Diff diff = diffMan.generateDiff(refFile, testFile, true)
				// debug
				int prevRev = command.versionNumber - 1
				int currRev = command.versionNumber
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
