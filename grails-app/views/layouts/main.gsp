<!doctype html>
<html>
<head>
    <title><g:layoutTitle default="${g.message(code: 'jummp.main.title') }"/></title>
    <link rel="shortcut icon" href="${g.createLink(uri: '/images/favicon.ico')}"/>
    <less:stylesheet name="jummp"/>
    <less:scripts />
    <r:require module="jquery"/>
    <r:require module="core"/>
    <r:layoutResources/>
    <g:layoutHead/>
    <r:script>
        $.appName = "${grailsApplication.metadata["app.name"]}";
    </r:script>
</head>
<body>
    <div class='modal' id='overlayContainer'><div class="contentWrap"></div></div>
    <div id="topBackground"></div>
    <div id="middleBackground"></div>
    <div id="logo"></div>
    <div id="modeSwitch">
        <!-- TODO: active class has to be set on really selected mode -->
        <jummp:button class="left active"><g:message code="jummp.main.search"/></jummp:button>
        <jummp:button class="right"><g:message code="jummp.main.submit"/></jummp:button>
    </div>
    <div id="container">
        <div id="loginLogout">
            <sec:ifLoggedIn>
                <button class="logout"><g:message code="jummp.main.logout"/></button>
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
