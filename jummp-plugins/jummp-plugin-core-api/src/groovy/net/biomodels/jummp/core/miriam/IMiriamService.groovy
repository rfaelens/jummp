package net.biomodels.jummp.core.miriam

/**
 * @short Interface for MIRIAM service.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
public interface IMiriamService {
    /**
     * Updates the MIRIAM Resources in the database from the XML specified in @p url.
     * @param url The URL to the MIRIAM Resource XML
     * @param force If @c true previously fetched data will be discarded, if @c false only new entries are added
     * @throws MiriamUpdateException In case an error occurs while downloading or parsing the XML
     */
    public void updateMiriamResources(String url, boolean force) throws MiriamUpdateException

    /**
     * Returns all relevant MIRIAM data in one map for the given @p resource, that is a complete URN
     * consisting of the MIRIAM datatype plus the element identifier within the datatype
     * The map consists of the following elements:
     * @li <strong>dataTypeLocation:</strong> URL for the datatype
     * @li <strong>dataTypeName:</strong> Human readable name of the datatype
     * @li <strong>name:</strong> Human readable name of the identifier (if it could be resolved) or the identifier
     * @li <strong>url:</strong> URL to the identifier
     *
     * If the datatype is unknown or no MIRIAM resource could be located for the datatype an empty map is returned.
     * @param urn The URN consisting of both MIRIAM datatype and identifier
     * @return Map as described above
     */
    public Map miriamData(String urn)

    /**
     * Tries to fetch all the MIRIAM data for each of the given @p urns and stores them in the database, if not
     * already present in the database
     * @param urns List of URNs to be resolved
     */
    public void fetchMiriamData(List<String> urns)
}
