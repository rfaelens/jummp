/**
 * This file is part of the project bives.jummp, and thus part of the
 * implementation for the diploma thesis "Versioning Concepts and Technologies
 * for Biochemical Simulation Models" by Robert Haelke, Copyright 2010.
 */
package net.biomodels.jummp.dbus.remote

import java.util.Map

import net.biomodels.jummp.dbus.DiffDataDBusAdapter
import net.biomodels.jummp.remote.RemoteDiffDataAdapter
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter

import org.freedesktop.dbus.DBusConnection
import org.springframework.beans.factory.InitializingBean

/**
 * @short DBus implementation for the {@link RemoteDiffDataAdapter}
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
@RemoteDBusAdapter(interfaceName="RemoteDiffDataAdapter",dbusAdapterName="diffDataDBusAdapter")
class RemoteDiffDataAdapterDBusImpl extends AbstractRemoteDBusAdapter implements RemoteDiffDataAdapter, InitializingBean {

	private DiffDataDBusAdapter diffDataDBusAdapter

	public void afterPropertiesSet() throws Exception {
		diffDataDBusAdapter = getRemoteObject("/DiffData", DiffDataDBusAdapter.class)
	}
}