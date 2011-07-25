package net.biomodels.jummp.remote

import net.biomodels.jummp.core.bives.DiffNotExistingException

/**
 * @short Interface describing how to access the DiffDataService
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 04.07.2011
 * @year 2011
 */
public interface RemoteDiffDataAdapter {

	/**
	 * Provides the data from a generated diff for the view if present or starts a thread
	 * for the creation of a non-existing diff
	 * @param modelId the id of the model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return a Map containing the different types of changes extracted from the diff
	 */
	public Map generateDiffData(long modelId, int previousRevision, int recentRevision) throws DiffNotExistingException

}
