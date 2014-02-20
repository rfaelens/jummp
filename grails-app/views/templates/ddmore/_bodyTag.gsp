<%
  	def sidebarContent=g.pageProperty(name:'page.sidebar')
  	if (sidebarContent) {
  		sidebarContent=sidebarContent.trim()
  	}
%>

<g:if test="${sidebarContent}">
    <body class="html not-front not-logged-in one-sidebar sidebar-second page-node page-node- page-node-37 node-type-webform section-content">
</g:if>
<g:else>
    <body>
</g:else>
