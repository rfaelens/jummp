<g:if test="${model.publication}">
	<div>
		<style>
			.hiddenContent {display:none;}
		</style>
		<a class="expander" href="#">${model.publication.title} - Click to see more</a>
		<div class="hiddenContent">
    		<span>
    			<label>
    				Journal:
    			</label>
    			${model.publication.journal}
    		</span>
    		<span>
    			<label>
    				Authors:
    			</label>
    			${model.publication.authors.collect{"${it.initials?:""} ${it.lastName}"}.join(",")}
    		</span>
    	</div>

    	<script src="http://sylvain-hamel.github.io/simple-expand/javascripts/simple-expand.js"></script>
    	<script>
			$('.expander').simpleexpand({'defaultTarget':'div.hiddenContent'});
		</script>
		</div>
</g:if>
<g:else>
	Not provided
</g:else>
