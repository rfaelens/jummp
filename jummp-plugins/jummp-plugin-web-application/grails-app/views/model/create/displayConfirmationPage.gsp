<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Confirmation Summary</title>
    </head>
    <body>
        <h1>A star was born</h1>
        <p>Thank you for submitting your model.</p>
        <p>You can access your model <a href="http://${request.serverName}:${request.serverPort}${request.forwardURI.split("create")[0]}model/${session.result_submission}">here</a>
        </p>
    </body>
</html>
