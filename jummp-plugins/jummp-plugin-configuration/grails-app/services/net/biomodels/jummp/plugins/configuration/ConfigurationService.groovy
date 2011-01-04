package net.biomodels.jummp.plugins.configuration

import java.util.concurrent.locks.ReentrantLock
import net.biomodels.jummp.controllers.FirstRunCommand
import net.biomodels.jummp.controllers.LdapCommand
import net.biomodels.jummp.controllers.MysqlCommand
import net.biomodels.jummp.controllers.SvnCommand
import net.biomodels.jummp.controllers.VcsCommand
import org.springframework.beans.factory.InitializingBean

/**
 * Service for managing the configuration stored in a properties file.
 *
 * This service can be used by controllers to update the jummp configuration.
 * It is important to remember that a change in the configuration does not have any
 * influence to the runtime behavior of the currently running application.
 * The application needs to be restarted, whenever the configuration changes.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ConfigurationService implements InitializingBean {
    /**
     * Methods accessing the configuration files, need to be thread save, therefore using a lock.
     */
    private final ReentrantLock lock = new ReentrantLock()

    static transactional = true
    /**
     * The configuration file is set in after properties set to a default value if not already set.
     * Primary purpose of this property is for integration tests to not overwrite the real configuration.
     */
    @SuppressWarnings('GrailsStatelessService')
    private File configurationFile = null

    void afterPropertiesSet() {
        if (!configurationFile) {
            configurationFile = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties")
        }
    }

    /**
     * Rewrites the complete configuration.
     * Web application needs to be restarted after using this method.
     * @param mysql The MySQL configuration
     * @param ldap The LDAP configuration, if @c null database backend is used
     * @param vcs The Version Control System configuration
     * @param svn The Subversion configuration, may be @c null
     * @param firstRun The First run configuration
     */
    public void storeConfiguration(MysqlCommand mysql, LdapCommand ldap, VcsCommand vcs, SvnCommand svn, FirstRunCommand firstRun) {
        Properties properties = new Properties()
        updateMysqlConfiguration(properties, mysql)
        updateLdapConfiguration(properties, ldap)
        updateVcsConfiguration(properties, vcs)
        updateSvnConfiguration(properties, svn)
        updateFirstRunConfiguration(properties, firstRun)
        if (ldap) {
            properties.setProperty("jummp.security.authenticationBackend", "ldap")
        } else {
            properties.setProperty("jummp.security.authenticationBackend", "database")
        }
        // and save - needs to be thread safe
        lock.lock()
        try {
            FileOutputStream out = new FileOutputStream(configurationFile)
            properties.store(out, "Jummp Configuration")
        } finally {
            lock.unlock()
        }
    }

    /**
     * Updates the @p properties with the settings from @p mysql.
     * @param properties The existing properties
     * @param mysql The new mysql settings
     */
    private void updateMysqlConfiguration(Properties properties, MysqlCommand mysql) {
        if (!mysql.validate()) {
            return
        }
        properties.setProperty("jummp.database.server",   mysql.server)
        properties.setProperty("jummp.database.port",     mysql.port.toString())
        properties.setProperty("jummp.database.database", mysql.database)
        properties.setProperty("jummp.database.username", mysql.username)
        properties.setProperty("jummp.database.password", mysql.password)
    }

    /**
     * Updates the @p properties with the settings from @p ldap.
     * If @p ldap is @c null, the LDAP configuration is deleted from the properties
     * @param properties The existing properties
     * @param ldap The new ldap settings, may be @c null
     */
    private void updateLdapConfiguration(Properties properties, LdapCommand ldap) {
        if (ldap && !ldap.validate()) {
            return
        }
        properties.setProperty("jummp.security.ldap.enabled", ldap ? "true" : "false")
        if (ldap) {
            properties.setProperty("jummp.security.ldap.server",         ldap.ldapServer)
            properties.setProperty("jummp.security.ldap.managerDn",      ldap.ldapManagerDn)
            properties.setProperty("jummp.security.ldap.managerPw",      ldap.ldapManagerPassword)
            properties.setProperty("jummp.security.ldap.search.base",    ldap.ldapSearchBase)
            properties.setProperty("jummp.security.ldap.search.filter",  ldap.ldapSearchFilter)
            properties.setProperty("jummp.security.ldap.search.subTree", ldap.ldapSearchSubtree.toString())
        } else {
            properties.remove("jummp.security.ldap.server")
            properties.remove("jummp.security.ldap.managerDn")
            properties.remove("jummp.security.ldap.managerPw")
            properties.remove("jummp.security.ldap.search.base")
            properties.remove("jummp.security.ldap.search.filter")
            properties.remove("jummp.security.ldap.search.subTree")
        }
    }

    /**
     * Updates the @p properties with the settings from @p vcs.
     * @param properties The existing properties
     * @param vcs The new version control system settings
     */
    private void updateVcsConfiguration(Properties properties, VcsCommand vcs) {
        if (!vcs.validate()) {
            return
        }
        properties.setProperty("jummp.vcs.plugin",            vcs.pluginName())
        properties.setProperty("jummp.vcs.exchangeDirectory", vcs.exchangeDirectory)
        properties.setProperty("jummp.vcs.workingDirectory",  vcs.workingDirectory)
    }

    /**
     * Updates the @p properties with the settings from @p svn.
     * If @p svn is @c null, the subversion configuration is deleted from the properties.
     * @param properties The existing properties
     * @param svn The new subversion settings, may be @c null
     */
    private void updateSvnConfiguration(Properties properties, SvnCommand svn) {
        if (svn) {
            if (!svn.validate()) {
                return
            }
            properties.setProperty("jummp.plugins.subversion.localRepository", svn.localRepository)
        } else {
            properties.remove("jummp.plugins.subversion.localRepository")
        }
    }

    /**
     * Updates the @p properties with the settings from @p firstRun.
     * @param properties The existing properties
     * @param firstRun The new first run settings
     */
    private void updateFirstRunConfiguration(Properties properties, FirstRunCommand firstRun) {
        if (!firstRun.validate()) {
            return
        }
        properties.setProperty("jummp.firstRun", firstRun.firstRun)
    }
}
