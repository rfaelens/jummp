/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


/**
 * This script allows to start a core server in testing mode.
 * This means nothing is written to database and 3 test users are created
 * and can be used from other components.
 *
 * This script makes it possible to test other components connected through JMS.
 * But the testing semantics changes by using this approach. An executed test does
 * not reset the database, test start to depend on each other.
 *
 * Note: This script is no longer useful due to the the fact that we create a dedicated
 * folder, which is managed by a version control system, for each model. 
 *
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
    depends(checkVersion, configureProxy, packageApp, parseArguments, bootstrapOnce)
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
    config.jummp.vcs.pluginServiceName="gitManagerFactory"
    appCtx.getBean("vcsService").vcsManager = appCtx.getBean("gitManagerFactory").getInstance()
}

setDefaultTarget(main)
