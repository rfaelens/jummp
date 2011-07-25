<g:if test="${modelId} && ${previousRevision} && ${currentRevision}">
	<div id="diff-summary">
		<p><b>Diff summary</b></p>
		<table>
			<tr>
				<td>Model id</td>
				<td>
					${modelId}
				</td>
				<td colspan="3"/>
			</tr>
			<tr>
				<td>Recent revision</td>
				<td>
					${currentRevision}
				</td>
				<td />
				<td>Previous revision</td>
				<td>
					${previousRevision}
				</td>
			</tr>
			<g:if test="${modifications}">
				<tr>
					<td>Moves</td>
					<td>
						<g:if test="${!modifications.moves}">
							0
						</g:if>
						<g:else>
							${modifications.moves.size()}
						</g:else>
					</td>
					<td />
					<td>Updates</td>
					<td>
						<g:if test="${!modifications.updates}">
							0
						</g:if>
						<g:else>
							${modifications.updates.size()}
						</g:else>
					</td>
				</tr>
				<tr>
					<td>Inserts</td>
					<td>
						<g:if test="${!modifications.inserts}">
							0
						</g:if>
						<g:else>
							${modifications.inserts.size()}
						</g:else>
					</td>
					<td />
					<td>Deletes</td>
					<td>
						<g:if test="${!modifications.deletes}">
							0
						</g:if>
						<g:else>
							${modifications.deletes.size()}
						</g:else>
					</td>
				</tr>
			</g:if>
			<g:else>
				<tr>
					<td  colspan="5">No modifications available</td>
				</tr>
			</g:else>
		</table>
		
	</div>
	
	<table>
		<g:if test="${modifications} && ${modifications.moves}">
			<g:if test="${modifications.moves.size() > 0}">
				<div id="diff-moves">
				<!-- <p><b>Moves</b></p> -->
					<tr>
						<td><b>Moves</b></td>
						<td />
					</tr>
					<tr>
						<td>Elements in revision ${previousRevision}</td>
						<td>Elements in revision ${currentRevision}</td>
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
				<!-- </table> -->
				</div>
			</g:if>
		</g:if>
	
		<g:if test="${modifications} && ${modifications.updates}}">
			<g:if test="${modifications.updates.size() > 0}">
				<div id="diff-updates">
				<!-- <p><b>Updates</b></p>
				<table> -->
					<tr>
						<td><b>Updates</b></td>
						<td />
					</tr>
					<tr>
						<td>Elements in revision ${previousRevision}</td>
						<td>Elements in revision ${currentRevision}</td>
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
				<!-- </table> -->
				</div>
			</g:if>
		</g:if>
	
		<g:if test="${modifications} && ${modifications.inserts}">
			<g:if test="${modifications.inserts.size() > 0}">
				<div id="diff-inserts">
				<!-- <p><b>Inserts</b></p>
				<table> -->
					<tr>
						<td><b>Inserts</b></td>
						<td />
					</tr>
					<tr>
						<td>Elements in revision ${previousRevision}</td>
						<td>Elements in revision ${currentRevision}</td>
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
				<!-- </table> -->
				</div>
			</g:if>
		</g:if>

		<g:if test="${modifications} && ${modifications.deletes}">
			<g:if test="${modifications.deletes.size() > 0}">	
				<div id="diff-deletes">
				<!-- <p><b>Deletes</b></p>
				<table> -->
					<tr>
						<td><b>Deletes</b></td>
						<td />
					</tr>
					<tr>
						<td>Elements in revision ${previousRevision}</td>
						<td>Elements in revision ${currentRevision}</td>
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
				</div>
			</g:if>
		</g:if>
	</table>
</g:if>