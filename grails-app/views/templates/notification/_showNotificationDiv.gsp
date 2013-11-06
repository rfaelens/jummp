<div class='flashNotificationDiv'>
<g:if test="${flashMessage && flashMessage.length()>0}">
   		<%
   			flashMessage=message(code: flashMessage, default:flashMessage)
   		%>
		${flashMessage}
   		<% flashMessage="" %>
   		<g:javascript>
   			scheduleHide()
   		</g:javascript>
</g:if>
<g:else>
	<g:if test="${validationErrorOn}">
		<g:hasErrors bean="${validationErrorOn}">
        		<g:renderErrors bean="${validationErrorOn}" as="list" />
        </g:hasErrors>
        <g:javascript>
   			scheduleHide()
   		</g:javascript>
    </g:if>
    <g:else>
		<g:javascript>
			hideNow()
		</g:javascript>
	</g:else>
</g:else>
</div>
