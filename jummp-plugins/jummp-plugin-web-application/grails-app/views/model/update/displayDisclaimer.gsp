<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Submit Model</title>
    </head>
    <body>
        <h1>Submission Guidelines</h1>
        <p>This will modify model ${params.id}. Are you sure?</p>
        <g:form>
            <div class="dialog">
                <div class="buttons">
                    <g:submitButton name="Cancel" value="Abort" />
                    <g:submitButton name="Continue" value="Continue" />
                </div>
            </div>
        </g:form>
    </body>
</html>
