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
    /**
     * Retrieves the parameter with given @p id from the SBML Model.
     * The returned Map contains all the elements as explained in @link getParameters
     * with additionally the annotation and notes added to the map. The annotation
     * element follows the description of @link getAnnotations.
     * @param revision
     * @param id The unique id in the SBML Model.
     * @return Map of all parameter data
     */
    public Map getParameter(RevisionTransportCommand revision, String id)
    /**
     * Retrieves all parameters local to the reactions in the SBML Model.
     * The parameters are sorted to the reactions. The returned list is actually a list
     * of minimum information about the reaction plus the parameters:
     * @li id: The id of the reaction
     * @li name: The name of the reaction
     * @li parameters: List of parameters as in @link getParameters with the following addition:
     * constant is always true and an additional map to identify the reaction is added.
     * @param revision
     * @return List of all reactions with their parameters
     */
    public List<Map> getLocalParameters(RevisionTransportCommand revision)
    /**
     * Retrieves all reactions in the SBML Model.
     * The returned list contains one map for each reaction with the following keys/values:
     * @li id: the id element
     * @li metaId: The metaId element
     * @li name: The name element
     * @li reversible: boolean indicating whether the reaction is reversible
     * @li sboTerm: The sboTerm if set, @c null otherwise
     * @li reactants: list of all Reactants with a Map for each Reactant, description see below
     * @li products: list of all Products with a Map for each Product, description see below
     * @li modifiers: list of all Modifiers with a Map for each Modifier, description see below
     *
     * The three contained lists reactants, products and modifiers represent a list of species each.
     * Each of the lists can be empty or contain one to many maps with the following keys/values:
     * @li species: the id of the referenced species
     * @li speciesName: The name of the referenced species
     * @li constant: boolean indicating whether the species has a constant value (only for products and reactants)
     * @li stoichiometry: The shoichiometry value (only for products and reactants
     * @param revision
     * @return  List of all reactions
     */
    public List<Map> getReactions(RevisionTransportCommand revision)
    /**
     * Retrieves the Reaction with the given @p id from the SBML Model.
     * The returned Map contains all the elements as explained in @link getReactions
     * with additionally the annotation, notes and math added to the map. The annotation
     * element follows teh description of @link getAnnotations. The math value is the
     * contained MathML of the kinetic law as a String or an empty string in case there
     * is no kinetic law
     * @param revision
     * @param id The id of the Reaction
     * @return Map describing the reaction
     */
    public Map getReaction(RevisionTransportCommand revision, String id)
}
