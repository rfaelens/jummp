<div id="nav">
    <!-- TODO: active class has to be set on really selected mode -->
    <div class="left ${pageProperty(name: 'body.activetab') ? (pageProperty(name: 'body.activetab') == 'about' ? 'active' : '') : 'active'}"><a href="${g.createLink(uri: '/')}"><g:message code="jummp.main.tabs.about"/></a></div>
    <div class="right ${pageProperty(name: 'body.activetab') ? (pageProperty(name: 'body.activetab') == 'search' ? 'active' : '') : ''}"><a href="${g.createLink(controller: 'search', action: 'list')}"><g:message code="jummp.main.tabs.search"/></a></div>
</div>
        
