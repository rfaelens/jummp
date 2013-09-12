import grails.util.Environment
import java.util.concurrent.Executors
import org.codehaus.groovy.grails.commons.ApplicationHolder

// Place your Spring DSL code here
beans = {
    xmlns aop: "http://www.springframework.org/schema/aop"
    def grailsApplication = ApplicationHolder.application

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

    resolveMiriamIdentifier(net.biomodels.jummp.core.miriam.ResolveMiriamIdentifierThread) { bean ->
        bean.autowire = "byName"
        bean.factoryMethod = "getInstance"
        bean.scope = "prototype"
    }

    uniProtResolver(net.biomodels.jummp.core.miriam.UniProtResolver) { bean ->
        bean.scope = "prototype"
        dataTypeIdentifier = "MIR:00000005"
        resourceIdentifier = "MIR:00100134"
    }

    taxonomyResolver(net.biomodels.jummp.core.miriam.TaxonomyResolver) { bean ->
        bean.scope = "prototype"
        dataTypeIdentifier = "MIR:00000006"
        resourceIdentifier = "MIR:00100019"
    }

    geneOntologyResolver(net.biomodels.jummp.core.miriam.GeneOntologyResolver) { bean ->
        bean.autowire = "byName"
        bean.scope = "prototype"
        dataTypeIdentifier = "MIR:00000022"
        resourceIdentifier = "MIR:00100012"
    }

    indexingEventListener(net.biomodels.jummp.search.UpdatedRepositoryListener) { bean ->
			bean.autowire = "byName"
			bean.singleton = true
    }
    searchEngine(net.biomodels.jummp.search.SearchProvider) { bean ->
			bean.autowire = "byName"
			bean.singleton = true
    }
    referenceTracker(net.biomodels.jummp.core.ReferenceTracker) { bean ->
			bean.autowire = "byName"
			bean.singleton = true
    }
    modelFileFormatConfig(net.biomodels.jummp.core.ModelFileFormatConfig) { bean ->
			bean.autowire = "byName"
			bean.singleton = true
    }
    ontologyLookupServiceResolver(net.biomodels.jummp.core.miriam.OntologyLookupResolver) { bean ->
        bean.scope = "prototype"
        supportedIdentifiers = [
                "MIR:00000056": "MIR:00100084",
                "MIR:00000067": "MIR:00100097",
                "MIR:00100097": "MIR:00100143",
                "MIR:00000111": "MIR:00100144",
                "MIR:00000112": "MIR:00100145",
                "MIR:00000002": "MIR:00100158"
        ]
    }

    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }

    executorService(  grails.plugin.executor.PersistenceContextExecutorWrapper ) { bean->
        bean.destroyMethod = 'destroy' //keep this destroy method so it can try and clean up nicely
        persistenceInterceptor = ref("persistenceInterceptor")
        executor = Executors.newFixedThreadPool(grailsApplication.config.jummp.threadPool.size)
    }
}
