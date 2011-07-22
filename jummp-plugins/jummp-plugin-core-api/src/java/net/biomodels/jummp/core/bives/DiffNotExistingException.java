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
