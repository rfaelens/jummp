/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.dbus

import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod
import net.biomodels.jummp.core.bives.DiffNotExistingException

/**
 * //TODO add description for class DiffDataDBusAdapterImpl.groovy
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
@DBusAdapter(interfaceName="DiffDataDBusAdapter", serviceName="diffDataService")
public class DiffDataDBusAdapterImpl extends AbstractDBusAdapter implements DiffDataDBusAdapter {
	
	def diffDataService
	
	@DBusMethod(isAuthenticate = true, json=true)
	public String generateDiffData(String authenticationHash, long modelId, int predecessorRevision,  int recentRevision) throws DiffNotExistingException {
	}
	
}
