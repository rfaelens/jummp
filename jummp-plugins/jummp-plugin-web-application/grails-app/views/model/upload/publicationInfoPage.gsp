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
        <script>
        	function addText() {
        		if ($('#newAuthorLastName').val()) {
			    var lastName=$('#newAuthorLastName').val()
			    var fullName=lastName
			    var id=lastName+"<init>"
			    if ($('#newAuthorInitials').val()) {
			    	    var initials=$('#newAuthorInitials').val()
			    		id=id+initials
			    	    fullName=initials+". "+lastName
			    }
			    var alreadyPresent=false
			    $("#authorList > option").each(function() {
			    	if (this.value === id) {
			    		alreadyPresent=true
			    	}
			    });
			    if (!alreadyPresent) {
			    	$('#authorList')
			    		.append($('<option>', { value : id })
			    		.text(fullName));
			    		setHiddenFieldValue();
			    }
			    else {
			    	alert("An author by that name is already added to the publication.")
			    }
			    $('#newAuthorInitials').val("")
			    $('#newAuthorLastName').val("")
			}
        	}
        	
        	$(document).ready(function () {
        			$('#addButton').click(function() {
        				addText()
        			});
        			setHiddenFieldValue()
        	});
        	
        	function setHiddenFieldValue() {
        		var text=""
        		$('#authorList > option').each(function(i, option) {
        				text=text+"!!author!!"+$(option).val();
        		});
        		$('#authorFieldTotal').val(text);
        	}
	</script>
     <style type="text/css">
	.addButton {
	-moz-box-shadow:inset 0px 1px 0px 0px #ffffff;
	-webkit-box-shadow:inset 0px 1px 0px 0px #ffffff;
	box-shadow:inset 0px 1px 0px 0px #ffffff;
	background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #ededed), color-stop(1, #dfdfdf) );
	background:-moz-linear-gradient( center top, #ededed 5%, #dfdfdf 100% );
	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#ededed', endColorstr='#dfdfdf');
	background-color:#ededed;
	-webkit-border-top-left-radius:6px;
	-moz-border-radius-topleft:6px;
	border-top-left-radius:6px;
	-webkit-border-top-right-radius:6px;
	-moz-border-radius-topright:6px;
	border-top-right-radius:6px;
	-webkit-border-bottom-right-radius:6px;
	-moz-border-radius-bottomright:6px;
	border-bottom-right-radius:6px;
	-webkit-border-bottom-left-radius:6px;
	-moz-border-radius-bottomleft:6px;
	border-bottom-left-radius:6px;
	text-indent:0;
	border:1px solid #dcdcdc;
	display:inline-block;
	color:#777777;
	font-family:arial;
	font-size:15px;
	font-weight:bold;
	font-style:normal;
	height:25px;
	line-height:25px;
	width:100px;
	text-decoration:none;
	text-align:center;
	text-shadow:1px 1px 0px #ffffff;
	}
	.addButton:hover {
	background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #dfdfdf), color-stop(1, #ededed) );
	background:-moz-linear-gradient( center top, #dfdfdf 5%, #ededed 100% );
	filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#dfdfdf', endColorstr='#ededed');
	background-color:#dfdfdf;
	}.addButton:active {
	top:1px;
	}
	label {
		display:block;
		margin-left:10px;
	}
	.authorLabel {
		display:block;
	}
	#authorform {
    	width:200px; 
    	overflow:hidden;
    	margin-left:-25px;
    	margin-top:5px;
    }
 
    #authorform li {
    	list-style:none; 
    	padding-bottom:10px;
    }
    table
    {
    	border-collapse:separate;
    	border-spacing:10px 5px;
    }

    </style>
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
                            <td class="title" style="vertical-align:top;">
                                <label for="title">
                                    <g:message code="submission.publication.title"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:textField name="title" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.title}"/>
                            </td>
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.journal"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:textField name="journal" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.journal}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.authors"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<select style="width:330px" id="authorList" name="authorList" size="2">
                          		<g:each in="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.authors}">
                          			<option value="${it.lastName}<init>${it.initials}">${it.initials}. ${it.lastName}</option>
                          		</g:each>
                          	</select>
                          		<div>
                          			<ul id="authorform">
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
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.synopsis"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:textArea name="synopsis" rows="13" cols="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.synopsis}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.affiliation"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:textField name="affiliation" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.affiliation}"/>
                            </td>
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.date"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:select name="month" from="${1..12}" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.month?:Calendar.instance.get(Calendar.MONTH)}"/>
                          	<g:select name="year" from="${1800..Calendar.instance.get(Calendar.YEAR)}" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.year?:Calendar.instance.get(Calendar.YEAR)}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.volume"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:textField name="volume" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.volume}"/>
                            </td>
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.issue"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:textField name="issue" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.issue}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="title" style="vertical-align:top;">
                                <label for="journal">
                                    <g:message code="submission.publication.pages"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<g:textField name="pages" size="50" value="${(workingMemory.get("ModelTC") as ModelTransportCommand).publication.pages}"/>
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
