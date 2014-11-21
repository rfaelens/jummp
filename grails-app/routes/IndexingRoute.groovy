import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Exchange
import org.apache.camel.Processor

class IndexingRoute extends RouteBuilder {
    
    @Override
    void configure() {
    	from("direct:exec")
        .setHeader("CamelExecCommandArgs", simple('-jar ${body[jarPath]} ${body[jsonPath]}'))
        .to("exec:java")
        .process(new Processor() {
        	public void process(Exchange exchange) throws Exception {
        		String indexerOutput = exchange.getIn().getBody(String.class);
        		System.out.println(indexerOutput);
        		// do something with the word count
        	}
        });
    	
    }
}