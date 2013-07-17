<!doctype html>
<html>
<head>
    <title><g:layoutTitle default="${g.message(code: 'jummp.main.title') }"/></title>
    <link rel="shortcut icon" href="${g.createLink(uri: '/images/favicon.ico')}"/>
    <r:script>
        $.appName = "${grailsApplication.metadata["app.name"]}";
    </r:script>
    <r:require module="style_${grailsApplication.config.jummp.branding.deployment}"/>
    <r:require module="core"/>
    <r:layoutResources/>
    <g:layoutHead/>
</head>
<body>
    <div class='modal' id='overlayContainer'><div class="contentWrap"></div></div>
    <div id="topBackground"></div>
    <div id="middleBackground"></div>
    <div id="logo"></div>
    <div id="modeSwitch">
        <!-- TODO: active class has to be set on really selected mode -->
        <a href="${g.createLink(controller: 'search', action: 'list')}"><jummp:button class="left"><g:message code="jummp.main.search"/></jummp:button></a>
        <a href="${g.createLink(controller: 'model', action: 'create')}"><jummp:button class="right"><g:message code="jummp.main.submit"/></jummp:button></a>
    </div>
    <div id="container">
        <div id='overlayTable'>
            <div id='overlayTableRow'>
                <div id="loginLogout">
                    <sec:ifLoggedIn>
                        <jummp:button class="close logout"><g:message code="jummp.main.logout"/></jummp:button>
                    </sec:ifLoggedIn>
                    <sec:ifNotLoggedIn>
                        <jummp:button class="login"><g:message code="jummp.main.login"/></jummp:button>
                    </sec:ifNotLoggedIn>
                </div>
            </div>
        </div>
        <div id="loginLogout">
            <sec:ifLoggedIn>
                <jummp:button class="close logout"><g:message code="jummp.main.logout"/></jummp:button>
            </sec:ifLoggedIn>
            <sec:ifNotLoggedIn>
                <jummp:button class="login"><g:message code="jummp.main.login"/></jummp:button>
            </sec:ifNotLoggedIn>
        </div>
        <div id="nav">
            <!-- TODO: active class has to be set on really selected mode -->
            <div class="left ${pageProperty(name: 'body.activetab') ? (pageProperty(name: 'body.activetab') == 'about' ? 'active' : '') : 'active'}"><a href="${g.createLink(uri: '/')}"><g:message code="jummp.main.tabs.about"/></a></div>
            <div class="right ${pageProperty(name: 'body.activetab') ? (pageProperty(name: 'body.activetab') == 'search' ? 'active' : '') : ''}"><a href="${g.createLink(controller: 'search', action: 'list')}"><g:message code="jummp.main.tabs.search"/></a></div>
        </div>
        <wcm:render path="branding"/>
        <div id="contentContainer">
            <div id="socialMedia">
            <!-- TODO: integrate social media button or use area for other part -->
            </div>
            <div id="content">
                <div id="main">
                    <g:pageProperty name="page.main-content" />
                    <div id="infoBox"></div>
                    <g:layoutBody/>
                </div>
                <div id="sideBar">
                    <g:pageProperty name="page.sidebar"/>
                </div>
            </div>
            <wcm:render path="links"/>
        </div>
    </div>
    <wcm:render path="footer"/>
    <r:layoutResources/>
</body>
</html>
