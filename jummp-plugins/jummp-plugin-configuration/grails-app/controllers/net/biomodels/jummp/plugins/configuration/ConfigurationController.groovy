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

    def mysql = {
        render(view: 'configuration', model: [mysql: configurationService.loadMysqlConfiguration(), title: "MySQL", action: "saveMysql", template: 'mysql'])
    }
    
    def saveMysql = { MysqlCommand mysql ->
        if (mysql.hasErrors()) {
            render(view: 'configuration', model: [mysql: mysql, title: "MySQL", action: "saveMysql", template: 'mysql'])
        } else {
            configurationService.saveMysqlConfiguration(mysql)
            render(view: "saved", model: [module: "MySQL"])
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
}
