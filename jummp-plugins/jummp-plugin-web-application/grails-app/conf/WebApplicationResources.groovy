/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


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
    jtree {
    	resource url:'/js/jquery.jstree.js'
    }
    ddmore_style {
    	resource url:'/css/ddmore.less'
    }
}
