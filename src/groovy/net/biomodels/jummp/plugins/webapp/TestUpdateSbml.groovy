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
**/


package net.biomodels.jummp.plugins.webapp
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import grails.util.Holders


    /* Class for testing out the update mechanism. Creates a model with an
     * unknown file format. Then updates the model with an sbml file thereby
     * changing the model type. Ensures that the revision reflects the new
     * model's name and description (from the sbml file), and contains both
     * the original and the new model files*/
    class TestUpdateSbml extends UpdateBase {
        File existing
        def modelService=Holders.applicationContext.getBean("modelService")
        TestUpdateSbml(long m, File uploaded) {
            super(m)
            existing=uploaded
        }
        void performTest() {
            def viewSelection = startFlow()
            //signalEvent("Continue")
            assertFlowState("uploadFiles")
            File newFile=bigModel()
            Map<File,String> additionalFiles=getRandomAdditionalFiles(10)
            addSubmissionFiles([newFile], additionalFiles)
            signalEvent("Upload")
            
            assertFlowState("displayModelInfo")
            signalEvent("Continue")
            assert true == (Boolean) flowScope.
                                        workingMemory.
                                        get("isUpdateOnExistingModel")
            assert "SBML" == flowScope.workingMemory.get("model_type") as String
            RTC revision=flowScope.workingMemory.get("RevisionTC") as RTC
            //test name
            assert "Becker2010_EpoR_AuxiliaryModel" == revision.name
            assert revision.description.contains("This relation solely depends on EpoR turnover independent of ligand binding, suggesting an essential role of large intracellular receptor pools. These receptor properties enable the system to cope with basal and acute demand in the hematopoietic system")
            
            //add tests for when displayModelInfo does something interesting
            //signalEvent("Continue") display model info disabled
            
            //Dont add publication info
            signalEvent("Continue")
            
            assertFlowState("displaySummaryOfChanges")
            Model model=modelService.getModel(modelid)
            Revision prev=modelService.getLatestRevision(model)
            assert prev
            signalEvent("Continue")
            //assert flowExecutionOutcome.id == "displayConfirmationPage"
            
            
            //test that the model is infact saved in the database
            Revision rev=modelService.getLatestRevision(model)
            //test that revision is saved correctly
            assert rev
            assert rev.comment.contains("Model revised without commit message")
            assert rev.revisionNumber==prev.revisionNumber+1
            
            //test that files are updated in the repository correctly
            List<RepositoryFileTransportCommand> files=modelService.retrieveModelFiles(model)
            validateFiles(files, [existing, newFile]+additionalFiles.keySet())
        }
    }

