<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${vcs}">
            <div class="errors">
                <g:renderErrors bean="${vcs}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Version Control System</h1>
            <p>Jummp can either use Subversion or Git as the Version Control System for storing model files.</p>
            <g:form name="vcs" action="setup">
                <g:render template="/templates/configuration/vcs"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
