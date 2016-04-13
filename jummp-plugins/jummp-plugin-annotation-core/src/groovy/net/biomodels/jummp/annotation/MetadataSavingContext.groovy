package net.biomodels.jummp.annotation

import net.biomodels.jummp.core.MetadataSavingStrategy
import net.biomodels.jummp.core.annotation.StatementTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand


/**
 * Created by Tung on 12/04/2016.
 */
class MetadataSavingContext {
    private MetadataSavingStrategy strategy;

    public MetadataSavingContext(MetadataSavingStrategy strategy) {
        this.strategy = strategy
    }

    RevisionTransportCommand executeMetadataSavingStrategy(RevisionTransportCommand revisionTransportCommand,
                                          List<StatementTransportCommand> statementTransportCommands) {
        return strategy.marshallAnnotations(revisionTransportCommand, statementTransportCommands)
    }
}
