<%--
 Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the
 terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 details.

 You should have received a copy of the GNU Affero General Public License along
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>

<h2>Oh, snap</h2>
<g:if test="${session.messageForError}">
    <p>An error occurred during the submission process. A ticket has been generated
    and the admin has been notified. Your ticket reference is <b>${session.messageForError}</b>
    </p>
    <% session.messageForError = null %>
</g:if>
<g:else>
    <g:if test="${session.updateMissingId}">
        <% session.updateMissingId = null %>
        <p>A valid model ID was not specified for the update process.</p>
    </g:if>
    <g:else>
        <p>Something bad happened. That is all we know. Sorry 'bout that.</p>
    </g:else>
</g:else>
