package net.biomodels.jummp.core

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
