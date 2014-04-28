import net.biomodels.jummp.plugins.pharmml.PharmMlRenderingService
import net.biomodels.jummp.plugins.pharmml.PharmMl0_2AwareRenderer

beans = {
    pharmMlRenderingService(PharmMlRenderingService) { bean ->
        bean.autowire = 'byName'
    }
}
