package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.remote.RemoteJummpApplicationAdapter
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.Authentication
import net.biomodels.jummp.core.JummpException
import org.freedesktop.dbus.DBusConnection
import net.biomodels.jummp.dbus.ApplicationDBusAdapter
import org.springframework.beans.factory.InitializingBean
import org.perf4j.aop.Profiled
import net.biomodels.jummp.core.user.JummpAuthentication
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter

/**
 * @short DBus Remote Adapter to JummpApplication.
 *
 * This bean connects itself to the DBus and retrieves the exported Application object
 * and delegates calls to it.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@RemoteDBusAdapter(interfaceName="RemoteJummpApplicationAdapter",dbusAdapterName="applicationDBusAdapter")
class RemoteJummpApplicationAdapterDBusImpl extends AbstractRemoteDBusAdapter implements RemoteJummpApplicationAdapter, InitializingBean {
    private DBusConnection connection
    private ApplicationDBusAdapter applicationDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection =  DBusConnection.getConnection(DBusConnection.SYSTEM)
        applicationDBusAdapter = (ApplicationDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/Application", ApplicationDBusAdapter.class)
    }

    @Profiled(tag="RemoteJummpApplicationAdapterDBusImpl.authenticate")
    public Authentication authenticate(Authentication authentication) throws AuthenticationException, JummpException {
        return applicationDBusAdapter.authenticate((String)authentication.principal, (String)authentication.credentials)
    }

    @Profiled(tag="RemoteJummpApplicationAdapterDBusImpl.getJummpConfig")
    public ConfigObject getJummpConfig(String appToken) {
        Map<String, String> flatten = applicationDBusAdapter.getJummpConfig()
        ConfigObject config = new ConfigObject()
        flatten.each { key, value ->
            List<String> parts = key.tokenize('.')
            def configObject = config
            parts.eachWithIndex { it, i ->
                if (i == parts.size() - 1) {
                    if (value == "true" || value == "false") {
                        configObject."${it}" = Boolean.parseBoolean(value)
                    } else {
                        configObject."${it}" = value
                    }
                } else {
                    configObject = configObject.getProperty(it)
                }
            }
        }
        return config
    }

    @Profiled(tag="RemoteJummpApplicationAdapterDBusImpl.isAuthenticated")
    boolean isAuthenticated(Authentication authentication) {
        return applicationDBusAdapter.isAuthenticated(((JummpAuthentication)authentication).getAuthenticationHash())
    }
}
