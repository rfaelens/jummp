<%@ page contentType="text/html;charset=UTF-8" %>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.abort.common.title"/></title>
    </head>
    <body>
        <xmp>${inputString}</xmp>
        <p>${maths}</p>
        
        <form action="${createLink(controller: 'model', action: 'mathsTest')}" method="post">
                    	<input title="Enter pharmml maths" type="text" name="maths" value="" size="15" maxlength="5000" class="form-text" />
                    	<input type="submit" value="Submit"/></div>
        </form> 
        
    </body>
    <content tag="submit">
    	selected
    </content>
