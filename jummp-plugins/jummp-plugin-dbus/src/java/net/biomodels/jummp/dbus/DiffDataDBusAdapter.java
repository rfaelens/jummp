/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.dbus;

import net.biomodels.jummp.plugins.bives.DiffDataService;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

import de.unirostock.bives.diff.model.Diff;

/**
 * @short DBus adapter for the {@link DiffDataService}
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
@DBusInterfaceName("net.biomodels.jummp.diffdata")
public interface DiffDataDBusAdapter extends DBusInterface {

	/**
	 * Provides the data from a generated diff for the view if present or starts a thread
	 * for the creation of a non-existing diff
	 * @param modelId the id of the model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return a JSON {@link String} containing the {@link Diff} information
	 */
	public String generateDiffData(String authenticationHash, long modelId, int predecessorRevision,  int recentRevision);
	
}