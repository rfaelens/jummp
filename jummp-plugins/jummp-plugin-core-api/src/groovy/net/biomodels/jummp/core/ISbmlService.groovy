package net.biomodels.jummp.core

import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Interface describing the service to access an SBML Model.
 *
 * The implementation of this interface is in the SBML plugin. Although this
 * interface is defined in core-api there is no guarantee that core provides
 * support for models in SBML. It is as well possible to support different
 * model formats in which case another service has to be defined to support
 * the interaction with the service for the file format.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
interface ISbmlService {
    /**
     * Retrieves the metaId on the Model level. Does not retrieve the metaId of child elements
     * of model such as compartment.
     * @param revision
     * @return MetaId on Model level
     */
    public String getMetaId(RevisionTransportCommand revision)
    /**
     *
     * @param revision
     * @return Version of the SBML file
     */
    public long getVersion(RevisionTransportCommand revision)
    /**
     *
     * @param revision
     * @return Level of the SBML file
     */
    public long getLevel(RevisionTransportCommand revision)
    /**
     * Retrieves the notes element as an xml String on model level.
     * It cannot be used to retrieve notes on another level. If there
     * are no notes an empty string is returned.
     * @param revision
     * @return The notes of the model.
     */
    public String getNotes(RevisionTransportCommand revision)

    /**
     * Retrieves the MIRIAM annotations for the model element.
     * The returned list contains one map for each annotation with the following keys/values:
     * @li qualifier: name for the MIRIAM qualifier as listed in http://sbml.org/Special/Software/JSBML/build/apidocs/org/sbml/jsbml/CVTerm.Qualifier.html
     * @li biologicalQualifier: @c true if the qualifier is a biological qualifier, @c false otherwise
     * @li modelQualifier: @c true if the qualifier is a model qualifier, @c false otherwise
     * @li resources: list of all uris for this annotation
     * @param revision
     * @return List of all MIRIAM annotations of the model element
     */
    public List<Map> getAnnotations(RevisionTransportCommand revision)

    /**
     * Retrieves the global parameters of the SBML Model.
     * The returned list contains one map for each parameter with the following keys/values:
     * @li id: the id element
     * @li metaId: The metaId element
     * @li name: The name element
     * @li constants: @c true if parameter is a constant, @c false otherwise
     * @li value: The value element if set, otherwise @c null
     * @li sboTerm: The numerical sbo term if set, otherwise @c null
     * @li unit: The unit element
     * @param revision
     * @return List of all parameters in the Model
     */
    public List<Map> getParameters(RevisionTransportCommand revision)
}
