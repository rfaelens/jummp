<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
    </head>
    <body>
        <h1>Welcome to Grails</h1>
        <p>Congratulations, you have successfully started your first Grails application! At the moment
           this is the default page, feel free to modify it to either redirect to a controller or display whatever
           content you may choose. Below is a list of controllers that are currently deployed in this application,
           click on each to execute its default action:
        </p>

        <div id="controller-list" role="navigation">
            <h2>Available Controllers:</h2>
            <ul>
                <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
                    <li class="controller"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
                </g:each>
            </ul>
        </div>
    </body>
    <content tag="sidebar">
        <div class="element">
            <h1>Application Status</h1>
            <h2></h2>
            <p>
                <ul>
                    <li>App version: <g:meta name="app.version"/></li>
                    <li>Grails version: <g:meta name="app.grails.version"/></li>
                    <li>Groovy version: ${org.codehaus.groovy.runtime.InvokerHelper.getVersion()}</li>
                    <li>JVM version: ${System.getProperty('java.version')}</li>
                    <li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
                    <li>Domains: ${grailsApplication.domainClasses.size()}</li>
                    <li>Services: ${grailsApplication.serviceClasses.size()}</li>
                    <li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
                </ul>
            </p>
        </div>
        <div class="element followup">
            <h1>Installed Plugins</h1>
            <h2></h2>
            <p>
                <ul>
                    <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
                        <li>${plugin.name} - ${plugin.version}</li>
                    </g:each>
                </ul>
            </p>
        </div>
    </content>
</html>
