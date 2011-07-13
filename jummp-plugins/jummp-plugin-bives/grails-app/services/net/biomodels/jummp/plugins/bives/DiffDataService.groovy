package net.biomodels.jummp.plugins.bives

import org.codehaus.groovy.grails.commons.ApplicationHolder
import net.biomodels.jummp.plugins.bives.DiffDataProvider

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
public class DiffDataService {

    static transactional = true

    Map generateDiffData(long modelId, int previousRevision, int recentRevision) {
		DiffDataProvider diffData = ApplicationHolder.application.mainContext.getBean("diffDataProvider") as DiffDataProvider
		if(!diffData.getDiffInformation(modelId, previousRevision, recentRevision)) {
			return [:]
		}
		return [moves: diffData.moves, updates: diffData.updates, inserts: diffData.inserts, deletes: diffData.deletes]
    }
}
