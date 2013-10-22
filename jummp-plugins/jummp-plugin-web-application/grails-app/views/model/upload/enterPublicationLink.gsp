<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>
<%@ page import="net.biomodels.jummp.core.model.ModelTransportCommand" %>
<%@ page import=" net.biomodels.jummp.model.PublicationLinkProvider" %>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.publicationLink.header"/></title>
    </head>
    <body>
    	<h2><g:message code="submission.publicationLink.header"/></h2>
        <g:form>
            <g:message code="submission.publink.publication"/>
            <% 
            	model=(workingMemory.get('ModelTC') as ModelTransportCommand) 
            %>
            <g:if test="${model.publication}">
				Currently, the model is associated with: 
            	<g:render  model="[model:model]" template="/templates/showPublication" />
			</g:if>
                                    
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                     	<tr class="prop">
                            <td class="value" style="vertical-align:top;">
                          		<g:if test="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).model.publication}">
                          					<g:select name="PubLinkProvider" 
                          					from="${PublicationLinkProvider.LinkType.
                          							values().collect(new LinkedList()) { it.toString() }}" 
                          							value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).
                          																		model.
                          																		publication.
                          																		linkProvider.
                          																		toString()}" 
                          							noSelection="['':'-Please select publication link type-']"/>
                          					<g:textField name="PublicationLink"
                          								 value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).
                          								 												model.
                          								 												publication.
                          								 												link}"/>
                          		</g:if>
                          		<g:else>
                          					<g:select name="PubLinkProvider" 
                          					from="${PublicationLinkProvider.LinkType.
                          							values().collect(new LinkedList()) { it.toString() }}" 
                          					noSelection="['':'-Please select publication link type-']"/>
                          					<g:textField name="PublicationLink"/>
                          		</g:else>
                          	</td>
                        </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                   	<g:submitButton name="Continue" value="${g.message(code: 'submission.publink.continueButton')}"/>
                </div>
            </div>
        </g:form>
    </body>
    <content tag="submit">
    	selected
    </content>
