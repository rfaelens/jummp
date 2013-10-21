<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.ModelTransportCommand"%>
    <head>
        <meta name="layout" content="main"/>
        <title>
        	<g:if test="${isUpdate}">
        	      <g:message code="submission.publicationInfoPage.update.title" args="${ [params.id] }" />
        	</g:if>
        	<g:else>
        	      <g:message code="submission.publicationInfoPage.create.title"/>
        	</g:else>
        </title>
        <link rel="stylesheet" href="${resource(contextPath: "${grailsApplication.config.grails.serverURL}", dir: '/css', file: 'publicationPageStyle.css')}" />
        <g:javascript contextPath="" src="publicationSubmission.js"/>
    </head>
    <body>
        <h2>Update Publication Information</h2>
        <g:hasErrors bean="${validationErrorOn}">
        <div class="errors">
        	<g:renderErrors bean="${validationErrorOn}" as="list" />
        </div>
        </g:hasErrors>
        <g:form>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td>
                                <label>
                                    <g:message code="submission.publication.title"/>
                                </label>
                            </td>
                            <td>
                          	<g:textField name="title" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.title}"/>
                            </td>
                            <td>
                                <label for="journal">
                                    <g:message code="submission.publication.journal"/>
                                </label>
                            </td>
                            <td>
                          	<g:textField name="journal" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.journal}"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:message code="submission.publication.authors"/>
                                </label>
                            </td>
                            <td>
                          	<select style="width:330px" id="authorList" name="authorList" size="2">
                          		<g:each in="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.authors}">
                          			<option value="${it.lastName}<init>${it.initials}">${it.initials}. ${it.lastName}</option>
                          		</g:each>
                          	</select>
                          		<div>
                          			<ul class="subListForm">
                          				<li>
                          					<label style="display:block;margin-left:0px">Initials</label>
                          					<span><input size="20" type="text" id="newAuthorInitials"/></span>
                          				</li>
                          				<li>
                          					<label style="display:block;margin-left:0px">Last name</label>
                          					<span>
                          					<input size="20" type="text" id="newAuthorLastName"/>
                          					</span>
                          				</li>
                          				<li>
                          					<a href="#" id="addButton" class="addButton">Add</a>
                          				</li>
                          			</ul>
                          		</div>
                          			
                            </td>
                            <td>
                                <label>
                                    <g:message code="submission.publication.synopsis"/>
                                </label>
                            </td>
                            <td>
                          	<g:textArea name="synopsis" rows="13" cols="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.synopsis}"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:message code="submission.publication.affiliation"/>
                                </label>
                            </td>
                            <td>
                          	<g:textArea name="affiliation" rows="5" cols="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.affiliation}"/>
                            </td>
                            <td>
                                <label>
                                	<g:message code="submission.publication.pubDetails"/>
                                </label>
                            </td>
                            <td>
                          		<div>
                          			<ul class="subListForm">
                          				<li>
                            				<label style="display:block;margin-left:0px"><g:message code="submission.publication.date"/></label>
                          					<span>                          	
                          						<g:select name="month" from="${1..12}" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.month?:Calendar.instance.get(Calendar.MONTH)}"/>
                          						<g:select name="year" from="${1800..Calendar.instance.get(Calendar.YEAR)}" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.year?:Calendar.instance.get(Calendar.YEAR)}"/>
                          					</span>
                          				</li>
                          				<li>
                          					<label style="display:block;margin-left:0px"><g:message code="submission.publication.volume"/></label>
                          					<span>
                          						<g:textField name="volume" size="20" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.volume}"/>
                          					</span>
                          				</li>
                          				<li>
                          					<label style="display:block;margin-left:0px"><g:message code="submission.publication.issue"/></label>
                          					<span>
                          						<g:textField name="issue" size="20" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.issue}"/>
                          					</span>
                          				</li>
                          				<li>
                          					<label style="display:block;margin-left:0px"><g:message code="submission.publication.pages"/></label>
                          					<span>
                          						<g:textField name="pages" size="20" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.pages}"/>
                            				</span>
                          				</li>
                          			</ul>
                          		</div>
                            </td>
                        </tr>
                        </table>
                  <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                    <g:submitButton name="Continue" value="${g.message(code: 'submission.publication.continueButton')}" />
                    <g:hiddenField name="authorFieldTotal" value="" />
                </div>
            </div>
        </g:form>
    </body>
    <content tag="submit">
    	selected
    </content>
