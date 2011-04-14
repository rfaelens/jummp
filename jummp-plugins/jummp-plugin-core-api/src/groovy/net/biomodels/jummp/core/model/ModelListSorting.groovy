package net.biomodels.jummp.core.model

/**
 * @short Enum to specify the column for sorting the Model overview.
 *
 * The primary use of this enum is to support sorting of arbitrary columns
 * in ModelService.getAllModels(). This enum is used to specify the column
 * which has to be used for sorting when going down to the database.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public enum ModelListSorting {
    ID, ///< sort by the Model.id column in numerical order
    NAME, ///< sort by Model.name in alphanumerical order
    PUBLICATION, ///< sort by the Model's publication, TODO: what to sort on directly
    LAST_MODIFIED, ///< sort by the last modification date, that is latest revision
    FORMAT ///< sort by the name of the format
}
