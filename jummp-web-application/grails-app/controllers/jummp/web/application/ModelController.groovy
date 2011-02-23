package jummp.web.application

import grails.converters.JSON
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import grails.plugins.springsecurity.Secured
import org.springframework.web.multipart.MultipartFile
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.PublicationLinkProvider
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Controller providing basic access to Models.
 *
 * This controller communicates with the coreAdapterService to retrieve Models and
 * Model information from the core application.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelController {
    /**
     * Dependency injection of coreAdapterService
     */
    def coreAdapterService

    /**
     * Default action showing a list view
     */
    def index = { }

    def show = {
        ModelTransportCommand model = new ModelTransportCommand(id: params.id as Long)
        RevisionTransportCommand rev = coreAdapterService.getLatestRevision(model)
        [revision: rev]
    }

    def summary = {
        ModelTransportCommand model = new ModelTransportCommand(id: params.id as Long)
        RevisionTransportCommand rev = coreAdapterService.getLatestRevision(model)
        PublicationTransportCommand publication = coreAdapterService.getPublication(model)
        [publication: publication, revision: rev]
    }
    /**
     * AJAX action to get all Models from the core the current user has access to.
     * Returns a JSON data structure for consumption by a jQuery DataTables. 
     */
    def dataTableSource = {
        // input validation
        int start = 0
        int length = 10
        if (params.iDisplayStart) {
            start = params.iDisplayStart as int
        }
        if (params.iDisplayLength) {
            length = Math.min(100, params.iDisplayLength as int)
        }
        def dataToRender = [:]
        dataToRender.sEcho = params.sEcho
        dataToRender.aaData = []

        dataToRender.iTotalRecords = coreAdapterService.getModelCount()
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords

        ModelListSorting sort
        switch (params.iSortCol_0 as int) {
        case 1:
            sort = ModelListSorting.NAME
            break
        case 2:
            sort = ModelListSorting.PUBLICATION
            break
        case 3:
            sort = ModelListSorting.LAST_MODIFIED
            break
        case 4:
            sort = ModelListSorting.FORMAT
            break
        case 0: // id column is the default
        default:
            sort = ModelListSorting.ID
            break
        }
        List models = coreAdapterService.getAllModels(start, length, params.sSortDir_0 == "asc", sort)
        models.each { model ->
            dataToRender.aaData << [model.id, model.name, model.publication, model.lastModifiedDate, model.format.name]
        }
        render dataToRender as JSON
    }

    @Secured('ROLE_USER')
    def upload = {
    }

    @Secured('ROLE_USER')
    def save = { UploadCommand cmd ->
        if (cmd.hasErrors()) {
            Map errors = [error: true]
            if (cmd.errors.getFieldError("model")) {
                errors.put("model", cmd.errors.getFieldError("model").code)
            }
            if (cmd.errors.getFieldError("name")) {
                switch (cmd.errors.getFieldError("name").code) {
                case "blank":
                    errors.put("name", g.message(code: "model.upload.error.name.blank"))
                    break
                default:
                    errors.put("name", g.message(code: "error.unknown", args: ["the Name of the Model"]))
                    break
                }
            }
            if (cmd.errors.getFieldError("comment")) {
                switch (cmd.errors.getFieldError("comment").code) {
                case "blank":
                    errors.put("comment", g.message(code: "model.upload.error.comment.blank"))
                    break
                default:
                    errors.put("comment", g.message(code: "error.unknown", args: ["the Comment"]))
                    break
                }
            }
            if (cmd.errors.getFieldError("pubmed")) {
                switch (cmd.errors.getFieldError("pubmed").code) {
                case "validator.invalid":
                    errors.put("pubmed", g.message(code: "model.upload.error.pubmed.blank"))
                    break
                case "typeMismatch":
                    errors.put("pubmed", g.message(code: "model.upload.error.pubmed.numeric"))
                    break
                default:
                    errors.put("pubmed", g.message(code: "error.unknown", args: ["PubMed ID"]))
                    break
                }
            }
            if (cmd.errors.getFieldError("doi")) {
                switch (cmd.errors.getFieldError("doi").code) {
                case "validator.invalid":
                    errors.put("doi", g.message(code: "model.upload.error.doi.blank"))
                    break
                default:
                    errors.put("doi", g.message(code: "error.unknown", args: ["DOI"]))
                    break
                }
            }
            if (cmd.errors.getFieldError("url")) {
                switch (cmd.errors.getFieldError("url").code) {
                case "validator.invalid":
                    errors.put("url", g.message(code: "model.upload.error.url.blank"))
                    break
                case "url.invalid":
                    errors.put("url", g.message(code: "model.upload.error.url.invalid"))
                    break
                default:
                    errors.put("doi", g.message(code: "error.unknown", args: ["Publication URL"]))
                    break
                }
            }
            errors.put("publicationType", cmd.errors.getFieldError("publicationType")?.code)
            // need to wrap JSON in a textarea to work with iframe used by jquery form plugin
            render "<textarea>" + (errors as JSON) + "</textarea>"
        } else {
            try {
                ModelTransportCommand model = coreAdapterService.uploadModel(cmd.model.bytes, cmd.toModelCommand())
                render "<textarea>" + ([success: true, model: model] as JSON) + "</textarea>"
            } catch (ModelException e) {
                Map errors = [error: true]
                errors.put("model", e.getMessage())
                render "<textarea>" + (errors as JSON) + "</textarea>"
            }
        }
    }

    /**
     * File download of the model file for a model by id
     */
    def download = {
        byte[] bytes = coreAdapterService.retrieveModelFile(new ModelTransportCommand(id: params.id as int))
        response.setContentType("application/xml")
        // TODO: set a proper name for the model
        response.setHeader("Content-disposition", "attachment;filename=\"model.xml\"")
        response.outputStream << new ByteArrayInputStream(bytes)
    }
}

