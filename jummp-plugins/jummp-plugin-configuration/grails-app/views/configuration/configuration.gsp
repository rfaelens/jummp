<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Configuration - ${title}</title>
    </head>
    <body>
        <g:hasErrors>
            <div class="errors">
                <g:renderErrors/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Configuration - ${title}</h1>
            <g:form action="${action}">
                <g:render template="/templates/configuration/${template}"/>
                <div class="buttons">
                    <jummp:button id="cancelButton">Cancel</jummp:button>
                    <jummp:button id="submitButton">Save</jummp:button>
                </div>
            </g:form>
        </div>
        <g:javascript>
$("#cancelButton").click(function() {
    $("form")[0].reset();
});
$("#submitButton").click(function() {
    $("form")[0].submit();
});
        </g:javascript>
    </body>
    <g:render template="/templates/configuration/configurationSidebar"/>
</html>
