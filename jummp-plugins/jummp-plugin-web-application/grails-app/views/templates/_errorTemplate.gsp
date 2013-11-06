<h2>Oh, snap</h2>
<g:if test="${session.messageForError}">
	<p>An error occurred during the submission process. A ticket has been generated
	and the admin has been notified. Your ticket reference is <b>${session.messageForError}</b></p>
	<% session.messageForError=null %>
</g:if>
<g:else>
	<g:if test="${session.updateMissingId}">
		<% session.updateMissingId=null %>
		<p>A model ID was not specified for the update process. </p>
	</g:if>
	<g:else>
		<p>Something bad happened. That is all we know. Sorry 'bout that.</p>
	</g:else>
</g:else>
