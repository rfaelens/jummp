package net.biomodels.jummp.plugins.configuration

import net.biomodels.jummp.core.UserCommand

/**
 * @short Controller to bootstrap the configuration of an application instance.
 *
 * The controller consists of two webflows:
 * @li @c setupFlow to configure database and authentication
 * @li @c firstRunFlow to create the admin user after the database is configured
 *
 * The configuration is stored into the file @c ~/.jummp.properties and used by
 * the Grails config.
 * The SetupFilters takes care to redirect all requests to the currently required
 * flow and blocks all access to this controller as soon as the application instance
 * is configured.
 * @see net.biomodels.jummp.filters.SetupFilters
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SetupController {
    def configurationService
    def springSecurityService
    def userService

    def index = {
        redirect(action: "setup")
    }

    def setupFlow = {
        start {
            on("next") { MysqlCommand cmd ->
                flow.mysql = cmd
                if (flow.mysql.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("authenticationBackend")
        }

        authenticationBackend {
            on("next").to("validateAuthenticationBackend")
            on("back").to("start")
        }

        ldap {
            on("next") { LdapCommand cmd ->
                flow.ldap = cmd
                if (flow.ldap.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("vcs")
            on("back").to("authenticationBackend")
        }
        vcs {
            on("next").to("validateVcs")
            on("back").to("decideBackFromVcs")
        }

        svn {
            on("next") { SvnCommand cmd ->
                flow.svn = cmd
                if (flow.svn.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("firstRun")
            on("back").to("vcs")
        }

        git {
            on("next").to("firstRun")
            on("back").to("vcs")
        }

        firstRun {
            on("next") { FirstRunCommand cmd ->
                flow.firstRun = cmd
                if (flow.firstRun.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("userRegistration")
            on("back").to("decideBackFromFirstRun")
        }

        userRegistration {
            on("next") { UserRegistrationCommand cmd ->
                flow.userRegistration = cmd
                if (flow.userRegistration.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("changePassword")
            on("back").to("firstRun")
        }

        changePassword {
            on("next") { ChangePasswordCommand cmd ->
                flow.changePassword = cmd
                if (flow.changePassword.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("remoteExport")
            on("back").to("userRegistration")
        }

        remoteExport {
            on("next") { RemoteCommand cmd ->
                flow.remote = cmd
                if (flow.remote.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("validateRemote")
            on("back").to("changePassword")
        }

        remoteRemote {
            on("next") { RemoteCommand cmd ->
                cmd.jummpExportDbus = flow.remote.jummpExportDbus
                cmd.jummpExportJms = flow.remote.jummpExportJms
                flow.remote = cmd
                flow.remote.validate()
                if (flow.remote.hasErrors()) {
                    return error()
                } else  {
                    return success()
                }
            }.to("validateDBus")
            on("back").to("remoteExport")
        }

        dbus {
            on("next") { DBusCommand cmd ->
                flow.dbus = cmd
                if(flow.dbus.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("server")
            on("back").to("remoteRemote")
        }

        server {
            on("next") { ServerCommand cmd ->
                flow.server = cmd
                if (flow.server.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("trigger")
            on("back").to("remoteExport")
        }

        trigger {
            on("next") { TriggerCommand cmd ->
                flow.trigger = cmd
                if (flow.trigger.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("sbml")
            on("back").to("server")
        }

        sbml {
            on("next") { SBMLCommand cmd ->
                flow.sbml = cmd
                if (flow.sbml.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("bives")
            on("back").to("trigger")
        }

        bives {
            on("next") { BivesCommand cmd ->
                flow.bives = cmd
                if (flow.bives.hasErrors()) {
                    return error()
                } else {
                    configurationService.storeConfiguration(flow.mysql, (flow.authenticationBackend == "ldap") ? flow.ldap : null, flow.vcs, flow.svn, flow.firstRun, flow.server, flow.userRegistration, flow.changePassword, flow.remote, flow.dbus, flow.trigger, flow.sbml, flow.bives)
                    return success()
                }
            }.to("finish")
            on("back").to("sbml")
        }

        validateAuthenticationBackend {
            action {
                if (params.authenticationBackend == "database") {
                    flow.authenticationBackend = "database"
                    database()
                } else if (params.authenticationBackend == "ldap") {
                    flow.authenticationBackend = "ldap"
                    ldap()
                } else {
                    error()
                }
            }
            on("database").to("vcs")
            on("ldap").to("ldap")
            on("error").to("authenticationBackend")
        }

        validateRemote {
            action {
                if (flow.remote.jummpExportDbus && flow.remote.jummpExportJms) {
                    remote()
                } else if(!flow.remote.jummpExportJms) {
                    flow.remote.jummpRemote = "dbus"
                    dbus()
                }
                else {
                    flow.remote.jummpRemote = "jms"
                    server()
                }
            }
            on("remote").to("remoteRemote")
            on("dbus").to("dbus")
            on("server").to("server")
        }

        validateDBus {
            action {
                if(params.jummpRemote == "dbus") {
                    dbus()
                } else {
                    server()
                }
            }
            on("dbus").to("dbus")
            on("server").to("server")
        }

        validateVcs {
            action { VcsCommand cmd ->
                flow.vcs = cmd
                if (flow.vcs.hasErrors()) {
                    error()
                } else if (flow.vcs.isGit()) {
                    git()
                } else if (flow.vcs.isSvn()) {
                    svn()
                }
            }
            on("svn").to("svn")
            on("git").to("git")
            on("error").to("vcs")
        }

        decideBackFromVcs {
            action {
                if (flow.authenticationBackend == "ldap") {
                    ldap()
                } else {
                    authenticationBackend()
                }
            }
            on("ldap").to("ldap")
            on("authenticationBackend").to("authenticationBackend")
        }

        decideBackFromFirstRun {
            action {
                if (flow.vcs.isGit()) {
                    git()
                } else if (flow.vcs.isSvn()) {
                    svn()
                } else {
                    // just for safety
                    error()
                }
            }
            on("git").to("git")
            on("svn").to("svn")
            on("error").to("firstRun")
        }

        finish {
        }
    }

    def firstRunFlow = {
            start {
                on("next") { UserCommand cmd ->
                flow.user = cmd
                if (flow.user.hasErrors()) {
                    return error()
                } else {
                    return success()
                }
            }.to("validateAdmin")
        }

        validateAdmin {
            action {
                if (userService.createAdmin(flow.user)) {
                    Properties props = new Properties()
                    File file = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties")
                    props.load(new FileInputStream(file))
                    props.setProperty("jummp.firstRun", "false")
                    FileOutputStream out = new FileOutputStream(file)
                    props.store(out, "Jummp Configuration")
                    next()
                } else {
                    error()
                }
            }
            on("error").to("start")
            on("next").to("finish")
        }

        finish {
        }
    }
}
