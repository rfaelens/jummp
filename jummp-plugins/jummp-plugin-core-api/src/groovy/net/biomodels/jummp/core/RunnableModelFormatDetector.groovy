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





package net.biomodels.jummp.core

/**
 * @short Interface describing a class that detects the format of a model.
 *
 * Plugins providing support for a particular model format are expected
 * to implement this interface.
 * @see net.biomodels.jummp.core.model.FileFormatService#areFilesThisFormat(Lis)
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public interface RunnableModelFormatDetector extends Runnable {
    /**
     * Detects whether a certain @p modelFile conforms to a given model format.
     * @param modelFile the file that should be examined.
     * @return true if there was a match, false otherwise.
     */
    public boolean isRecognisedFormat(final File modelFile);
}
