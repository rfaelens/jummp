<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <div id="remote" class="body">
            <h1>Version Control System</h1>
            <p>Jummp can either use Subversion or Git as the Version Control System for storing model files.</p>
            <g:form name="vcs" action="setup">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="subversion">Subversion:</label></td>
                                <td class="value"><input type="radio" name="vcs" id="subversion" value="svn" checked="checked"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="git">Git:</label></td>
                                <td class="value"><input type="radio" name="vcs" id="git" value="git"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="workingDirectory">Working Directory (Local path to the directory where the checkout is kept. Required in git):</label></td>
                                <td class="value"><input type="text" name="workingDirectory" id="workingDirectory"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="exchangeDirectory">Exchange Directory (Local path to the directory where retrieved files from VCS are saved):</label></td>
                                <td class="value"><input type="text" name="exchangeDirectory" id="exchangeDirectory"/></td>
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
