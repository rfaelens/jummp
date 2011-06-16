<div id="model-revisions">
<table>
    <g:each var="revision" in="${revisions}">
        <tr>
            <td class="revisionNumber"><a href="#">${revision.revisionNumber}</a></td>
            <td><span class="revisionComment">${revision.comment}</span><br/>
                <span class="revisionUploadedBy"><g:message code="model.revision.uploadedBy" args="${[revision.uploadDate, revision.owner]}"/></span>
                <g:if test="${revision.minorRevision}"><span class="revisionMinor"><g:message code="model.revision.minor"/></span></g:if>
            </td>
        </tr>
    </g:each>
</table>
</div>
