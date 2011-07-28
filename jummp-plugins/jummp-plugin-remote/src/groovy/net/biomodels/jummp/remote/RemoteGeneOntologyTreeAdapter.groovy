package net.biomodels.jummp.remote

import net.biomodels.jummp.core.miriam.GeneOntologyTreeLevel

public interface RemoteGeneOntologyTreeAdapter {
    public GeneOntologyTreeLevel treeLevel(Long id)
}
