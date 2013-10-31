/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


package net.biomodels.jummp.core.vcs;

/**
 * Exception indicating that a VCS operation was tried to perform on a file that is not under version control.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class FileNotVersionedException extends VcsException {
    private String fileName;

    /**
     * Constructs the exception
     * @param fileName The name of the file tried to access
     */
    public FileNotVersionedException(String fileName) {
        super("The File " + fileName + " is not under version control.");
        this.fileName = fileName;
    }

    /**
     * @return The name of the file tried to access
     */
    public String getFileName() {
        return this.fileName;
    }
}
