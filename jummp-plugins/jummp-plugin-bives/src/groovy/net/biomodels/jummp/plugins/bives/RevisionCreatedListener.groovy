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
* groovy, Spring Framework, Grails, Bives (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, GNU GPL v3.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of groovy, Spring Framework, Grails, Bives used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.events.RevisionCreatedEvent
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
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
                //FIXME we assume there is only one file
				File testFile = ((RevisionCreatedEvent) event).files.first()
                                File parentTempDir = testFile.getParentFile();
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
                List<RepositoryFileTransportCommand> modelFiles = modelDelegateService.retrieveModelFiles(
                        modelDelegateService.getRevision(command.model.id, command.revisionNumber - 1))
                modelFiles=modelFiles.findAll {it.mainFile }
                File file=new File(modelFiles.first().path)
                def modelByteArray = file.getBytes()
                new FileOutputStream(refFile).write(modelByteArray)
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
                                parentTempDir.deleteDir()
			} catch (Exception e) {
				e.printStackTrace()
			}
		}
	}
}
