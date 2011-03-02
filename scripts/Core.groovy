/**
 * This script allows to start a core server in testing mode as a backend for the web-application.
 * This means nothing is written to database, 3 test users are created and some models are created.
 *
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
// this script needs to be run in the test environment
scriptEnv = "test"
includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsRun")
includeTargets << grailsScript("TestCore")

target(main: "Runs a core application with a few models") {
    depends(checkVersion, configureProxy, packageApp, parseArguments, bootstrap)
    // bind a Hibernate Session to avoid lazy initialization exceptions
    TransactionSynchronizationManager.bindResource(appCtx.sessionFactory,
        new SessionHolder(SessionFactoryUtils.getSession(appCtx.sessionFactory, true)))
    createUsers()
    setupVcs()
    models()
    // and execute
    watchContext()
}

target(models: "Creates some models to be used in the application") {
    def authenticationManager = appCtx.getBean("authenticationManager")
    Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
    SecurityContextHolder.context.authentication = auth
    def modelService = appCtx.getBean("modelService")
    File modelFile = File.createTempFile("model", null)
    modelFile.append('''<?xml version="1.0" encoding="UTF-8"?>
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
      </reaction>
    </listOfReactions>
  </model>
</sbml>''')
    def modelTransportCommandClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.core.model.ModelTransportCommand")
    def modelFormatClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.core.model.ModelFormatTransportCommand")
    def publicationTransportCommandClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.core.model.PublicationTransportCommand")
    def publicationLinkProviderClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.core.model.PublicationLinkProvider")
    def publication = publicationTransportCommandClass.newInstance(link: "20488988", linkProvider: publicationLinkProviderClass.PUBMED)
    def doiPublication = publicationTransportCommandClass.newInstance(link: "10.1016/S0006-3495(61)86902-6", linkProvider: publicationLinkProviderClass.DOI, journal: "Journal", title: "Title", affiliation: "Affiliation", synopsis: "")
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "1st model", publication: publication))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "2nd model", publication: doiPublication))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "3rd model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "4th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "5th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "6th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "7th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "8th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "9th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "10th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "11th model"))
    modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "12th model"))
    def model = modelService.uploadModel(modelFile, modelTransportCommandClass.newInstance(format: modelFormatClass.newInstance(identifier: "SBML"), comment: "Test", name: "13th model"))
    def userClass = grailsApp.classLoader.loadClass("net.biomodels.jummp.plugins.security.User")
    modelService.grantReadAccess(model, userClass.findByUsername("user"))
}

setDefaultTarget(main)
