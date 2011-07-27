/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating BiVeS settings.
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 26.07.2011
 * @year 2011
 */
class BivesCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String diffDir

    static constraints = {
        diffDir(nullable: false, blank: false)
    }
}
