/**
 * This script allows to start a core server in testing mode.
 * This means nothing is written to database and 3 test users are created
 * and can be used from other components.
 *
 * This script makes it possible to test other components connected through JMS.
 * But the testing semantics changes by using this approach. An executed test does
 * not reset the database, test start to depend on each other.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.api.Git
import org.apache.commons.io.FileUtils
// this script needs to be run in the test environment
scriptEnv = "test"

includeTargets << grailsScript('_GrailsBootstrap')
includeTargets << grailsScript("_GrailsRun")

target(main: "Bootstraps a test server with some test users") {
    depends(checkVersion, configureProxy, packageApp, parseArguments, bootstrap)
    // bind a Hibernate Session to avoid lazy initialization exceptions
    TransactionSynchronizationManager.bindResource(appCtx.sessionFactory,
        new SessionHolder(SessionFactoryUtils.getSession(appCtx.sessionFactory, true)))
    createUsers()
    setupVcs()
    // and execute
    watchContext()
}

target(createUsers: "Creates some test users") {
    def userClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.plugins.security.User")
    def roleClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.plugins.security.Role")
    def userRoleClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.plugins.security.UserRole")
    def encoderClass = grailsApp.classLoader.loadClass("org.springframework.security.authentication.encoding.ShaPasswordEncoder")
    def aclSidClass = grailsApp.classLoader.loadClass("org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid")
    def encoder = encoderClass.newInstance(256)
    def user = userClass.newInstance(username: "testuser",
            password: encoder.encodePassword("secret", null),
            userRealName: "Test",
            email: "test@test.com",
            enabled: true,
            accountExpired: false,
            accountLocked: false,
            passwordExpired: false)
    user.save()
    aclSidClass.newInstance(sid: user.username, principal: true).save(flush: true)
    def user2 = userClass.newInstance(username: "user",
            password: encoder.encodePassword("verysecret", null),
            userRealName: "Test2",
            email: "test2@test.com",
            enabled: true,
            accountExpired: false,
            accountLocked: false,
            passwordExpired: false)
    user2.save()
    aclSidClass.newInstance(sid: user2.username, principal: true).save(flush: true)
    def admin = userClass.newInstance(username: "admin",
            password: encoder.encodePassword("1234", null),
            userRealName: "Administrator",
            email: "admin@test.com",
            enabled: true,
            accountExpired: false,
            accountLocked: false,
            passwordExpired: false)
    admin.save()
    aclSidClass.newInstance(sid: admin.username, principal: true).save(flush: true)
    def userRole = roleClass.newInstance(authority: "ROLE_USER")
    userRole.save()
    userRoleClass.create(user, userRole, false)
    userRoleClass.create(user2, userRole, false)
    userRoleClass.create(admin, userRole, false)
    def adminRole = roleClass.newInstance(authority: "ROLE_ADMIN")
    adminRole.save()
    userRoleClass.create(admin, adminRole, false)
}

target(setupVcs: "Creates a VCS repository") {
    File clone = new File("target/vcs/git")
    FileUtils.deleteDirectory(clone)
    clone.mkdirs()
    FileRepositoryBuilder builder = new FileRepositoryBuilder()
    Repository repository = builder.setWorkTree(clone)
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir() // scan up the file system tree
    .build()
    Git git = new Git(repository)
    git.init().setDirectory(clone).call()
    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
    config.jummp.plugins.git.enabled = true
    config.jummp.plugins.svn.enabled = false
    config.jummp.vcs.workingDirectory="target/vcs/git"
    config.jummp.vcs.exchangeDirectory="target/vcs/exchange"
    config.jummp.vcs.pluginServiceName="gitService"
    appCtx.getBean("vcsService").vcsManager = appCtx.getBean("gitService").getInstance()
}

setDefaultTarget(main)
