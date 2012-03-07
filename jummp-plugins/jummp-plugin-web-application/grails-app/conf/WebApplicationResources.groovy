modules = {
    dynatree {
        dependsOn 'jquery,jquery-ui'
        resource url: '/js/jquery/dynatree/jquery.cookie.js'
        resource url: '/js/jquery/dynatree/jquery.dynatree.min.js'
    }
    gotree {
        dependsOn 'dynatree, core'
        resource url: '/js/gotree.js'
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
        dependsOn 'core,overlay'
        resource url: '/js/showmodels.js'
    }
}
