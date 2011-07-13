package net.biomodels.jummp.plugins.bives

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 01.07.2011
 * @year 2011
 */
class ModelDiffController {

	def remoteDiffDataService
	
    def index = {
		[modelId:params.id, previousRevision:params.prevRev, currentRevision:params.currRev,
			modifications:remoteDiffDataService.generateDiffData(params.id as Long,
				params.prevRev as Integer, params.currRev as Integer)]
    }
}