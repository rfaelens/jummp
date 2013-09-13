package net.biomodels.jummp.core
/**
 * @short Class written as a spring-bean singleton to hold modelfileformatservice config data
 * 
 * The class exists to store config required by the modelfileformatservice, which was getting lost
 * after jummp was left running for a long time.
 *
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class ModelFileFormatConfig {
    /**
    * The registered services to handle ModelFormats
    */
    static final Map<String, String> services = new HashMap()

    
    /**
    * The registered plugins to handle ModelFormats visualisations
    */
    static final Map<String, String> controllers = new HashMap()
    
    public Map<String,String> getServices() {
    	    services
    }
    
    public Map<String,String> getControllers() {
    	    controllers
    }

    public void status() {
    	    System.out.println("MODEL FILE FORMAT CONFIG STATUS:")
    	    System.out.println("SERVICES: "+services.inspect())
    	    System.out.println("PLUGINS: "+controllers.inspect())
    }
    
    
}
