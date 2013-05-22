package net.biomodels.jummp.webapp
import net.biomodels.jummp.core.model.ModelVersionTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand

class ModelController {
    /**
     * Dependency injection of modelDelegateService.
     **/
    def modelDelegateService
    /**
     * Dependency injection of sbmlService.
     */
    def sbmlService

    def show = {
        [id: params.id]
    }

    /**
     * File download of the model file for a model by id
     */
    def download = {
        byte[] bytes = modelDelegateService.retrieveModelFile(new ModelVersionTransportCommand(id: params.id as int))
        response.setContentType("application/xml")
        // TODO: set a proper name for the model
        response.setHeader("Content-disposition", "attachment;filename=\"model.xml\"")
        response.outputStream << new ByteArrayInputStream(bytes)
    }

    def model = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        List<String> authors = []
        ver.model.publication?.authors.each {
            authors.add("${it.firstName} ${it.lastName}, ")
        }
        if(!authors.empty) {
            String auth = authors.get(authors.size() - 1)
            authors.remove(authors.get(authors.size() - 1))
            authors.add(authors.size(), auth.substring(0, auth.length() - 2))
        }
        [version: ver, authors: authors]
    }

    /**
     * Display basic information about the model
     */
    def summary = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [publication: modelDelegateService.getPublication(params.id as Long), version: ver, notes: sbmlService.getNotes(ver), annotations: sbmlService.getAnnotations(ver)]
    }

    def overview = {
        ModelVersionTransportCommand rev = modelDelegateService.getLatestVersion(params.id as Long)
        [
                    reactions: sbmlService.getReactions(ver),
                    rules: sbmlService.getRules(ver),
                    parameters: sbmlService.getParameters(ver),
                    compartments: sbmlService.getCompartments(ver)
                ]
    }

    /**
     * Renders html snippet with Publication information for the current Model identified by the id.
     */
    def publication = {
        PublicationTransportCommand publication = modelDelegateService.getPublication(params.id as Long)
        [publication: publication]
    }

    def notes = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [notes: sbmlService.getNotes(rev)]
    }

    /**
     * Retrieve annotations and hand them over to the view
     */
    def annotations = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [annotations: sbmlService.getAnnotations(ver)]
    }

    /**
    * File download of the model file for a model by id
    */
   def downloadModelVersion = {
       ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
       byte[] bytes = modelDelegateService.retrieveModelFile(ver)
       response.setContentType("application/xml")
       // TODO: set a proper name for the model
       response.setHeader("Content-disposition", "attachment;filename=\"model.xml\"")
       response.outputStream << new ByteArrayInputStream(bytes)
   }
}
