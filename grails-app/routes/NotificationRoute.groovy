import org.apache.camel.builder.RouteBuilder
class NotificationRoute extends RouteBuilder {
    def grailsApplication

    @Override
    void configure() {
        def config = grailsApplication?.config

        // example:
        from("seda:model.publish").to("bean:notificationService?method=modelPublished")
    	from("seda:model.readAccessGranted").to("bean:notificationService?method=readAccessGranted")
    	from("seda:model.writeAccessGranted").to("bean:notificationService?method=writeAccessGranted")
        from("seda:model.delete").to("bean:notificationService?method=delete")
        from("seda:model.update").to("bean:notificationService?method=update")
    	
    }
}