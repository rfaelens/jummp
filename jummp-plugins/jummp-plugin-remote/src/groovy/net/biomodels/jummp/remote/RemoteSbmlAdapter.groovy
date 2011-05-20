package net.biomodels.jummp.remote

/**
 * Created by IntelliJ IDEA.
 * User: graessli
 * Date: May 12, 2011
 * Time: 11:23:04 AM
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteSbmlAdapter {
    /**
     * Retrieves the metaId on the Model level. Does not retrieve the metaId of child elements
     * of model such as compartment.
     * @param modelId
     * @param revisionNumber
     * @return MetaId on Model level
     */
    public String getMetaId(long modelId, int revisionNumber)
    /**
     *
     * @param modelId
     * @param revisionNumber
     * @return Version of the SBML file
     */
    public long getVersion(long modelId, int revisionNumber)
    /**
     *
     * @param modelId
     * @param revisionNumber
     * @return Level of the SBML file
     */
    public long getLevel(long modelId, int revisionNumber)
    /**
     * Retrieves the notes element as an xml String on model level.
     * It cannot be used to retrieve notes on another level. If there
     * are no notes an empty string is returned.
     * @param modelId
     * @param revisionNumber
     * @return The notes of the model.
     */
    public String getNotes(long modelId, int revisionNumber)

    /**
     * Retrieves the MIRIAM annotations for the model element.
     * The returned list contains one map for each annotation with the following keys/values:
     * @li qualifier: name for the MIRIAM qualifier as listed in http://sbml.org/Special/Software/JSBML/build/apidocs/org/sbml/jsbml/CVTerm.Qualifier.html
     * @li biologicalQualifier: @c true if the qualifier is a biological qualifier, @c false otherwise
     * @li modelQualifier: @c true if the qualifier is a model qualifier, @c false otherwise
     * @li resources: list of all uris for this annotation
     * @param modelId
     * @param revisionNumber
     * @return List of all MIRIAM annotations of the model element
     */
    public List<Map> getAnnotations(long modelId, int revisionNumber)

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
    public List<Map> getParameters(long modelId, int revisionNumber)
    /**
     * Retrieves the parameter with given @p id from the SBML Model.
     * The returned Map contains all the elements as explained in @link getParameters
     * with additionally the annotation and notes added to the map. The annotation
     * element follows the description of @link getAnnotations.
     * @param modelId
     * @param revisionNumber
     * @param id The unique id in the SBML Model.
     * @return Map of all parameter data
     */
    public Map getParameter(long modelId, int revisionNumber, String id)
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
    public List<Map> getLocalParameters(long modelId, int revisionNumber)
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
     * @param modelId
     * @param revisionNumber
     * @return List of all reactions
     */
    public List<Map> getReactions(long modelId, int revisionNumber)
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
    public Map getReaction(long modelId, int revisionNumber, String id)
    /**
     * Retrieves all events in the SBML Model.
     * The returned list contains one map for each event with the following keys/values:
     * @li id: The id element
     * @li metaId: The metaId element
     * @li name: The name element
     * @li assignments: List of all Assignments for this Event. It contains a map for each Assignment
     * with the following key/value pairs:
     * @li metaId: The metaId element associated with the event assignment
     * @li math: The Content MathML of the assignment as a String
     * @li variableId: The Id of the referenced variable
     * @li variableName: The resolved name of the referenced variable
     * @li variableType: The element name of the referenced variable
     * @param modelId
     * @param revisionNumber
     * @return List of all events
     */
    public List<Map> getEvents(long modelId, int revisionNumber)
    /**
     * Retrieves the Event with the given @p id from the SBML Model.
     * The returned map contains all the elements as explained in @link getEvents
     * with additionally the annotation, notes, sboTerm, trigger and delay. The
     * annotation element follows the description of @link getAnnotations. Trigger
     * and delay contain both either a Content MathML String or an empty String.
     * @param modelId
     * @param revisionNumber
     * @param id The Id of the Event
     * @return Map describing the event
     */
    public Map getEvent(long modelId, int revisionNumber, String id)
    /**
     * Retrieves all rules in the SBML Model.
     * The returned list contains one map for each rule with the following keys/values:
     * @li metaId: The metaId element
     * @li math: The Content MathML of this rule
     * @li type: Either rate, assignment or algebraic. Describes the type of the rule.
     * @li variableId: Id element of referenced variable if present, otherwise @c null
     * @li variableId: Name element of referenced variable if present, otherwise @c null
     * @li variableType: Element name of referenced variable if present, otherwise @c null
     * @param modelId
     * @param revisionNumber
     * @return List of all rules
     */
    public List<Map> getRules(long modelId, int revisionNumber)
    /**
     * Retrieves the Rule for the given referenced @p variable in the SBML Model.
     * The returned map contains all the elements as explained in @link getRules with
     * additionally the annotation and notes string. The annotation element follows
     * the description of @link getAnnotations.
     * @param modelId
     * @param revisionNumber
     * @param variable The id of the referenced Variable
     * @return Map describing the Rule
     */
    public Map getRule(long modelId, int revisionNumber, String variable)
}
