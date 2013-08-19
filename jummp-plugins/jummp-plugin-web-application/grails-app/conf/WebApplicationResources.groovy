modules = {
    style {
        resource url: '/css/jummp.less'
    }
    core {
        dependsOn 'jquery'
        resource url: '/js/jquery/jquery.i18n.properties-min-1.0.9.js'
        resource url: '/js/jummp.js'
    }
    dynatree {
        dependsOn 'jquery,jquery-ui'
        resource url: '/js/jquery/dynatree/jquery.cookie.js'
        resource url: '/js/jquery/dynatree/jquery.dynatree.min.js'
    }
    gotree {
        dependsOn 'dynatree, core'
        resource url: '/js/gotree.js'
        resource url: '/js/jquery/jquery.ui.autocomplete.html.js'
    }
    gotreeImages {
        resource url: '/css/dynatree/go_devfrom.gif', disposition: 'inline'
        resource url: '/css/dynatree/go_isa.gif', disposition: 'inline'
        resource url: '/css/dynatree/go_other.gif', disposition: 'inline'
        resource url: '/css/dynatree/go_partof.gif', disposition: 'inline'
    }
    userAdministration {
        dependsOn 'core'
        resource url: '/js/useradministration.js'
    }
    showModels {
        dependsOn 'core'
        resource url: '/js/jquery/jquery.tools.min-1.2.6.js', disposition: 'head'
        resource url: '/js/jquery/jquery.blockUI.js', disposition: 'head'
        resource url: '/js/showmodels.js'
    }
    miriamAdministration {
        dependsOn 'core'
        resource url: '/js/miriamadministration.js'
    }
    branding_style {
        if (grails.util.Holders.getGrailsApplication().config.jummp.branding.style) {
        	resource url:"/css/${grails.util.Holders.getGrailsApplication().config.jummp.branding.style}.less"
        }
    	else {
    		resource url:"/css/${grails.util.Holders.getGrailsApplication().config.jummp.branding.deployment}.less" 
    	}
    }
    jqueryui_latest {
    	resource url:'/js/jquery/jquery-ui-v1.10.3.js'
    }
    ddmore_style {
    	resource url:'/css/ddmorestyle.css'
    }
}
