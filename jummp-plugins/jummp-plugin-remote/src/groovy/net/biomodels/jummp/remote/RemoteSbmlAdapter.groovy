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

}