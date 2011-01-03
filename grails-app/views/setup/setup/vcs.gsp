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
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="subversion">Subversion:</label></td>
                                <td class="value ${hasErrors(bean: vcs, field: 'vcs', 'errors')}">
                                    <input type="radio" name="vcs" id="subversion" value="svn" ${!vcs || vcs.vcs == "svn" ? 'checked="checked"' : ''} title="Subversion"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="git">Git:</label></td>
                                <td class="value ${hasErrors(bean: vcs, field: 'vcs', 'errors')}">
                                    <input type="radio" name="vcs" id="git" value="git" ${vcs?.vcs == "git" ? 'checked="checked"' : ''} title="Git"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="workingDirectory" title="Local path to the directory where the checkout is kept. Required in git">Working Directory:</label></td>
                                <td class="value ${hasErrors(bean: vcs, field: 'workingDirectory', 'errors')}">
                                    <input type="text" name="workingDirectory" id="workingDirectory" value="${vcs?.workingDirectory}" title="Local path to the directory where the checkout is kept. Required in git"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="exchangeDirectory">Exchange Directory (Local path to the directory where retrieved files from VCS are saved):</label></td>
                                <td class="value ${hasErrors(bean: vcs, field: 'exchangeDirectory', 'errors')}">
                                    <input type="text" name="exchangeDirectory" id="exchangeDirectory" value="${vcs?.exchangeDirectory}"/>
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
