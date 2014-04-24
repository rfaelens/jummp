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





/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.core.bives;

import net.biomodels.jummp.core.JummpException;

/**
 * Exception class for DiffDataService in case a diff file does not exist
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 20.07.2011
 * @year 2011
 */
public class DiffNotExistingException extends JummpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DiffNotExistingException() {
		super("DiffNotExistingException");
	}
	
	public DiffNotExistingException(String message) {
		super(message);
	}
	
    public DiffNotExistingException(String message, Throwable cause) {
        super(message, cause);
    }
}
