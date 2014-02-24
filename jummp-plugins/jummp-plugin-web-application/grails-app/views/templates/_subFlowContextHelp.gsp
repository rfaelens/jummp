    <content tag="contexthelp">
		<g:if test="${workingMemory.get("isUpdateOnExistingModel") as Boolean}">
			update
		</g:if>
		<g:else>
			submission
		</g:else>
	</content>
