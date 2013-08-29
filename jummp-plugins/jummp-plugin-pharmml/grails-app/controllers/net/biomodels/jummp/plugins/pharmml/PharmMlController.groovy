package net.biomodels.jummp.plugins.pharmml

import net.biomodels.jummp.core.model.RevisionTransportCommand
import eu.ddmore.libpharmml.dom.PharmML
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingSteps
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType

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
        TrialDesignType design = dom?.design
        ModellingSteps steps = pharmMlService.getModellingSteps(revision)

        render(view:"/model/pharmml/show", model: [
                revision: revision,
                authors: revision.model.creators,
                allRevs: revs,
                independentVar: pharmMlService.getIndependentVariable(dom),
                symbolDefs: pharmMlService.getSymbolDefinitions(dom),
                variabilityLevel: pharmMlService.getVariabilityLevel(dom),
                covariateModel: pharmMlService.getCovariateModel(dom),
                parameterModel: pharmMlService.getParameterModel(dom),
                structuralModel: pharmMlService.getStructuralModel(dom),
                observationModel: pharmMlService.getObservationModel(dom),
                treatment: pharmMlService.getTreatment(design),
                treatmentEpoch: pharmMlService.getTreatmentEpoch(design),
                group: pharmMlService.getGroup(design),
                modellingSteps: pharmMlService.getModellingSteps(revision),
                variableDefinitions: pharmMlService.getVariableDefinitions(steps),
                estSimSteps: pharmMlService.getEstimationOrSimulationSteps(steps),
                stepDeps: pharmMlService.getStepDependencies(steps)
            ]
        )
    }
}
