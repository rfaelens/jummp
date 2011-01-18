package net.biomodels.jummp.core.model

/**
 * Constants defining the format of a Model File.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public enum ModelFormat {
    /**
     * Model File Format is not known. This type is mostly for testing purpose.
     */
    UNKNOWN,
    /**
     * Model File Format is Systems Biology Markup Language.
     */
    SBML
    // TODO: do we need more constants for the different SBML levels?
}
