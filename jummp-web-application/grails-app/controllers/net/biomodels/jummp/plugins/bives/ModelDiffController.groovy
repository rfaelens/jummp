package net.biomodels.jummp.plugins.bives

import net.biomodels.jummp.core.bives.DiffNotExistingException

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 01.07.2011
 * @year 2011
 */
class ModelDiffController {
    /**
     * Dependency injection of remoteModelService
     */
    def remoteModelService

	def remoteDiffDataService
	
    def index = {
		try {
			[revision: remoteModelService.getRevision(params.id as Long, params.currRev as Integer), modelId:params.id, previousRevision:params.prevRev, currentRevision:params.currRev,
				modifications:remoteDiffDataService.generateDiffData(params.id as Long,
					params.prevRev as Integer, params.currRev as Integer)]
		} catch (DiffNotExistingException e) {
			response.status = 400
			render "Model Diff in creation. Please try again later"
		}
    }
}
