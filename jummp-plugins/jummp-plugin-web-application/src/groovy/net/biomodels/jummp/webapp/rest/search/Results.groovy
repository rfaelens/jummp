package net.biomodels.jummp.webapp.rest.search

class Results
{
    RequestParameters queryParameters
    List<ModelSummary> models = []
                                public Results(def results)
    {
        results.models.each {
            models.add(new net.biomodels.jummp.webapp.rest.search.ModelSummary(it))
        }
        queryParameters = new RequestParameters(results)
    }
}