/**
 * Command Object used by save action
 */
class UploadCommand implements Serializable {
    MultipartFile model
    String name
    String comment
    String publicationType
    Integer pubmed
    String doi
    String url

    static constraints = {
        model(nullable: false)
        name(nullable: false, blank: false)
        comment(nullable: false, blank: false)
        publicationType(nullable: false, inList: ["PUBMED", "DOI", "URL", "UNPUBLISHED"])
        pubmed(nullable: true, validator: { pubmed, cmd ->
            return (cmd.publicationType == "PUBMED" && pubmed != null) || cmd.publicationType != "PUBMED"
        })
        doi(nullable: true, validator: { doi, cmd ->
            return (cmd.publicationType == "DOI" && doi != null) || cmd.publicationType != "DOI"
        })
        url(nullable: true, url: true, validator: { url, cmd ->
            return (cmd.publicationType == "URL" && url != null) || cmd.publicationType != "URL"
        })
    }

    ModelTransportCommand toModelCommand() {
        PublicationTransportCommand publication = new PublicationTransportCommand()
        boolean populatePublicationData = false
        switch (publicationType) {
        case "PUBMED":
            publication.linkProvider = PublicationLinkProvider.PUBMED
            publication.link = pubmed.toString()
            break
        case "DOI":
            publication.linkProvider = PublicationLinkProvider.DOI
            publication.link = doi
            populatePublicationData = true
            break
        case "URL":
            publication.linkProvider = PublicationLinkProvider.URL
            publication.link = url
            populatePublicationData = true
            break
        case "UNPUBLISHED": // same as default
        default:
            publication = null
            break
        }
        if (populatePublicationData) {
            // TODO: populate the publication data
        }
        return new ModelTransportCommand(name: name,
                format: new ModelFormatTransportCommand(identifier: "SBML"),
                comment: comment,
                publication: publication)
    }
}
