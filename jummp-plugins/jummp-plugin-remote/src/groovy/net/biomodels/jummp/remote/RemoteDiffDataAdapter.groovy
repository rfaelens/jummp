package net.biomodels.jummp.remote

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 04.07.2011
 * @year 2011
 */
public interface RemoteDiffDataAdapter {

	/**
	 * 
	 * @param modelId
	 * @param previousRevision
	 * @param recentRevision
	 * @return
	 */
	public Map generateDiffData(long modelId, int previousRevision, int recentRevision)

}
