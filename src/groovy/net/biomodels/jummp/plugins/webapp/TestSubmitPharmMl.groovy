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

class TestSubmitPharmMl extends TestUploadFiles {
    void performRemainingTest() {
        final File MODEL_FILE =
                new File("jummp-plugins/jummp-plugin-pharmml/test/files/example1.xml")
        String[] descriptionTokens = new String[11]
        descriptionTokens[0] = "Model comprised of files: example1.xml"
        (0..<10).each {
            descriptionTokens[it +1] = "add_file_${it}.xml".toString()
        }

        fileUploadPipeline(MODEL_FILE, "PharmML", "Example 1 - simulation continuous PK/PD",
                descriptionTokens)
    }
}

