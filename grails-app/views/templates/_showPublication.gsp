<g:if test="${model.publication}">
	<div>
		<style>
			.hiddenContent {display:none;}
		</style>
		<a class="expander" title="Click to see more" href="#">
			<span>${model.publication.title}</span>
			<span>
				<img style="width:12px;margin:2px;float:none" src="http://www.ebi.ac.uk/web_guidelines/images/icons/EBI-Functional/Functional%20icons/expand.png"/>
			</span>
		</a>
		<a href="${model.publication.linkProvider.identifiersPrefix?model.publication.linkProvider.identifiersPrefix+model.publication.link:model.publication.link}">
			<img style="width:12px;margin:2px;float:none" src="http://www.ebi.ac.uk/web_guidelines/images/icons/EBI-Generic/Generic%20icons/external_link.png"/>
		</a>		
		<div class="hiddenContent">
    		<ul style="list-style-type: none;margin: 3px;">
    			<li>${model.publication.authors.collect{"${it.initials?:""} ${it.lastName}"}.join(", ")}</li>
    			<li>${model.publication.affiliation}</li>
    			<li>
    				${model.publication.journal}${model.publication.month?", ${model.publication.month}/":""}${model.publication.year?"${model.publication.year}":""}${model.publication.volume?", Volume ${model.publication.volume}":""}${model.publication.issue?", Issue ${model.publication.issue}":""}${model.publication.pages?", pages: ${model.publication.pages}":""}
    			</li>
    			<li>
    				<a href="${model.publication.linkProvider.identifiersPrefix?model.publication.linkProvider.identifiersPrefix+model.publication.link:model.publication.link}">View Publication</a>
    			</li>
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
