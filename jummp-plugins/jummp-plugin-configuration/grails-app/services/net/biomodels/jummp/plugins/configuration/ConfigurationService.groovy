package net.biomodels.jummp.plugins.configuration

import java.util.concurrent.locks.ReentrantLock
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
     * @param server The Server configuration
     * @param userRegistration the user registration configuration
     * @param changePassword the change/reset password configuration
     */
    public void storeConfiguration(MysqlCommand mysql, LdapCommand ldap, VcsCommand vcs, SvnCommand svn, FirstRunCommand firstRun,
                                   ServerCommand server, UserRegistrationCommand userRegistration, ChangePasswordCommand changePassword) {
        Properties properties = new Properties()
        updateMysqlConfiguration(properties, mysql)
        updateLdapConfiguration(properties, ldap)
        updateVcsConfiguration(properties, vcs)
        updateSvnConfiguration(properties, svn)
        updateFirstRunConfiguration(properties, firstRun)
        updateServerConfiguration(properties, server)
        updateUserRegistrationConfiguration(properties, userRegistration)
        updateChangePasswordConfiguration(properties, changePassword)
        if (ldap) {
            properties.setProperty("jummp.security.authenticationBackend", "ldap")
        } else {
            properties.setProperty("jummp.security.authenticationBackend", "database")
        }
        saveProperties(properties)
    }

    /**
     * Loads the current MySql Configuration.
     * @return A command object encapsulating the current MySql configuration
     */
    public MysqlCommand loadMysqlConfiguration() {
        Properties properties = loadProperties()
        MysqlCommand mysql = new MysqlCommand()
        mysql.server   = properties.getProperty("jummp.database.server")
        mysql.port     = Integer.parseInt(properties.getProperty("jummp.database.port"))
        mysql.database = properties.getProperty("jummp.database.database")
        mysql.username = properties.getProperty("jummp.database.username")
        mysql.password = properties.getProperty("jummp.database.password")
        return mysql
    }

    /**
     * Loads the current LDAP Configuration.
     * @return A command object encapsulating the current LDAP configuration
     */
    public LdapCommand loadLdapConfiguration() {
        Properties properties = loadProperties()
        LdapCommand ldap = new LdapCommand()
        ldap.ldapServer          = properties.getProperty("jummp.security.ldap.server")
        ldap.ldapManagerDn       = properties.getProperty("jummp.security.ldap.managerDn")
        ldap.ldapManagerPassword = properties.getProperty("jummp.security.ldap.managerPw")
        ldap.ldapSearchBase      = properties.getProperty("jummp.security.ldap.search.base")
        ldap.ldapSearchFilter    = properties.getProperty("jummp.security.ldap.search.filter")
        ldap.ldapSearchSubtree   = Boolean.parseBoolean(properties.getProperty("jummp.security.ldap.search.subTree"))
        return ldap
    }

    /**
     * Loads the current Version Control System Configuration.
     * @return A command object encapsulating the current VCS configuration
     */
    public VcsCommand loadVcsConfiguration() {
        Properties properties = loadProperties()
        VcsCommand vcs = new VcsCommand()
        vcs.vcs = properties.getProperty("jummp.vcs.plugin") == "subversion" ? "svn" : "git"
        vcs.exchangeDirectory = properties.getProperty("jummp.vcs.exchangeDirectory")
        vcs.workingDirectory  = properties.getProperty("jummp.vcs.workingDirectory")
        return vcs
    }

    /**
     * Loads the current Subversion Configuration.
     * @return A command object encapsulating the current SVN configuration
     */
    public SvnCommand loadSvnConfiguration() {
        Properties properties = loadProperties()
        SvnCommand svn = new SvnCommand()
        svn.localRepository = properties.getProperty("jummp.plugins.subversion.localRepository")
        return svn
    }

    /**
     * Loads the current Server Configuration.
     * @return A command object encapsulating the current server configuration
     */
    public ServerCommand loadServerConfiguration() {
        Properties properties = loadProperties()
        ServerCommand server = new ServerCommand()
        server.url = properties.getProperty("jummp.server.url")
        return server
    }

    /**
     * Loads the current user registration Configuration
     * @return A command object encapsulating the current user registration configuration
     */
    public UserRegistrationCommand loadUserRegistrationConfiguration() {
        Properties properties = loadProperties()
        UserRegistrationCommand cmd = new UserRegistrationCommand()
        cmd.registration  = Boolean.parseBoolean(properties.getProperty("jummp.security.anonymousRegistration"))
        cmd.sendEmail     = Boolean.parseBoolean(properties.getProperty("jummp.security.registration.email.send"))
        cmd.sendToAdmin   = Boolean.parseBoolean(properties.getProperty("jummp.security.registration.email.sendToAdmin"))
        cmd.subject       = properties.getProperty("jummp.security.registration.email.subject")
        cmd.body          = properties.getProperty("jummp.security.registration.email.body")
        cmd.url           = properties.getProperty("jummp.security.registration.verificationURL")
        cmd.senderAddress = properties.getProperty("jummp.security.registration.email.sender")
        cmd.adminAddress  = properties.getProperty("jummp.security.registration.email.adminAddress")
        cmd.activationBody    = properties.getProperty("jummp.security.activation.email.body")
        cmd.activationSubject = properties.getProperty("jummp.security.activation.email.subject")
        cmd.activationUrl     = properties.getProperty("jummp.security.activation.activationURL")
        return cmd
    }

    /**
     * Loads the current change/reset password Configuration
     * @return A command object encapsulating the current change/reset password configuration
     */
    public ChangePasswordCommand loadChangePasswordConfiguration() {
        Properties properties = loadProperties()
        ChangePasswordCommand cmd = new ChangePasswordCommand()
        cmd.changePassword = Boolean.parseBoolean(properties.getProperty("jummp.security.ui.changePassword"))
        cmd.resetPassword  = Boolean.parseBoolean(properties.getProperty("jummp.security.resetPassword.email.send"))
        cmd.senderAddress  = properties.getProperty("jummp.security.resetPassword.email.sender")
        cmd.subject        = properties.getProperty("jummp.security.resetPassword.email.subject")
        cmd.body           = properties.getProperty("jummp.security.resetPassword.email.body")
        cmd.url            = properties.getProperty("jummp.security.resetPassword.url")
        return cmd
    }

    /**
     * Updates the MySQL configuration stored in the properties file.
     * Other settings are not changed!
     * It is important to remember that the settings will only be activated after
     * a restart of the application!
     * @param mysql The new MySQL configuration
     */
    public void saveMysqlConfiguration(MysqlCommand mysql) {
        Properties properties = loadProperties()
        updateMysqlConfiguration(properties, mysql)
        saveProperties(properties)
    }

    /**
     * Updates the LDAP configuration stored in the properties file.
     * Other settings are not changed!
     * It is important to remember that the settings will only be activated after
     * a restart of the application!
     * @param ldap The new LDAP configuration
     */
    public void saveLdapConfiguration(LdapCommand ldap) {
        Properties properties = loadProperties()
        updateLdapConfiguration(properties, ldap)
        saveProperties(properties)
    }

    /**
     * Updates the Version Control System configuration stored in the properties file.
     * Other settings are not changed!
     * It is important to remember that the settings will only be activated after
     * a restart of the application!
     * @param vcs The new VCS configuration
     */
    public void saveVcsConfiguration(VcsCommand vcs) {
        Properties properties = loadProperties()
        updateVcsConfiguration(properties, vcs)
        saveProperties(properties)
    }

    /**
     * Updates the Subversion configuration stored in the properties file.
     * Other settings are not changed!
     * It is important to remember that the settings will only be activated after
     * a restart of the application!
     * @param svn The new Svn configuration
     */
    public void saveSvnConfiguration(SvnCommand svn) {
        Properties properties = loadProperties()
        updateSvnConfiguration(properties, svn)
        saveProperties(properties)
    }

    /**
     * Updates the Server configuration stored in the properties file.
     * Other settings are not changed!
     * It is important to remember that the settings will only be activated after
     * a restart of the application!
     * @param svn The new Svn configuration
     */
    public void saveServerConfiguration(ServerCommand server) {
        Properties properties = loadProperties()
        updateServerConfiguration(properties, server)
        saveProperties(properties)
    }

    /**
     * Updates the User registration settings stored in the properties file.
     * Other settings are not changed!
     * It is important to remember that the settings will only be activated after
     * a restart of the application!
     * @param cmd The new User Registration settings
     */
    public void saveUserRegistrationConfiguration(UserRegistrationCommand cmd) {
        Properties properties = loadProperties()
        updateUserRegistrationConfiguration(properties, cmd)
        saveProperties(properties)
    }

    /**
     * Updates the change/reset password settings stored in the properties file.
     * Other settings are not changed!
     * It is important to remember that the settings will only be activated after
     * a restart of the application!
     * @param cmd The new change/reset password settings
     */
    public void saveChangePasswordConfiguration(ChangePasswordCommand cmd) {
        Properties properties = loadProperties()
        updateChangePasswordConfiguration(properties, cmd)
        saveProperties(properties)
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

    /**
     * Updates the @p properties with the settings from @p server.
     * @param properties The existing properties
     * @param firstRun The new server settings
     */
    private void updateServerConfiguration(Properties properties, ServerCommand server) {
        if (!server.validate()) {
            return
        }
        properties.setProperty("jummp.server.url", server.url)
    }

    /**
     * Updates the @p properties with the settings from @p cmd
     * @param properties The existing properties
     * @param cmd The new user registration settings
     */
    private void updateUserRegistrationConfiguration(Properties properties, UserRegistrationCommand cmd) {
        if (!cmd.validate()) {
            return
        }
        if (!cmd.url.endsWith("/")) {
            cmd.url = cmd.url + "/"
        }
        cmd.url = cmd.url + "register/validate/{{CODE}}"
        if (!cmd.activationUrl.endsWith("/")) {
            cmd.activationUrl = cmd.activationUrl + "/"
        }
        cmd.activationUrl = cmd.activationUrl + "register/confirmRegistration/{{CODE}}"
        properties.setProperty("jummp.security.anonymousRegistration", cmd.registration ? "true" : "false")
        properties.setProperty("jummp.security.registration.email.send", cmd.sendEmail ? "true" : "false")
        properties.setProperty("jummp.security.registration.email.sendToAdmin", cmd.sendToAdmin ? "true" : "false")
        properties.setProperty("jummp.security.registration.email.sender", cmd.senderAddress)
        properties.setProperty("jummp.security.registration.email.adminAddress", cmd.adminAddress)
        properties.setProperty("jummp.security.registration.email.subject", cmd.subject)
        properties.setProperty("jummp.security.registration.email.body", cmd.body)
        properties.setProperty("jummp.security.registration.verificationURL", cmd.url)
        properties.setProperty("jummp.security.activation.email.body", cmd.activationBody)
        properties.setProperty("jummp.security.activation.email.subject", cmd.activationSubject)
        properties.setProperty("jummp.security.activation.activationURL", cmd.activationUrl)
    }

    /**
     * Updates the @p properties with the settings from @p cmd
     * @param properties The existing properties
     * @param cmd The new change/reset password settings
     */
    private void updateChangePasswordConfiguration(Properties properties, ChangePasswordCommand cmd) {
        if (!cmd.validate()) {
            return
        }
        if (!cmd.url.endsWith("/")) {
            cmd.url = cmd.url + "/"
        }
        cmd.url = cmd.url + "user/resetPassword/{{CODE}}"
        properties.setProperty("jummp.security.ui.changePassword",           cmd.changePassword ? "true" : "false")
        properties.setProperty("jummp.security.resetPassword.email.send",    cmd.resetPassword ? "true" : "false")
        properties.setProperty("jummp.security.resetPassword.email.sender",  cmd.senderAddress)
        properties.setProperty("jummp.security.resetPassword.email.subject", cmd.subject)
        properties.setProperty("jummp.security.resetPassword.email.body",    cmd.body)
        properties.setProperty("jummp.security.resetPassword.url",           cmd.url)
    }

    /**
     * Loads the properties from the configuration file
     * @return The Jummp Configuration Properties
     */
    private Properties loadProperties() {
        Properties properties = new Properties()
        properties.load(new FileInputStream(configurationFile))
        return properties
    }


     /**
     * Stores the @p properties to the configuration file in a thread safe manner.
     * @param properties The new properties
     */
    private void saveProperties(Properties properties) {
        lock.lock()
        try {
            FileOutputStream out = new FileOutputStream(configurationFile)
            properties.store(out, "Jummp Configuration")
        } finally {
            lock.unlock()
        }
    }
}
