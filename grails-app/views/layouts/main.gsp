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
        <button type="button" class="left active">
            <p><g:message code="jummp.main.search"/></p>
            <div class="glow"></div>
        </button>
        <button type="button" class="right">
            <p><g:message code="jummp.main.submit"/></p>
            <div class="glow"></div>
        </button>
    </div>
    <div id="container">
        <div id="nav">
            <div class="left"><a href="${g.createLink(uri: '/')}"><g:message code="jummp.main.tabs.about"/></a></div>
            <div class="right"><a href="#"><g:message code="jummp.main.tabs.search"/></a></div>
        </div>
        <div id="branding">
        <!-- TODO: integrate branding button -->
        </div>
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
            <div id="linkArea">
            <!--  TODO: customizable Link area -->
            Some site specific links
            </div>
        </div>
    </div>
    <div id="footer">
    <!-- TODO: customizable Imprint -->
    Imprint
    </div>
    <r:layoutResources/>
</body>
</html>
