package net.biomodels.jummp.plugins.bives

import java.lang.reflect.UndeclaredThrowableException;

import net.biomodels.jummp.core.bives.DiffNotExistingException;


/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 01.07.2011
 * @year 2011
 */
class ModelDiffController {

	def remoteDiffDataService
	
    def index = {
		try {
			[modelId:params.id, previousRevision:params.prevRev, currentRevision:params.currRev,
				modifications:remoteDiffDataService.generateDiffData(params.id as Long,
					params.prevRev as Integer, params.currRev as Integer)]
		} catch (DiffNotExistingException e) {
			render {
				div(id: "diffError", style: "display: none")
			}
		} catch (Exception e) {
			e.printStackTrace()
			render {
				div(id: "diffError", style: "display: none")
			}
		}
    }
}