<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${dbus}">
            <div class="errors">
                <g:renderErrors bean="${dbus}"/>
            </div>
        </g:hasErrors>
        <div id="dbus" class="body">
            <h1>DBus Settings</h1>
            <g:form name="firstRun" action="setup">
                <g:render template="/templates/configuration/dbus"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
