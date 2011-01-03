<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${svn}">
            <div class="errors">
                <g:renderErrors bean="${svn}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Version Control System - Subversion</h1>
            <p>The Subversion backend only provides checking out from a local repository.</p>
            <g:form name="svn" action="setup">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="localRepository" title="The repository to checkout from and commit to">Local Repository Path:</label></td>
                                <td class="value ${hasErrors(bean: svn, field: 'localRepository', 'errors')}">
                                    <input type="text" name="localRepository" id="localRepository" value="${svn?.localRepository}" title="Local Repository Path - The repository to checkout from and commit to"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
