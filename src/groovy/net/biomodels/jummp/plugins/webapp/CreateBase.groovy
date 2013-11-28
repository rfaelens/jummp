package net.biomodels.jummp.plugins.webapp
import net.biomodels.jummp.webapp.ModelController

/* Base class for the create flows*/
    abstract class CreateBase extends FlowBase {
        def getFlow() { 
            new ModelController().createFlow 
        }
    }

