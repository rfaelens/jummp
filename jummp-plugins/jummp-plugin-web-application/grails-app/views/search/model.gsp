<div id='overlayHeadline'>
    <h1>${revision.model.name}</h1>
    <button type='button' class='close'><p>Quit</p></button>
</div>
<div id='overlayInfoStripe'>
    <table border='0'>
        <tr>
            <td class='key left'>ID:</td>
            <td class='value'>${revision.id}</td>
            <td class='key'>Version:</td>
            <td class='value'>${revision.revisionNumber}</td>
            <td class='key'>Format:</td>
            <td class='value'>${revision.format.identifier}</td>
        </tr>
        <tr>
            <td class='key left'>Biomodels-ID:</td>
            <td class='value'></td>
            <td class='key'>Status:</td>
            <td class='value'>${revision.model.state}</td>
            <td class='key'>Original Model:</td>
            <td class='value'></td>
        <tr>
            <td class='key left'>Name:</td>
            <td class='value'>${revision.model.name}</td>
            <td class='key'>Creation Date:</td>
            <td class='value'>${revision.model.submissionDate}</td>
            <td class='key'>Change Comment:</td>
            <td></td>
        </tr>
        </tr>
    </table>
</div>
<div id='overlayAuthorStripe'>
    <table border='0'>
        <tr>
            <td class='key left'>Authors:</td>
            <td class='value'><g:each in="${revision.model.publication.authors}">${it.firstName} ${it.lastName}, </g:each></td>
        </tr>
    </table>
    <button type='button' class='download'><p>Download</p></button>
</div>
<div id='overlayNav'>
    <div class='unselected'><a href='#'>Publication</a></div>
    <div class='unselected'><span><a href='#'>Annotations</a></span></div>
    <div class='unselected'><span><a href='#'>Notes</a></span></div>
    <div class='unselected'><span><a href='#'>Reaction-Graph</a></span></div>
    <div class='unselected'><span><a href='#'>Curation Results</a></span></div>
    <div class='selected'><span><a id='modelTabs-model' onClick='createLink("search", "summary", params.id)' href='#'>Model</a></span></div>
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
