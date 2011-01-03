package net.biomodels.jummp.controllers

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
    def springSecurityService

    def index = {
        redirect(action: "setup")
    }

    def setupFlow = {
        start {
            on("next").to("validateDatabase")
        }

        authenticationBackend {
            on("next").to("validateAuthenticationBackend")
        }

        ldap {
            on("next").to("validateLdap")
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
            on("next").to("validateFirstRun")
        }

        validateDatabase {
            action {
                flow.mysqlUsername = params.mysqlUsername
                flow.mysqlPassword = params.mysqlPassword
                flow.mysqlServer   = params.mysqlServer
                flow.mysqlPort     = params.mysqlPort
                flow.mysqlDatabase = params.mysqlDatabase
                next()
            }
            on("next").to("authenticationBackend")
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
                    // for safety - use database
                    database()
                }
            }
            on("database").to("vcs")
            on("ldap").to("ldap")
        }

        validateLdap {
            action {
                flow.ldapServer          = params.ldapServer
                flow.ldapManagerDn       = params.ldapManagerDn
                flow.ldapManagerPassword = params.ldapManagerPassword
                flow.ldapSearchBase      = params.ldapSearchBase
                flow.ldapSearchFilter    = params.ldapSearchFilter
                flow.ldapSearchSubtree   = params.ldapSearchSubtree ? "true" : "false"
                next()
            }
            on("next").to("vcs")
        }

        validateVcs {
            action {
                flow.vcsExchangeDirectory = params.exchangeDirectory
                flow.vcsWorkingDirectory = params.workingDirectory
                if (params.vcs == "svn") {
                    flow.vcs = "subversion"
                    svn()
                } else if (params.vcs == "git") {
                    if (!params.workingDirectory) {
                        error()
                    } else {
                        flow.vcs = "git"
                        git()
                    }
                } else {
                    error()
                }
            }
            on("svn").to("svn")
            on("git").to("git")
            on("error").to("vcs")
        }

        validateFirstRun {
            action {
                flow.firstRun = params.firstRun
                next()
            }
            on("next").to("save")
        }

        save {
            action {
                Properties props = new Properties()
                props.setProperty("jummp.database.server", flow.mysqlServer)
                props.setProperty("jummp.database.port", flow.mysqlPort)
                props.setProperty("jummp.database.database", flow.mysqlDatabase)
                props.setProperty("jummp.database.username", flow.mysqlUsername)
                props.setProperty("jummp.database.password", flow.mysqlPassword)
                props.setProperty("jummp.security.authenticationBackend", flow.authenticationBackend)
                if (flow.authenticationBackend == "ldap") {
                    props.setProperty("jummp.security.ldap.enabled", "true")
                    props.setProperty("jummp.security.ldap.server", flow.ldapServer)
                    props.setProperty("jummp.security.ldap.managerDn", flow.ldapManagerDn)
                    props.setProperty("jummp.security.ldap.managerPw", flow.ldapManagerPassword)
                    props.setProperty("jummp.security.ldap.search.base", flow.ldapSearchBase)
                    props.setProperty("jummp.security.ldap.search.filter", flow.ldapSearchFilter)
                    props.setProperty("jummp.security.ldap.search.subTree", flow.ldapSearchSubtree)
                } else {
                    props.setProperty("jummp.security.ldap.enabled", "false")
                }
                props.setProperty("jummp.firstRun", flow.firstRun)
                props.setProperty("jummp.vcs.plugin", flow.vcs)
                props.setProperty("jummp.vcs.exchangeDirectory", flow.vcsExchangeDirectory)
                props.setProperty("jummp.vcs.workingDirectory", flow.vcsWorkingDirectory)
                switch (flow.vcs) {
                case "subversion":
                    props.setProperty("jummp.plugins.subversion.localRepository", flow.svn.localRepository)
                    break
                case "git":
                    break
                default:
                    // should never happen
                    break
                }
                File file = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties")
                FileOutputStream out = new FileOutputStream(file)
                props.store(out, "Jummp Configuration")
                next()
            }
            on("next").to("finish")
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

/**
 * Command Object for validating subversion settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SvnCommand implements Serializable {
    String localRepository

    static constraints = {
        localRepository(blank: false,
                        validator: { repository, cmd ->
                            File directory = new File((String)repository)
                            // TODO: test whether the directory is an svn repository
                            return (directory.exists() && directory.isDirectory())
                        })
    }
}
