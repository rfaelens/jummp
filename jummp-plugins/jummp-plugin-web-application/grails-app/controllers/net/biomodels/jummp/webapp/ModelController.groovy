package net.biomodels.jummp.webapp
import net.biomodels.jummp.core.model.RevisionTransportCommand

class ModelController {
    /**
     * Dependency injection of modelDelegateService.
     **/
    def modelDelegateService

    def show = {
        [id: params.id]
    }

    /**
     * File download of the model file for a model by id
     */
    def download = {
        byte[] bytes = modelDelegateService.retrieveModelFile(new RevisionTransportCommand(id: params.id as int))
        response.setContentType("application/xml")
        // TODO: set a proper name for the model
        response.setHeader("Content-disposition", "attachment;filename=\"model.xml\"")
        response.outputStream << new ByteArrayInputStream(bytes)
    }
}
