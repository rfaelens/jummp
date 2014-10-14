import org.apache.camel.builder.RouteBuilder
class MyMessageRoute extends RouteBuilder {
    def grailsApplication

    @Override
    void configure() {
        def config = grailsApplication?.config

        // example:
        from("seda:input.queue").to("bean:modelDelegateService?method=printMessage")
    	
        
    }
}