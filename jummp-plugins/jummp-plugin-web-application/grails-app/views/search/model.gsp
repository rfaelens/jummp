<div id='overlayHeadline'>
    <h1>${revision.model.name}</h1>
    <jummp:button class="close">Quit</jummp:button>
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
<div id='overlayAuthorStripe'>
    <table border='0'>
        <tr>
            <td class='key left'><g:message code="model.model.authors"/></td>
            <td class='value'><g:each in="${revision.model.publication.authors}">${it.firstName} ${it.lastName}, </g:each></td>
        </tr>
    </table>
    <button type='button' class='download'><p>Download</p></button>
</div>
<div id='overlayNav'>
    <div class='unselected'><a href='#'><g:message code="model.model.publication"/></a></div>
    <div class='unselected'><span><a href='#'><g:message code="model.model.annotations"/></a></span></div>
    <div class='unselected'><span><a href='#'><g:message code="model.model.notes"/></a></span></div>
    <div class='unselected'><span><a href='#'><g:message code="model.model.reactionGraph"/></a></span></div>
    <div class='unselected'><span><a href='#'><g:message code="model.model.curationResults"/></a></span></div>
    <div class='selected'><span><a href='#'><g:message code="model.model.model"/></a></span></div>
</div>
<div id='overlayContentContainer'>
    <h2>This is the core model described in the article:</h2>
    <p class='text'>core ...</p>
    <h2>Abstract:</h2>
    <p class='text'>the abstract ...</p>
    <p class='formula'>Here are the formula ...</p>
</div>
<div id='overlayLinkArea'>Here are some | links</div>
<div id='overlayFooter'>Here is the | Impressium</div>
