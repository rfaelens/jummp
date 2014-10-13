<g:form name="newTeamForm" action="save">
	<table class='spaced'>
		<tbody>
			<tr>
				<td><label class="required" for="name">Name</label></td>
				<td><span><g:textField required="true" value="${name}" autofocus="true" maxlength="255" id="teamName" name="name"/></span></td>
			</tr>
			<tr>
				<td><label for="description">Description</label></td>
				<td><span><g:textField maxlength="255" value="${description}" id="teamDescription" name="description"/></span></td>
			</tr>
		</tbody>
	</table>
	<div id="membersContainer" class='spaced'>
		<h2>Team Members</h2>
		<div>
			<label for="nameSearch">User</label>
			<input placeholder="Name, username or email" id="nameSearch" name="nameSearch" type="text"/>
			<g:submitButton name="add" value="Add"/>
		</div>
		<span class="tip">
			<span class='tipNote'>Tip:</span>
			Choose the collaborator, then press enter to add them to this team.
		</span>
		<div class="spaced" id="members">
			<table id="membersTable">
				<thead id='nameLabel'>
					<tr>
						<th>Name</th>
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody id="membersTableBody">
					<!-- ADD MEMBERS HERE. -->
				</tbody>
			</table>
		</div>
	</div>

	<g:submitButton class='submitButton' name="${buttonLabel}" value="${buttonLabel}"/>
</g:form>