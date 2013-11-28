package net.biomodels.jummp.plugins.webapp
import net.biomodels.jummp.webapp.ModelController

/* Base class for Update flow tests. Sets the model id in the request
     * params as supplied in the constructor*/
    abstract class UpdateBase extends FlowBase {
        long modelid
        UpdateBase(long m) {
            modelid=m
        }
        protected void setUp() {
           super.setUp()
           mockRequest.setParameter("id",""+modelid)
        }
        def getFlow() {
            new ModelController().updateFlow
        }
    }

