package net.biomodels.jummp.plugins.configuration

import grails.plugins.springsecurity.Secured

/**
 * Controller for configuring the application after the first setup.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Secured(["hasRole('ROLE_ADMIN')"])
class ConfigurationController {
    def configurationService

    def index = { }

    def database = {
        render(view: 'configuration', model: [database: configurationService.loadDatabaseConfiguration(), title: "Database", action: "saveDatabase", template: 'database'])
    }

    def saveDatabase = { DatabaseCommand database ->
        if (database.hasErrors()) {
            render(view: 'configuration', model: [database: database, title: "Database", action: "saveDatabase", template: 'database'])
        } else {
            configurationService.saveDatabaseConfiguration(database)
            render(view: "saved", model: [module: "Database"])
        }
    }

    def remote = {
        render(view: 'configuration', model: [remote: configurationService.loadRemoteConfiguration(), title: "Remote", action: "saveRemote", template: 'remote'])
    }

    def saveRemote = { RemoteCommand remote  ->
        if (remote.hasErrors()) {
            render(view: 'configuration', model: [remote: remote, title: "Invoker Service", action: "saveRemote", template: 'remote'])
        } else {
            configurationService.saveRemoteConfiguration(remote)
            render(view: "saved", model: [module: "Remote"])
        }
    }

    def dbus = {
        render(view: 'configuration', model: [dbus: configurationService.loadDBusConfiguration(), title: "DBus", action: "saveDBus", template: 'dbus'])
    }

    def saveDBus = { DBusCommand dbus  ->
        if (remote.hasErrors()) {
            render(view: 'configuration', model: [dbus: remote, title: "DBus", action: "saveDBus", template: 'dbus'])
        } else {
            configurationService.saveDBusConfiguration(dbus)
            render(view: "saved", model: [module: "DBus"])
        }
    }

    def ldap = {
        render(view: 'configuration', model: [ldap: configurationService.loadLdapConfiguration(), title: "LDAP", action: "saveLdap", template: 'ldap'])
    }

    def saveLdap = { LdapCommand ldap ->
        if (ldap.hasErrors()) {
            render(view: 'configuration', model: [ldap: ldap, title: "LDAP", action: "saveLdap", template: 'ldap'])
        } else {
            configurationService.saveLdapConfiguration(ldap)
            render(view: "saved", model: [module: "LDAP"])
        }
    }

    def vcs = {
        render(view: 'configuration', model: [vcs: configurationService.loadVcsConfiguration(), title: "Version Control System", action: "saveVcs", template: 'vcs'])
    }

    def saveVcs = { VcsCommand vcs ->
        if (vcs.hasErrors()) {
            render(view: 'configuration', model: [vcs: vcs, title: "Version Control System", action: "saveVcs", template: 'vcs'])
        } else {
            configurationService.saveVcsConfiguration(vcs)
            render(view: "saved", model: [module: "Version Control System"])
        }
    }

    def svn = {
        render(view: 'configuration', model: [svn: configurationService.loadSvnConfiguration(), title: "Subversion", action: "saveSvn", template: 'svn'])
    }

    def saveSvn = { SvnCommand svn ->
        if (svn.hasErrors()) {
            render(view: 'configuration', model: [svn: svn, title: "Subversion", action: "saveSvn", template: 'svn'])
        } else {
            configurationService.saveSvnConfiguration(svn)
            render(view: "saved", model: [module: "Subversion"])
        }
    }

    def server = {
        render(view: 'configuration', model: [server: configurationService.loadServerConfiguration(), title: "Server", action: "saveServer", template: "server"])
    }

    def saveServer = { ServerCommand server ->
        if (server.hasErrors()) {
            render(view: 'configuration', model: [server: server, title: "Server", action: "saveServer", template: "server"])
        } else {
            configurationService.saveServerConfiguration(server)
            render(view: "saved", model: [module: "Server"])
        }
    }

    def userRegistration = {
        UserRegistrationCommand cmd = configurationService.loadUserRegistrationConfiguration()
        cmd.url = cmd.url.replace("register/validate/{{CODE}}", "")
        if (cmd.activationUrl) {
            cmd.activationUrl = cmd.activationUrl.replace("register/confirmRegistration/{{CODE}}", "")
        }
        render(view: 'configuration', model: [userRegistration: cmd, title: "User Registration", action: "saveUserRegistration", template: "userRegistration"])
    }

    def saveUserRegistration = { UserRegistrationCommand cmd ->
        if (cmd.hasErrors()) {
            render(view: 'configuration', model: [userRegistration: cmd, title: "User Registration", action: "saveUserRegistration", template: "userRegistration"])
        } else {
            configurationService.saveUserRegistrationConfiguration(cmd)
            render(view: "saved", model: [module: "User Registration"])
        }
    }

    def changePassword = {
        ChangePasswordCommand cmd = configurationService.loadChangePasswordConfiguration()
        cmd.url = cmd.url.replace("user/resetPassword/{{CODE}}", "")
        render(view: 'configuration', model: [changePassword: cmd, title: "Change/Reset Password", action: "saveChangePassword", template: "changePassword"])
    }

    def saveChangePassword = { ChangePasswordCommand cmd ->
        if (cmd.hasErrors()) {
            render(view: 'configuration', model: [changePassword: cmd, title: "Change/Reset Password", action: "saveChangePassword", template: "changePassword"])
        } else {
            configurationService.saveChangePasswordConfiguration(cmd)
            render(view: "saved", model: [module: "Change/Reset Password"])
        }
    }

    def trigger = {
        TriggerCommand cmd = configurationService.loadTriggerConfiguration()
        render(view: 'configuration', model: [trigger: cmd, title: "Change Session Remove Intervals", action: "save", template: "trigger"])
    }

    def saveTrigger = { TriggerCommand cmd ->
        if (cmd.hasErrors()) {
            render(view: 'configuration', model: [trigger: cmd, title: "Change Session Remove Intervals", action: "saveTrigger", template: "trigger"])
        } else {
            configurationService.saveTriggerConfiguration(cmd)
            render(view: "saved", model: [module: "Change Session Remove Intervals"])
        }
    }

    def sbml = {
        SBMLCommand cmd = configurationService.loadSBMLConfiguration()
        render(view: 'configuration', model: [sbml: cmd, title: "Enable/Disable SBML Validation", action: "save", template: "sbml"])
    }

    def saveSBML = { SBMLCommand cmd ->
        if (cmd.hasErrors()) {
            render(view: 'configuration', model: [sbml: cmd, title: "Enable/Disable SBML Validation", action: "saveSBML", template: "sbml"])
        } else {
            configurationService.saveSBMLConfiguration(cmd)
            render(view: "saved", model: [module: "Enable/Disable SBML Validation"])
        }
    }

    def bives = {
        BivesCommand cmd = configurationService.loadBivesConfiguration()
        render(view: 'configuration', model: [bives: cmd, title: "Model Versioning System - BiVeS", action: "saveBives", template: "bives"])
    }
    
    def saveBives = { BivesCommand cmd ->
        if (cmd.hasErrors()) {
            render(view: 'configuration', model: [bives: cmd, title: "Model Versioning System - BiVeS", action: "saveBives", template: "bives"])
        } else {
            configurationService.saveBivesConfiguration(cmd)
            render(view: "saved", model: [module: "Model Versioning System - BiVeS"])
        }
    }

    def cms = {
        CmsCommand cmd = configurationService.loadCmsConfiguration()
        render(view: 'configuration', model: [cms: cmd, title: "Content Management System", action: "saveCms", template: "cms"])
    }

    def saveCms = { CmsCommand cmd ->
        if (cmd.hasErrors()) {
            render(view: 'configuration', model: [cms: cmd, title: "Content Management System", action: "saveCms", template: "cms"])
        } else {
            configurationService.saveCmsConfiguration(cmd)
            render(view: "saved", model: [module: "Content Management System"])
        }
    }

    def branding = {
        BrandingCommand cmd = configurationService.loadBrandingConfiguration()
        render(view: 'configuration', model: [branding: cmd, title: "Select Branding", action: "saveBranding", template: "branding"])
    }

    def saveBranding = { BrandingCommand cmd ->
        if (cmd.hasErrors()) {
            render(view: 'configuration', model: [branding: cmd, title: "Select Branding", action: "saveBranding", template: "branding"])
        } else {
            configurationService.saveBrandingConfiguration(cmd)
            render(view: "saved", model: [module: "Select Branding"])
        }
    }
}
