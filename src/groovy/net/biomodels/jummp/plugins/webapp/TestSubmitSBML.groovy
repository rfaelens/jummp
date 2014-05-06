/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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

