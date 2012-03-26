<div id='overlayTable'>
    <div id='overlayTableRow'>
        <div id='overlayHeadline'>
            <h1>${revision.model.name}</h1>
        </div>
        <div id='overlayQuit'>
            <jummp:button class="close">Quit</jummp:button>
        </div>
    </div>
</div>
<div id='overlayInfoStripe'>
    <table border='0'>
        <tr>
            <td class='key left'><g:message code="model.model.id"/></td>
            <td class='value'>${revision.id}</td>
            <td class='key'><g:message code="model.model.version"/></td>
            <td class='value'>${revision.revisionNumber}</td>
            <td class='key'><g:message code="model.model.format"/></td>
            <td class='value'>${revision.format.identifier}</td>
        </tr>
        <tr>
            <td class='key left'><g:message code="model.model.biomodelsId"/></td>
            <td class='value'></td>
            <td class='key'><g:message code="model.model.status"/></td>
            <td class='value'>${revision.model.state}</td>
            <td class='key'><g:message code="model.model.originalModel"/></td>
            <td class='value'></td>
        <tr>
            <td class='key left'><g:message code="model.model.name"/></td>
            <td class='value'>${revision.model.name}</td>
            <td class='key'><g:message code="model.model.creationDate"/></td>
            <td class='value'>${revision.model.submissionDate}</td>
            <td class='key'><g:message code="model.model.changeComment"/></td>
            <td></td>
        </tr>
        </tr>
    </table>
</div>
<div id='overlayTable'>
    <div id='overlayTableRow'>
        <div id='overlayAuthorStripe'>
            <table border='0'>
                <tr>
                    <td class='key left'><g:message code="model.model.authors"/></td>
                    <td class='value'><g:each in="${authors}">${it}</g:each></td>
                </tr>
            </table>
        </div>
        <div id='download'>
            <p><a href="${g.createLink(controller: 'model', action: 'download', id: revision.id)}" target="_blank">Download</a></p>
        </div>
    </div>
</div>
<div id='overlayNav'>
    <div rel="${g.createLink(controller: 'model', action: 'publication', id: revision.id)}"><span><g:message code="model.model.publication"/></span></div>
    <div rel="${g.createLink(controller: 'model', action: 'annotations', id: revision.id)}"><span><g:message code="model.model.annotations"/></span></div>
    <div rel="${g.createLink(controller: 'model', action: 'notes', id: revision.id)}"><span><g:message code="model.model.notes"/></span></div>
    <div style="display: none;"><span><g:message code="model.model.reactionGraph"/></span></div>
    <div style="display: none;"><span><g:message code="model.model.curationResults"/></span></div>
    <div rel="${g.createLink(controller: 'model', action: 'summary', id: revision.id)}"><span><g:message code="model.model.model"/></span></div>
</div>
<div id='overlayContentContainer'></div>
<div id='overlayLinkArea'>Here are some | links</div>
<div id='overlayFooter'>Here is the | Impressium</div>