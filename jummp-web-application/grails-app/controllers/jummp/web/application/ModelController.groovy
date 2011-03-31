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
import net.biomodels.jummp.core.model.AuthorTransportCommand

/**
 * @short Controller providing basic access to Models.
 *
 * This controller communicates with the remoteModelService to retrieve Models and
 * Model information from the core application.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelController {
    /**
     * Dependency injection of remoteModelService
     */
    def remoteModelService
    /**
     * Dependency injection of springSecurityService
     */
    def springSecurityService

    /**
     * Default action showing a list view
     */
    def index = {
        if (!springSecurityService.isAjax(request)) {
            render(template: "/templates/page", model: [link: g.createLink(action: "index"), callback: "loadModelListCallback"])
            return
        }
    }

    def show = {
        if (!springSecurityService.isAjax(request)) {
            render(template: "/templates/page", model: [link: g.createLink(action: "show", id: params.id), callback: "loadModelTabCallback"])
            return
        }
        ModelTransportCommand model = new ModelTransportCommand(id: params.id as Long)
        RevisionTransportCommand rev = remoteModelService.getLatestRevision(model)
        [revision: rev, addRevision: remoteModelService.canAddRevision(model)]
    }

    def summary = {
        if (!springSecurityService.isAjax(request)) {
            render(template: "/templates/page", model: [link: g.createLink(action: "show", id: params.id), callback: "loadModelTabCallback"])
            return
        }
        ModelTransportCommand model = new ModelTransportCommand(id: params.id as Long)
        RevisionTransportCommand rev = remoteModelService.getLatestRevision(model)
        [publication: remoteModelService.getPublication(model), revision: rev]
    }

    /**
     * View for uploading a new Model Revision.
     */
    def newRevision = {
        if (!springSecurityService.isAjax(request)) {
            render(template: "/templates/page", model: [link: g.createLink(action: "show", id: params.id), callback: "loadModelTabCallback", data: "#modelTabs-addRevision"])
            return
        }
        // TODO: verify that user has write access to the Model
        [params: params]
    }

    /**
     * Renders html snippet with Publication information for the current Model identified by the id.
     */
    def publication = {
        ModelTransportCommand model = new ModelTransportCommand(id: params.id as Long)
        PublicationTransportCommand publication = remoteModelService.getPublication(model)
        render(template: "/templates/publication", model: [publication: publication])
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

        dataToRender.iTotalRecords = remoteModelService.getModelCount()
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
        List models = remoteModelService.getAllModels(start, length, params.sSortDir_0 == "asc", sort)
        models.each { model ->
            Map publication = [:]
            if (model.publication) {
                publication.put("link", model.publication.link)
                publication.put("linkProvider", model.publication.linkProvider.toString())
                publication.put("compactTitle", jummp.compactPublicationTitle(publication: model.publication))
            }
            dataToRender.aaData << [model.id, model.name, publication, model.lastModifiedDate, model.format.name]
        }
        render dataToRender as JSON
    }

    @Secured('ROLE_USER')
    def upload = {
        if (!springSecurityService.isAjax(request)) {
            render(template: "/templates/page", model: [link: g.createLink(action: "upload"), callback: "loadUploadModelCallback"])
            return
        }
    }

    @Secured('ROLE_USER')
    def save = { UploadCommand cmd ->
        if (cmd.hasErrors()) {
            Map errors = [error: true]
            if (cmd.errors.getFieldError("model")) {
                errors.put("model", g.message(code: "model.upload.error.file"))
            }
            errors.put("name",    resolveErrorMessage(cmd, "name",    "the Name of the Model"))
            errors.put("comment", resolveErrorMessage(cmd, "comment", "the Comment"))
            errors.put("pubmed",  resolveErrorMessage(cmd, "pubmed",  "PubMed ID"))
            errors.put("doi",     resolveErrorMessage(cmd, "doi",     "DOI"))
            errors.put("url",     resolveErrorMessage(cmd, "url",     "Publication URL"))
            errors.put("publicationType", cmd.errors.getFieldError("publicationType")?.code)
            // publications
            errors.put("publicationTitle", resolveErrorMessage(cmd, "publicationTitle", "Publication Title"))
            errors.put("publicationJournal", resolveErrorMessage(cmd, "publicationJournal", "Publication Journal"))
            errors.put("publicationAffiliation", resolveErrorMessage(cmd, "publicationAffiliation", "Publication Affiliation"))
            errors.put("publicationAbstract", resolveErrorMessage(cmd, "publicationAbstract", "Publication Abstract"))
            errors.put("publicationYear", resolveErrorMessage(cmd, "publicationYear", "Publication Year"))
            errors.put("publicationMonth", resolveErrorMessage(cmd, "publicationMonth", "Publication Month"))
            errors.put("publicationDay", resolveErrorMessage(cmd, "publicationDay", "Publication Day"))
            errors.put("authorInitials", resolveErrorMessage(cmd, "authorInitials", "Initials"))
            errors.put("authorFirstName", resolveErrorMessage(cmd, "authorFirstName", "First Name"))
            errors.put("authorLastName", resolveErrorMessage(cmd, "authorLastName", "Last Name"))
            // need to wrap JSON in a textarea to work with iframe used by jquery form plugin
            render "<textarea>" + (errors as JSON) + "</textarea>"
        } else {
            try {
                ModelTransportCommand uploadModel = cmd.toModelCommand()
                for (int i=0; i<(params.authorCount as int); i++) {
                    String initialsField = "authorInitials" + i
                    String firstNameField = "authorFirstName" + i
                    String lastNameField = "authorLastName" + i
                    if (params.containsKey(initialsField) && params.containsKey(firstNameField) && params.containsKey(lastNameField)) {
                        AuthorTransportCommand author = new AuthorTransportCommand(initials: params.get(initialsField),
                                firstName: params.get(firstNameField),
                                lastName: params.get(lastNameField))
                        println author.lastName
                        if (author.lastName != "") {
                            uploadModel.publication.authors << author
                        }
                    }
                }
                ModelTransportCommand model = remoteModelService.uploadModel(cmd.model.bytes, uploadModel)
                render "<textarea>" + ([success: true, model: model] as JSON) + "</textarea>"
            } catch (ModelException e) {
                Map errors = [error: true]
                errors.put("model", e.getMessage())
                render "<textarea>" + (errors as JSON) + "</textarea>"
            }
        }
    }

    /**
     * Action for uploading a new Model Revision.
     * The security is with access control, so there is no need to have an @Secured annotation.
     */
    def saveNewRevision = { RevisionUploadCommand cmd ->
        if (cmd.hasErrors()) {
            Map errors = [error: true]
            if (cmd.errors.getFieldError("model")) {
                errors.put("model", g.message(code: "model.upload.error.file"))
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
            // need to wrap JSON in a textarea to work with iframe used by jquery form plugin
            render "<textarea>" + (errors as JSON) + "</textarea>"
        } else {
            try {
                ModelTransportCommand model = new ModelTransportCommand(id: cmd.modelId)
                RevisionTransportCommand revision = remoteModelService.addRevision(model, cmd.model.bytes, new ModelFormatTransportCommand(identifier: "SBML"), cmd.comment)
                render "<textarea>" + ([success: true, revision: revision] as JSON) + "</textarea>"
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
        byte[] bytes = remoteModelService.retrieveModelFile(new ModelTransportCommand(id: params.id as int))
        response.setContentType("application/xml")
        // TODO: set a proper name for the model
        response.setHeader("Content-disposition", "attachment;filename=\"model.xml\"")
        response.outputStream << new ByteArrayInputStream(bytes)
    }
    /**
     * File download of the model file for a model by id
     */
    def downloadModelRevision = {
        byte[] bytes = remoteModelService.retrieveModelFile(new RevisionTransportCommand(id: params.id as int))
        response.setContentType("application/xml")
        // TODO: set a proper name for the model
        response.setHeader("Content-disposition", "attachment;filename=\"model.xml\"")
        response.outputStream << new ByteArrayInputStream(bytes)
    }

    /**
     * Resolves the error message for a field error
     * @param cmd The UploadCommand for resolving the errors
     * @param field The field to be tested
     * @param description A descriptive name of the field to be passed to unknown errors
     * @return The resolved error message or @c null if there is no error
     */
    private String resolveErrorMessage(UploadCommand cmd, String field, String description) {
        if (cmd.errors.getFieldError(field)) {
            switch (cmd.errors.getFieldError(field).code) {
            case "blank":
                return g.message(code: "model.upload.error.${field}.blank")
            case "validator.invalid":
                return g.message(code: "model.upload.error.${field}.blank")
            case "url.invalid":
                return g.message(code: "model.upload.error.${field}.invalid")
            case "typeMismatch":
                return g.message(code: "model.upload.error.${field}.numeric")
            case "range.toobig":
                return g.message(code: "model.upload.error.${field}.range")
            case "range.toosmall":
                return g.message(code: "model.upload.error.${field}.range")
            case "not.inList":
                return g.message(code: "model.upload.error.${field}.inList")
            case "maxSize.exceeded":
                return  g.message(code: "model.upload.error.${field}.maxSize")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }
}

/**
 * Command Object used by save action
 */
class UploadCommand implements Serializable {
    private static final long serialVersionUID = 1L
    MultipartFile model
    String name
    String comment
    String publicationType
    Integer pubmed
    String doi
    String url
    // publication
    String publicationTitle
    String publicationJournal
    Integer publicationIssue
    Integer publicationVolume
    String publicationPages
    String publicationAffiliation
    String publicationAbstract
    // publication date
    Integer publicationYear
    String publicationMonth
    Integer publicationDay
    String authorInitials
    String authorFirstName
    String authorLastName

    static constraints = {
        model(nullable: false,
                validator: { model ->
                    return !model.isEmpty()
                })
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
        // publication
        publicationTitle(nullable: true, validator: { publicationTitle, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return publicationTitle != null && publicationTitle.size() > 0
            } else {
                return true
            }
        })
        publicationJournal(nullable: true, validator: { publicationJournal, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return publicationJournal != null && publicationJournal.size() > 0
            } else {
                return true
            }
        })
        publicationAffiliation(nullable: true, validator: { publicationAffiliation, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return publicationAffiliation != null && publicationAffiliation.size() > 0
            } else {
                return true
            }
        })
        publicationAbstract(nullable: true, maxSize: 1000, validator: { publicationAbstract, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return publicationAbstract != null && publicationAbstract.size() > 0
            } else {
                return true
            }
        })
        publicationIssue(nullable: true)
        publicationVolume(nullable: true)
        publicationPages(nullable: true)
        publicationYear(nullable: true, range: 1980..(new GregorianCalendar().get(Calendar.YEAR)),
                validator: { publicationYear, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return publicationYear != null
            } else {
                return true
            }
        })
        publicationMonth(nullable: true, inList: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"])
        publicationDay(nullable: true, range: 1..31, validator: { publicationDay, cmd ->
            if (cmd.publicationYear && cmd.publicationMonth && publicationDay) {
                int month
                switch (cmd.publicationMonth) {
                case "Jan":
                    month = 0
                    break
                case "Feb":
                    month = 1
                    break
                case "Mar":
                    month = 2
                    break
                case "Apr":
                    month = 3
                    break
                case "May":
                    month = 4
                    break
                case "Jun":
                    month = 5
                    break
                case "Jul":
                    month = 6
                    break
                case "Aug":
                    month = 7
                    break
                case "Sep":
                    month = 8
                    break
                case "Oct":
                    month = 9
                    break
                case "Nov":
                    month = 10
                    break
                case "Dec":
                    month = 11
                    break
                default:
                    // incorrect month value
                    return false
                }
                GregorianCalendar cal = new GregorianCalendar(cmd.publicationYear, month, 1)
                return publicationDay >= 1 && publicationDay <= cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            } else if (cmd.publicationYear && !cmd.publicationMonth && publicationDay) {
                return false
            } else {
                return true
            }
        })
        authorInitials(nullable: true, validator: { authorInitials, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return authorInitials != null && authorInitials.size() > 0
            } else {
                return true
            }
        })
        authorFirstName(nullable: true, validator: { authorFirstName, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return authorFirstName != null && authorFirstName.size() > 0
            } else {
                return true
            }
        })
        authorLastName(nullable: true, validator: { authorLastName, cmd ->
            if (cmd.publicationType == "DOI" || cmd.publicationType == "URL") {
                return authorLastName != null && authorLastName.size() > 0
            } else {
                return true
            }
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
            publication.affiliation = publicationAffiliation
            publication.title       = publicationTitle
            publication.journal     = publicationJournal
            publication.synopsis    = publicationAbstract
            publication.issue       = publicationIssue
            publication.volume      = publicationVolume
            publication.pages       = publicationPages
            publication.year        = publicationYear
            publication.month       = publicationMonth
            publication.day         = publicationDay
            AuthorTransportCommand author = new AuthorTransportCommand(initials: authorInitials, firstName: authorFirstName, lastName: authorLastName)
            publication.authors = []
            publication.authors << author
        }
        return new ModelTransportCommand(name: name,
                format: new ModelFormatTransportCommand(identifier: "SBML"),
                comment: comment,
                publication: publication)
    }
}

/**
 * Command Object used by saveNewRevision action
 */
class RevisionUploadCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long modelId
    MultipartFile model
    String comment

    static constraints = {
        modelId(nullable: false)
        model(nullable: false,
                validator: { model ->
                    return !model.isEmpty()
                })
        comment(nullable: false, blank: false)
    }
}
