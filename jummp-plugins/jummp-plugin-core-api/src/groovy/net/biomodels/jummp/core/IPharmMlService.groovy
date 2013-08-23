package net.biomodels.jummp.core

import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Interface describing the service to access a model encoded in PharmML.
 *
 * An implementation of this interface is provided by the PharmML plugin, however
 * this interface can be used to provide an alternative one.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
interface IPharmMlService {
    public List getModelDefinition(RevisionTransportCommand rev)
    public List getTrialDesign(RevisionTransportCommand rev)
    public List getModellingSteps(RevisionTransportCommand rev)
}
