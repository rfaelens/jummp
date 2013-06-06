import static org.junit.Assert.*

import grails.converters.*
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.UserRole
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.web.json.*
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsBootstrap")

/**
 * Provides a mechanism for importing models into JUMMP from a user-specified location.
 *
 * The location of the folder containing all models can be specified as a command-line
 * argument to this script and could be extended in the future to accept an environment
 * variable as an alternative.
 *
 * The other argument this script expects is the path to a file with the following format
 * {username:<username>, password:<unencrypted_password>}
 * These credentials should belong to a user with an active JUMMP account on whose behalf 
 * the submissions will be made.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */

/*
 * The account of the user who is submitting the models
 */
String username
/**
 * The password in plain-text that the user supplies.
 * For added security, this can be specified in a file that is only readable by the file owner.
 */
String password
/**
 * The target directory containing all the models we wish to import.
 */
File modelFolder
/**
 * The directory where all models are stored.
 */
String workingDirectory
/**
 * The location of the exchange directory that is required by the current VcsManager implementation.
 */
String exchangeDirectory
/**
 * The authentication details of the user that is logged in.
 */
def userAuthenticationDetails

target(main: "Puts everything together to import models from a given folder") {
    bootstrap()
    int inputIssues = sanitiseInput()
    if (inputIssues) {
        println "\tERROR There was a problem parsing the input parameters so I'm giving up. Sorry about that."
        return inputIssues
    }
    int configIssues = resetConfiguration()
    if (configIssues) {
        println "\tERROR Your \$HOME/.jummp.properties does not look right. Perhaps the working or exchange folders do not exist?"
        return configIssues
    }
    int vcsIssues = vcsSetup()
    if (vcsIssues) {
        println "\tERROR There was a problem configuring Jummp's model versioning so I'm giving up. Sorry about that."
        return vcsIssues
    }
    // bind a Hibernate Session to avoid lazy initialization exceptions
    TransactionSynchronizationManager.bindResource(appCtx.sessionFactory,
        new SessionHolder(SessionFactoryUtils.getSession(appCtx.sessionFactory, true)))

    int authIssues = authenticate()
    if (authIssues) {
        println "\tERROR Why don't you try again?"
        return authIssues
    }

    def mtc = grailsApp.classLoader.loadClass("net.biomodels.jummp.core.model.ModelTransportCommand")
    def model
    def mftc = grailsApp.classLoader.loadClass("net.biomodels.jummp.core.model.ModelFormatTransportCommand")
    def sbml = mftc.newInstance(identifier:"SBML")
    modelFolder.eachFileRecurse {
        if (it.isFile()) {
            final String MODEL_NAME = it.name - ".xml"
            model = mtc.newInstance(name: MODEL_NAME, submitter: userAuthenticationDetails.principal,
                    submissionDate: new Date(), format: sbml)
            File temp = File.createTempFile("metadata", ".xml")
            def writer = new java.io.FileWriter(temp)
            def xmlWriter = new groovy.xml.MarkupBuilder(writer)
            xmlWriter.model {
                name(MODEL_NAME)
                date(new Date().format("dd-MM-yyyy'T'HH-mm-ss"))
            }
           appCtx.getBean("modelService").uploadModelAsList([it, temp], model)
           FileUtils.deleteQuietly(temp)
        }
    }

    return 0
}

target(sanitiseInput: "Processes user input") {
    // provides argsMap
    parseArguments()
    def modelFolderParameter = argsMap.get("models")
    def credentialsParameter = argsMap.get("credentials")
    File credentials
    if (argsMap.size() != 3 || !modelFolderParameter || !credentialsParameter || argsMap.get("params")) {
        println '''\tUSAGE
\t\tbatch-import --models=<model_folder_location> --credentials=<path_to_credentials_file>'''
        return 1
    }
    File location = new File(modelFolderParameter)
    if (!location.exists() || !location.isDirectory()) {
        println "\tERROR There is no directory that I can access ${location.absolutePath}"
        return 2
    }
    modelFolder = location.getCanonicalFile()
    location = new File(credentialsParameter)
    if (!location.exists() || !location.isFile()) {
        println "\tERROR Did not find any credentials in ${location.absolutePath}"
        return 4
    }
    credentials = location.getCanonicalFile()
    def c = JSON.parse(new FileInputStream(credentials.absolutePath), "UTF8")

    (username, password) = [c.'username', c.'password']
    return 0
}

target(vcsSetup: "Ensures Git is in charge of versioning models") {
    grailsApp.config.jummp.plugins.git.enabled = true
    grailsApp.config.jummp.plugins.svn.enabled = false
    grailsApp.config.jummp.vcs.pluginServiceName="gitManagerFactory"
    appCtx.getBean("vcsService").vcsManager = appCtx.getBean("gitManagerFactory").getInstance()
    assert appCtx.getBean("vcsService").isValid() == true
    return 0
}

def parseJummpConfig = {
    def props = new Properties()
    props.load(new FileInputStream(System.getProperty("user.home")+File.separator+".jummp.properties"))
    new ConfigSlurper().parse(props)
}

target(resetConfiguration: "Resets the key properties to the user-supplied defaults") {
    def jummpConfig = parseJummpConfig()
    workingDirectory = jummpConfig.jummp.vcs.workingDirectory
    def wd = new File(workingDirectory)
    if (!wd.exists() || !wd.isDirectory()) {
        workingDirectory = null
        exchangeDirectory = null
        println "\tERROR Please set jummp.vcs.workingDirectory in .jummp.properties to point to an empty folder"
        return 8
    } else {
        grailsApp.config.jummp.vcs.workingDirectory = workingDirectory
    }
    exchangeDirectory= jummpConfig.jummp.vcs.exchangeDirectory
    def ed = new File(exchangeDirectory)
    if (!ed.exists() || !ed.isDirectory()) {
        workingDirectory = null
        exchangeDirectory = null
        println
            "\tERROR Please set jummp.vcs.exchangeDirectory in .jummp.properties to point to an empty folder"
        return 8
    } else {
        grailsApp.config.jummp.vcs.exchangeDirectory = exchangeDirectory
    }
    return 0
}

target(authenticate: "Attempts to authenticate the user or fails badly") {
    def authToken = new UsernamePasswordAuthenticationToken(username, password)
    def auth = appCtx.getBean("authenticationManager").authenticate(authToken)
    if (!auth.authenticated) {
        println "\tERROR Are you sure that is the right username/password combination for your account?"
        return 16
    }
    SecurityContextHolder.getContext().setAuthentication(auth)
    userAuthenticationDetails = auth
    return 0
}

setDefaultTarget(main)
