<g:applyLayout name="main">
<content tag="main-content">
    <!-- the trigger -->
<p>
    <a href class="modalInput" rel="#overlayContainer">Example Model</a>
</p>
</content>


<g:javascript>

$(document).ready(function() {
    showModal();
    var triggers = $(".modalInput").overlay({
        // some mask tweaks suitable for modal dialogs
        mask: {
            color: 'black',
            loadSpeed: 200,
            opacity: 0.8
        },
        closeOnClick: false
    });
    $("#overlayHeadline form").submit(function(e) {
        // close the overlay
        triggers.eq(1).overlay().close();
        // do not submit the form
        return e.preventDefault();
    });
});

function showModal() {
  $("body").prepend("<div class='modal' id='overlayContainer'>\
             <div id='overlayHeadline'>\
                  <h1>Becker2010_EpoR_CoreModel</h1>\
                  <form>\
                       <button type='button' class='close'><p>Quit</p></button>\
                  </form>\
             </div>\
             <div id='overlayInfoStripe'>\
                <table border='0'>\
                    <tr>\
                       <td class='key left'>ID:</td>\
                       <td class='value'>MODEL100526</td>\
                       <td class='key'>Version:</td>\
                       <td class='value'>123456</td>\
                       <td class='key'>Format:</td>\
                       <td class='value'>SBML</td>\
                    </tr>\
                    <tr>\
                       <td class='key left'>Biomodels-ID:</td>\
                       <td class='value'>BIOMD000000000271</td>\
                       <td class='key'>Status:</td>\
                       <td class='value'>published</td>\
                       <td class='key'>Original Model:</td>\
                       <td class='value'>Link or Icon</td>\
                    <tr>\
                       <td class='key left'>Name:</td>\
                       <td class='value'>Sagenhafte Entdeckung</td>\
                       <td class='key'>Creation Date:</td>\
                       <td class='value'>26 May 2010</td>\
                       <td class='key'>Change Comment:</td>\
                       <td>Link</td>\
                    </tr>\
                    </tr>\
                </table>\
             </div>\
             <div id='overlayAuthorStripe'>\
                <table border='0'>\
                    <tr>\
                       <td class='key left'>Authors:</td>\
                       <td class='value'>Becker V, Schilling M, Bachmann J, Baumann U, Raue A, Maiwald T,\ Timmer J, Klingmüller U </td>\
                    </tr>\
                </table>\
                <button type='button' class='download'><p>Download</p></button>\
             </div>\
             <div id='overlayNav'>\
                <div class='unselected'><a href='#'>Publication</a></div>\
                <div class='unselected'><span><a href='#'>Annotations</a></span></div>\
                <div class='selected'><span><a href='#'>Notes</a></span></div>\
                <div class='unselected'><span><a href='#'>Reaction-Graph</a></span></div>\
                <div class='unselected'><span><a href='#'>Curation Results</a></span></div>\
                <div class='unselected'><span><a href='#'>Model-Data</a></span></div>\
             </div>\
             <div id='overlayContentContainer'>\
<h2>This is the core model described in the article:</h2>\
<p class='text'>\
core ...\
</p>\
<h2>Abstract:</h2>\
<p class='text'>\
the abstract ...\
</p>\
<p class='formula'>\
Here are the formula ...\
</p>\
</div>\
<div id='overlayLinkArea'>Here are some | links</div>\
<div id='overlayFooter'>Here is the | Impressium</div>\
</div>\
");
}

</g:javascript>

</g:applyLayout>
