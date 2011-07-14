/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

/**
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
@DBusInterfaceName("net.biomodels.jummp.diffdata")
public interface DiffDataDBusAdapter extends DBusInterface {

	public String generateDiffData(String authenticationHash, long modelId, int predecessorRevision,  int recentRevision);
	
}