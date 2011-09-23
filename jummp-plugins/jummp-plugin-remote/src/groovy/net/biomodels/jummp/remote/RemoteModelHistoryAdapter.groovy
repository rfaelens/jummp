package net.biomodels.jummp.remote

import net.biomodels.jummp.core.model.ModelTransportCommand

public interface RemoteModelHistoryAdapter {
    public List<ModelTransportCommand> history()
    public ModelTransportCommand lastAccessedModel()
}
