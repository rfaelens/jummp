package net.biomodels.jummp.plugins.pharmml

import net.biomodels.jummp.core.model.RevisionTransportCommand
import eu.ddmore.libpharmml.dom.PharmML

/**
 * Controller for handling Model files in the PharmML format.
 * @author  Raza Ali <raza.ali@ebi.ac.uk>
 * @author  Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMlController {

    def modelDelegateService
    def pharmMlService

    def show = {
        Long modelID = params.id as Long;
        List<RevisionTransportCommand> revs = modelDelegateService.getAllRevisions(modelID)
        final RevisionTransportCommand revision = revs.last()
        PharmML dom = pharmMlService.getDomFromRevision(revision)

        render(view:"/model/pharmml/show", model: [
                revision: revision,
                authors: revision.model.creators,
                allRevs: revs,
                independentVar: pharmMlService.getIndependentVariable(dom),
                symbolDefs: pharmMlService.getSymbolDefinitions(dom),
                variabilityLevel: pharmMlService.getVariabilityLevel(dom),
                covariateModel: pharmMlService.getCovariateModel(dom),
                parameterModel: pharmMlService.getParameterModel(dom),
                trialDesign: pharmMlService.getTrialDesign(revision),
                modellingSteps: pharmMlService.getModellingSteps(revision)
            ]
        )
    }
}
