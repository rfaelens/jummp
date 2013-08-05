<!doctype html>
<html>
<head>
    <g:applyLayout name="${grailsApplication.config.jummp.branding.style}/head"/>
    <r:layoutResources/>
    <g:layoutHead/>
</head>
<body>
    <g:applyLayout name="${grailsApplication.config.jummp.branding.style}/header"/>
    <g:applyLayout name="${grailsApplication.config.jummp.branding.style}/displayOptions"/>
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
    </div>
    <div id="container">
        <div id="loginLogout">
            <sec:ifLoggedIn>
                <jummp:button class="close logout"><g:message code="jummp.main.logout"/></jummp:button>
            </sec:ifLoggedIn>
            <sec:ifNotLoggedIn>
                <jummp:button class="login"><g:message code="jummp.main.login"/></jummp:button>
            </sec:ifNotLoggedIn>
        </div>
    </div>
    <div id="container">
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
    <g:applyLayout name="${grailsApplication.config.jummp.branding.style}/footer"/>
    <r:layoutResources/>
</body>
</html>
