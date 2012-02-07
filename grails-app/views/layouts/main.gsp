<!doctype html>
<html>
<head>
    <title><g:layoutTitle default="${g.message(code: 'jummp.main.title') }"/></title>
    <link rel="shortcut icon" href="${g.createLink(uri: '/images/favicon.ico')}"/>
    <less:stylesheet name="jummp"/>
    <less:scripts />
    <r:require module="jquery"/>
    <r:layoutResources/>
    <g:layoutHead/>
</head>
<body>
    <div id="topBackground"></div>
    <div id="middleBackground"></div>
    <div id="logo"></div>
    <div id="modeSwitch">
        <!-- TODO: active class has to be set on really selected mode -->
        <jummp:button class="left active"><g:message code="jummp.main.search"/></jummp:button>
        <jummp:button class="right"><g:message code="jummp.main.submit"/></jummp:button>
    </div>
    <div id="container">
        <div id="nav">
            <div class="left"><a href="${g.createLink(uri: '/')}"><g:message code="jummp.main.tabs.about"/></a></div>
            <div class="right"><a href="#"><g:message code="jummp.main.tabs.search"/></a></div>
        </div>
        <wcm:render path="jummp/branding"/>
        <div id="contentContainer">
            <div id="socialMedia">
            <!-- TODO: integrate social media button or use area for other part -->
            </div>
            <div id="content">
                <div id="main">
                    <g:layoutBody/>
                </div>
                <div id="sideBar">
                    <g:pageProperty name="page.sidebar"/>
                </div>
            </div>
            <wcm:render path="jummp/links"/>
        </div>
    </div>
    <wcm:render path="jummp/footer"/>
    <r:layoutResources/>
</body>
</html>
