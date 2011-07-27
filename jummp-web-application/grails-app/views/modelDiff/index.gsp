<g:if test="${modelId} && ${previousRevision} && ${currentRevision}">
    <div id="diff-summary">
        <p><b><g:message code="bives.overview.headline"></g:message></b></p>
        <ul>
        	<li class="modelId"><g:message code="model.list.modelId"></g:message>:&nbsp;<a href="#">${modelId}</a></li>
        	<li class="diffRevisionNumber"><g:message code="bives.revision.recent"></g:message>&nbsp;<a href="#">${currentRevision}</a></li>
        	<li class="diffRevisionNumber"><g:message code="bives.revision.previous"></g:message>:&nbsp;<a href="#">${previousRevision}</a></li>
        	<li><g:message code="bives.modifications"></g:message></li>
        	<ul>
        		<li>
        			<g:if test="${!modifications.moves}">0</g:if>
        			<g:else>${modifications.moves.size()}</g:else>
        			&nbsp;<g:message code="bives.overview.modifications.move"></g:message>
        		</li>
        		<li>
        			<g:if test="${!modifications.updates}">0</g:if>
        			<g:else>${modifications.updates.size()}</g:else>
        			&nbsp;<g:message code="bives.overview.modifications.update"></g:message>
        		</li>
        		<li>
        			<g:if test="${!modifications.inserts}">0</g:if>
        			<g:else>${modifications.inserts.size()}</g:else>
        			&nbsp;<g:message code="bives.overview.modifications.insert"></g:message>
        		</li>
        		<li>
        			<g:if test="${!modifications.deletes}">0</g:if>
        			<g:else>${modifications.deletes.size()}</g:else>
        			&nbsp;<g:message code="bives.overview.modifications.delete"></g:message>
        		</li>
        	</ul>
        </ul>
    </div>
    
    <div id="diff-modifications">
    	<table>
    
    	    <g:if test="${modifications} && ${modifications.inserts}">
	            <g:if test="${modifications.inserts.size() > 0}">
                    <tr>
                        <td><b><g:message code="bives.modifications.inserts"></g:message></b></td>
                        <td />
                    </tr>
                    <tr>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>&nbsp;${previousRevision}</td>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>${currentRevision}</td>
                    </tr>
                    <g:each var="insert" in="${modifications.inserts}" status="i">
                        <tr>
                            <td><div class="tree" id="tree_diffInsert${2*i}"></div></td>
                            <td><div class="tree" id="tree_diffInsert${2*i+1}"></div></td>
                        <tr />
                        <tr>
                            <td><div class="diffData" id="diffInsert${2*i}">${insert.previous}</div></td>
                            <td><div class="diffData" id="diffInsert${2*i+1}">${insert.current}</div></td>
                        </tr>
                    </g:each>
	            </g:if>
	        </g:if>

	        <g:if test="${modifications} && ${modifications.deletes}">
	            <g:if test="${modifications.deletes.size() > 0}">
                    <tr>
                        <td><b><g:message code="bives.modifications.deletes"></g:message></b></td>
                        <td />
                    </tr>
                    <tr>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>${previousRevision}</td>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>${currentRevision}</td>
                    </tr>
                    <g:each var="delete" in="${modifications.deletes}" status="i">
                        <tr>
                            <td><div class="tree" id="tree_diffDelete${2*i}"></div></td>
                            <td><div class="tree" id="tree_diffDelete${2*i+1}"></div></td>
                        </tr>
                        <tr>
                            <td><div class="diffData" id="diffDelete${2*i}">${delete.previous}</div></td>
                            <td><div class="diffData" id="diffDelete${2*i+1}">${delete.current}</div></td>
                        </tr>
                        <tr />
                    </g:each>
	            </g:if>
	        </g:if>    
    
	        <g:if test="${modifications} && ${modifications.updates}}">
	            <g:if test="${modifications.updates.size() > 0}">
                    <tr>
                        <td><b><g:message code="bives.modifications.updates"></g:message></b></td>
                        <td />
                    </tr>
                    <tr>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>${previousRevision}</td>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>${currentRevision}</td>
                    </tr>
                    <g:each var="update" in="${modifications.updates}" status="i">
                        <tr>
                            <td><div class="tree" id="tree_diffUpdate${2*i}"></div></td>
                            <td><div class="tree" id="tree_diffUpdate${2*i+1}"></div></td>
                        </tr>
                        <tr>
                            <td><div class="diffData" id="diffUpdate${2*i}">${update.previous}</div></td>
                            <td><div class="diffData" id="diffUpdate${2*i+1}">${update.current}</div></td>
                        </tr>
                        <tr />
                    </g:each>
	            </g:if>
	        </g:if>
	        
	        <g:if test="${modifications} && ${modifications.moves}">
	            <g:if test="${modifications.moves.size() > 0}">
                    <tr>
                        <td><b><g:message code="bives.modifications.moves"></g:message></b></td>
                        <td />
                    </tr>
                    <tr>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>${previousRevision}</td>
                        <td><g:message code="bives.modifications.elementsInRevision"></g:message>${currentRevision}</td>
                    </tr>
                    <g:each var="move" in="${modifications.moves}" status="i">
                        <tr>
                            <td><div class="tree" id="tree_diffMove${2*i}"></div></td>
                            <td><div class="tree" id="tree_diffMove${2*i+1}"></div></td>
                        </tr>
                        <tr>
                            <td><div class="diffData" id="diffMove${2*i}">${move.previous}</div></td>
                            <td><div class="diffData" id="diffMove${2*i+1}">${move.current}</div></td>
                        </tr>
                        <tr />
                    </g:each>
	            </g:if>
	        </g:if>
	        
    	</table>
    </div>
</g:if>