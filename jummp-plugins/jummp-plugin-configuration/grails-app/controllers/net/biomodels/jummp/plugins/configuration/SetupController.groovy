package net.biomodels.jummp.plugins.configuration

import net.biomodels.jummp.security.User
import net.biomodels.jummp.security.Role
import net.biomodels.jummp.security.UserRole
import org.springframework.transaction.TransactionStatus

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
        }

        vcs {
            on("next").to("validateVcs")
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
        }

        git {
            on("next").to("firstRun")
        }

        firstRun {
            on("next") { FirstRunCommand cmd ->
                flow.firstRun = cmd
                if (flow.firstRun.hasErrors()) {
                    return error()
                } else {
                    configurationService.storeConfiguration(flow.mysql, (flow.authenticationBackend == "ldap") ? flow.ldap : null, flow.vcs, flow.svn, flow.firstRun)
                    return success()
                }
            }.to("finish")
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

        finish {
        }
    }

    def firstRunFlow = {
        start {
            on("next").to("validateAdmin")
        }

        validateAdmin {
            action {
                User person = new User()
                person.properties = params

                File file = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties")
                Properties props = new Properties()
                props.load(new FileInputStream(file))

                if (Boolean.parseBoolean(props.getProperty("jummp.security.ldap.enabled"))) {
                    // no password for ldap
                    person.password = "*"
                } else {
                    person.password = springSecurityService.encodePassword(params.passwd)
                }
                person.enabled = true
                if (person.validate()) {
                    if (create(person)) {
                        props.setProperty("jummp.firstRun", "false")
                        FileOutputStream out = new FileOutputStream(file)
                        props.store(out, "Jummp Configuration")
                        next()
                    } else {
                        log.debug("Serious error occurred")
                        error()
                    }
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

    private boolean create(User person) {
        boolean ok = true
        User.withTransaction { TransactionStatus status ->

            if (!person.save()) {
                ok = false
                status.setRollbackOnly()
            }
            if (!createRoles(person)) {
                ok = false
                status.setRollbackOnly()
            }
        }
        return ok
    }

    private boolean createRoles(User user) {
        Role adminRole = new Role(authority: "ROLE_ADMIN")
        if (!adminRole.save(flush: true)) {
            return false
        }
        UserRole.create(user, adminRole, true)
        Role userRole = new Role(authority: "ROLE_USER")
        if (!userRole.save(flush: true)) {
            return false
        }
        UserRole.create(user, userRole, true)
        return true
    }
}
