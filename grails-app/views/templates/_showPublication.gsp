<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



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
    			<%-- <li>
    				<a href="${model.publication.linkProvider.identifiersPrefix?model.publication.linkProvider.identifiersPrefix+model.publication.link:model.publication.link}">View Publication</a>
    			</li> --%>
    	</div>
    	<g:javascript contextPath="" src="simple-expand.js"/>
    	<script>
			$('.expander').simpleexpand({'defaultTarget':'div.hiddenContent'});
		</script>
		</div>
</g:if>
<g:else>
	Not provided
</g:else>
