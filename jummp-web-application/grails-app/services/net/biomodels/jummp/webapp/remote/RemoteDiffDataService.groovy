package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteDiffDataAdapter
import net.biomodels.jummp.remote.RemoteSbmlAdapter
import net.biomodels.jummp.webapp.ast.RemoteService
/**
 * 
 * //TODO add description for class RemoteDiffService.groovy
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 04.07.2011
 * @year 2011
 */
@RemoteService("RemoteDiffDataAdapter")
class RemoteDiffDataService implements RemoteDiffDataAdapter {

    static transactional = true
	RemoteDiffDataAdapter remoteDiffDataAdapter
}