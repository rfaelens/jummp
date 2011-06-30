import grails.util.Environment

// Place your Spring DSL code here
beans = {
    xmlns aop: "http://www.springframework.org/schema/aop"

    aop.config {
        // intercept all methods annotated with PostLogging annotation
        // and pass it to PostLoggingAdvice
        pointcut(id: "postLoggingPointcut", expression: "@annotation(net.biomodels.jummp.core.events.PostLogging)")
        advisor('pointcut-ref': "postLoggingPointcut", 'advice-ref': "postLogging")
    }
    postLogging(net.biomodels.jummp.core.events.PostLoggingAdvice)

    fetchAnnotations(net.biomodels.jummp.core.miriam.FetchAnnotationsThread) { bean ->
        bean.autowire = "byName"
        bean.factoryMethod = "getInstance"
        bean.scope = "prototype"
    }

    uniProtResolver(net.biomodels.jummp.core.miriam.UniProtResolver) {
        dataTypeIdentifier = "MIR:00000005"
        resourceIdentifier = "MIR:00100134"
    }

    taxonomyResolver(net.biomodels.jummp.core.miriam.TaxonomyResolver) {
        dataTypeIdentifier = "MIR:00000006"
        resourceIdentifier = "MIR:00100019"
    }

    geneOntologyResolver(net.biomodels.jummp.core.miriam.GeneOntologyResolver) {
        dataTypeIdentifier = "MIR:00000022"
        resourceIdentifier = "MIR:00100012"
    }

    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }
}
