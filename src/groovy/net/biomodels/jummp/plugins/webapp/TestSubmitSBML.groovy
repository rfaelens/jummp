package net.biomodels.jummp.plugins.webapp



/* Tests the SBML functionality with the known SBML file */
    class TestSubmitSBML extends TestUploadFiles {
        void performRemainingTest() {
            String[] descriptionTests = new String[4]
            descriptionTests[0]="Verena Becker, Marcel Schilling, Julie Bachmann, Ute Baumann, Andreas Raue, Thomas Maiwald, Jens Timmer and Ursula Klingmüller"
            descriptionTests[1]="This relation solely depends on EpoR turnover independent of ligand binding, suggesting an essential role of large intracellular receptor pools. These receptor properties enable the system to cope with basal and acute demand in the hematopoietic system"
            descriptionTests[2]="%% Default sampling time points"
            descriptionTests[3]="BioModels Database: An enhanced, curated and annotated resource for published quantitative kinetic models"
            fileUploadPipeline(bigModel(), 
                               "SBML", 
                               "Becker2010_EpoR_AuxiliaryModel",
                               descriptionTests)
        }
    }

