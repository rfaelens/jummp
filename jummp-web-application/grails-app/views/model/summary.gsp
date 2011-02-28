<div id="model-reference-publication">
    <g:render template="/templates/publication" bean="${publication}" var="publication"/>
</div>
<div id="model-information-summary">
    <h2><g:message code="model.summary.information"/></h2>
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><strong><g:message code="model.summary.model.id"/></strong></td>
            <td>${revision.model.id}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.submitter"/></strong></td>
            <td>${revision.model.submitter}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.submissionDate"/></strong></td>
            <td>${revision.model.submissionDate}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.revision.id"/></strong></td>
            <td>${revision.revisionNumber}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.revision.uploadDate"/></strong></td>
            <td>${revision.uploadDate}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.revision.submitter"/></strong></td>
            <td>${revision.owner}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.format"/></strong></td>
            %{--TODO: download should be for revision and not for model!--}%
            <td>${revision.format.name} <a href="${g.createLink(controller: 'model', action: 'download', id: revision.model.id)}"><g:message code="model.summary.model.download"/></a></td>
        </tr>
        </tbody>
    </table>
</div>
