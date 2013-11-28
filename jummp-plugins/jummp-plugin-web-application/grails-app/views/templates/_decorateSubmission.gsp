 <content tag="submit">
    	selected
    </content>
    <content tag="title">
		<g:if test="${workingMemory.get("isUpdateOnExistingModel") as Boolean}">
			Update Model ${workingMemory.get("model_id")}
		</g:if>
		<g:else>
			Submit a model
		</g:else>
	</content>

