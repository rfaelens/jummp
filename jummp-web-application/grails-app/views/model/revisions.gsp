<div id="model-revisions">
<table>
    <g:each var="revision" in="${revisions}" status="i">
        <tr>
            <td class="revisionNumber"><a href="#">${revision.revisionNumber}</a></td>
            <td><span class="revisionComment">${revision.comment}</span><br/>
                <span class="revisionUploadedBy"><g:message code="model.revision.uploadedBy" args="${[revision.uploadDate, revision.owner]}"/></span>
                <g:if test="${revision.minorRevision}"><span class="revisionMinor"><g:message code="model.revision.minor"/></span></g:if>
            </td>
            <td class="revisionControl">
                <g:if test="${i == 0}">
                    <form name="delete" accept="${g.createLink(controller: model, action: deleteRevision)}">
                        <input type="hidden" name="id" value="${revision.model.id}"/>
                        <input type="hidden" name="revision" value="${revision.revisionNumber}"/>
                    </form>
                    <a href="#" class="delete"><g:message code="model.revision.delete.button"/></a>
                </g:if>
            </td>
        </tr>
    </g:each>
</table>
</div>
