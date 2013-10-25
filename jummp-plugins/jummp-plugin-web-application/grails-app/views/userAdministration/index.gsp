<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>User Administration</title>
        <meta name="layout" content="main" />
        <g:javascript contextPath="" src="useradministration.js"/>
        <g:javascript contextPath="" src="jquery/jquery.dataTables.js"/>
        <link rel="stylesheet" href="${resource(contextPath: "${grailsApplication.config.grails.serverURL}", dir: '/css', file: 'datatablestyle.css')}" />
    </head>
    <body>
    
    	<div class="content">
    		  <div class="view view-dom-id-9c00a92f557689f996511ded36a88594">
    		  <a style="float:right;margin-top:5px" title="Add new user" href="${g.createLink(action: "register")}">
	  	     		<span>
	  	     			Add new user
    		  		<img style="width:20px;float:none;margin-left:5px" src="http://www.ebi.ac.uk/web_guidelines/images/icons/EBI-Functional/Functional%20icons/user_add.png"/>
    		  		</span>
	  	      </a>
    		  <div class="view-content">
        <table id="userTable" class="views-table cols-4">
            <thead>
            <tr>
                <th><g:message code="user.administration.list.id"/></th>
                <th><g:message code="user.administration.list.username"/></th>
                <th><g:message code="user.administration.list.realname"/></th>
                <th><g:message code="user.administration.list.email"/></th>
                <th><g:message code="user.administration.list.enabled"/></th>
                <th><g:message code="user.administration.list.accountExpired"/></th>
                <th><g:message code="user.administration.list.accountLocked"/></th>
                <th><g:message code="user.administration.list.passwordExpired"/></th>
            </tr>
            </thead>
            <tbody></tbody>
            <tfoot>
            <tr>
                <th><g:message code="user.administration.list.id"/></th>
                <th><g:message code="user.administration.list.username"/></th>
                <th><g:message code="user.administration.list.realname"/></th>
                <th><g:message code="user.administration.list.email"/></th>
                <th><g:message code="user.administration.list.enabled"/></th>
                <th><g:message code="user.administration.list.accountExpired"/></th>
                <th><g:message code="user.administration.list.accountLocked"/></th>
                <th><g:message code="user.administration.list.passwordExpired"/></th>
            </tr>
            </tfoot>
        </table>
        </div>
        </div>
        </div>
        <g:javascript>
$(function() {
    $.jummp.userAdministration.loadUserList();
});
        </g:javascript>
    </body>
</html>
