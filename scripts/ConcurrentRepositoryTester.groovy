/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* groovy, Apache Commons, Spring Framework, Grails (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of groovy, Apache Commons, Spring Framework, Grails used as well as
* that of the covered work.}
**/





import grails.converters.*
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.plugins.security.UserRole
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.web.json.*
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsBootstrap")

target(main: "The description of the script goes here!") {


    
    // TODO: Implement script here
    bootstrap()
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

    def syncClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.util.interprocess.InterJummpSync")
    
    def sync=syncClass.newInstance()
    
    sync.start(true, 50000)
    
    File modelFolder=new File("target/vcs/modelfolder")
    File sbmlModel=File.createTempFile("model", ".xml")
    sbmlModel.setText('''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
Add a comment to this line
      </reaction>
    </listOfReactions>
  </model>
</sbml>''')

    String rev=appCtx.getBean("vcsService").vcsManager.updateModel(modelFolder, [sbmlModel], null)
    
    
    sync.sendMessage("DoneStartup")
    
    def repTest= grailsApp.
    			classLoader.
    			loadClass("net.biomodels.jummp.util.interprocess.UpdateAndTest").
    			newInstance()

    repTest.init(new File("target/vcs/exchange"), 100)
    repTest.setModel(modelFolder)
    repTest.setManager(appCtx.getBean("vcsService").vcsManager)
    repTest.run()
    sync.sendMessage("TestResult: ${repTest.dotestsPass()}")
    sync.sendMessage("Errors: ${repTest.getAllErrors()}")
    sync.sendMessage("DoneTesting")
    sync.waitForMessage("donetesting")
    Thread.sleep(2000)
    
    //sync.terminate()
 
}

target(vcsSetup: "Ensures Git is in charge of versioning models") {
    File exchange=new File("target/vcs/exchange")
    exchange.mkdirs()        
    grailsApp.config.jummp.vcs.pluginServiceName="gitManagerFactory"
    appCtx.getBean("vcsService").vcsManager = appCtx.getBean("gitManagerFactory").getInstance()
    appCtx.getBean("vcsService").vcsManager.init(exchange)
    assert appCtx.getBean("vcsService").isValid() == true
    return 0
}

def parseJummpConfig = {
    def props = new Properties()
    def service = appCtx.getBean("configurationService")
    String pathToConfig=service.getConfigFilePath()
   	if (!pathToConfig) {
       		throw new Exception("No config file available.")
   	}
    props.load(new FileInputStream(pathToConfig))
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
    def authToken = new UsernamePasswordAuthenticationToken("testuser", "secret")
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
