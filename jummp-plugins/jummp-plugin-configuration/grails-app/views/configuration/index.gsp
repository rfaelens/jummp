<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Configuration</title>
    </head>
    <body>
        <div id="remote" class="body">
            <h1>Configuration</h1>
            <p>Select the module to configure:
                <ul>
                    <li><a href="${createLink(action: 'mysql')}">MySQL</a></li>
                    <li><a href="${createLink(action: 'ldap')}">LDAP</a></li>
                    <li><a href="${createLink(action: 'svn')}">Subversion</a></li>
                    <li><a href="${createLink(action: 'vcs')}">Version Control System</a></li>
                    <li><a href="${createLink(action: 'remote')}">Remote</a></li>
                    <li><a href="${createLink(action: 'server')}">Server</a></li>
                    <li><a href="${createLink(action: 'userRegistration')}">User Registration</a></li>
                    <li><a href="${createLink(action: 'changePassword')}">Change/Reset Password</a></li>
                </ul>
            </p>
        </div>
    </body>
</html>