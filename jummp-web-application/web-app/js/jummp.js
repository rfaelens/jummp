/**
 * Updates the user information panel to hide/show login/logout data.
 * @param logedIn @c true if the user logged in, @c false if he logged out
 * @param userName The name of the user when logged in, field is optional
 */
function switchUserInformation(logedIn, userName) {
    if (logedIn) {
        if (userName) {
            $("#userInformationLogedIn span").first().text(userName);
        }
        $("#userInformationLogedIn").show();
        $("#userInformationLogedOut").hide();
    } else {
        $("#userInformationLogedIn").hide();
        $("#userInformationLogedOut").show();
    }
}

/**
 * Shows and resets the login dialog.
 */
function showLoginDialog() {
    $("#ajax_j_username").val("");
    $("#ajax_j_password").val("");
    $("#ajaxLoginStatus").hide();
    $('#ajaxLoginDialog').dialog('open');
}

/**
 * Performs authentication through AJAX by serializing the ajaxLoginForm.
 * On success the login dialog is closed and the login event is fired.
 * On failure the error message is displayed.
 */
function authAjax() {
    $.post(createURI("j_spring_security_check"), $("#ajaxLoginForm").serialize(), function(data) {
        if (data.success) {
            $("#ajaxLoginDialog").dialog('close');
            $(document).trigger("login", data.username)
        } else if (data.error) {
            $("#ajaxLoginStatus").html(data.error);
            $("#ajaxLoginStatus").show();
        }
    });
}

/**
 * Performs logout through AJAX.
 * On success the logout event is fired.
 */
function logout() {
    $.ajax({ url: createURI("logout"),
        success: function(data, textStatus, jqXHR) {
            $(document).trigger("logout");
        }
    });
}

/**
 * Creates a URI to be used in a href or src HTML attribute.
 * @param path The path
 */
function createURI(path) {
    return "/" + $.appName + "/" + path;
}

/**
 * Same as g.createLink.
 * @param controller The name of the grails controller
 * @param action The optional action
 * @param id The optional id
 */
function createLink(controller, action, id) {
    var path = controller;
    if (action != undefined) {
        path += "/" + action;
        if (id != undefined) {
            path += "/" + id;
        }
    }
    return createURI(path);
}

/**
 * Creates the data table used in the model overview page.
 */
function createModelDataTable() {
    if ($('#modelTable').length == 0) {
        return;
    }
    $('#modelTable').dataTable({
        // TODO: in future it might be interesting to allow filtering
        bFilter: false,
        bProcessing: true,
        bServerSide: true,
        bJQueryUI: true,
        sPaginationType: "full_numbers",
        sAjaxSource: createLink('model', 'dataTableSource'),
        // TODO: move function into an own method,
        "fnServerData": function(sSource, aoData, fnCallback) {
            $.getJSON(sSource, aoData, function(json) {
                for (var i=0; i<json.aaData.length; i++) {
                    var rowData = json.aaData[i];
                    var id = rowData[0];
                    if (rowData[2] != null) {
                        var publication = rowData[2];
                        var html = "";
                        if (publication.linkProvider.name == "PUBMED") {
                            html = createPubMedLink(publication);
                        } else if (publication.linkProvider.name == "DOI") {
                            html = createDoiLink(publication);
                        }
                        html += createPublicationTooltip(publication);
                        rowData[2] = html;
                    }
                    // id column
                    rowData[0] = '<a href="' + createLink("model", "show", id) + '">' + id + '</a>';
                    // the format/download column
                    rowData[4] = rowData[4] + '&nbsp;<a href="' + createLink('model', 'download', id) + '">' + i18n.model.list.download + '</a>';
                }
                fnCallback(json);
                $('a.tooltip').cluetip({local: true, width: 550});
            });
        },
        // i18n
        oLanguage: {
            oPaginate: {
                sFirst:    i18n.dataTables.paginate.first,
                sLast:     i18n.dataTables.paginate.last,
                sNext:     i18n.dataTables.paginate.next,
                sPrevious: i18n.dataTables.paginate.previous
            },
            sEmptyTable:   i18n.dataTables.empty,
            sInfo:         i18n.dataTables.info,
            sInfoEmpty:    i18n.dataTables.infoEmpty,
            sInfoFiltered: i18n.dataTables.infoFiltered,
            sLengthMenu:   i18n.dataTables.lengthMenu,
            sProcessing:   i18n.dataTables.processing,
            sSearch:       i18n.dataTables.search,
            sZeroRecords:  i18n.dataTables.noFilterResults
        }
    });
    $(document).bind("login", function(event) {
        $('#modelTable').dataTable().fnDraw();
    });
}

/**
 * Creates HTML markup for a tooltip describing a publication.
 * The tooltip is a table embedded in a div element with id "publication-tooltip-${id}".
 * The ${id} is the PubMed ID or DOI ID.
 * The following information from the JSON structure is used:
 * @li link: The PubMed ID or DOI ID
 * @li linkProvider: subsection "name" either "PUBMED" or "DOI"
 * @li authors: list of author objects containing fields firstName, lastName and initials (optional)
 * @li journal: The name of the Journal (optional)
 * @li issue: The Journal issue (optional)
 * @li year: The year of the publication (optional)
 * @li month: The month of the publication (optional)
 * @li day: The day of month of the publication (optional)
 * @li volume: The volume of the journal issue (optional)
 * @li pages: The pages in the journal (optional)
 * @li affiliation: The affiliation of the authors (optional)
 * @li synopsis: The abstract of the publication (optional)
 * @param publication JSON object describing the publication
 */
function createPublicationTooltip(publication) {
    var tooltip = "";
    if (publication.link && publication.linkProvider.name == "PUBMED") {
        tooltip += "<tr><td><strong>" + i18n.publication.pubmedid + ":</strong></td><td>" + publication.link + "</td></tr>";
    }
    if (publication.link && publication.linkProvider.name == "DOI") {
        tooltip += "<tr><td><strong>" + i18n.publication.doi + ":</strong></td><td>" + publication.link + "</td></tr>";
    }
    if (publication.authors && publication.authors.length > 0) {
        var authors = "";
        for (var i=0; i < publication.authors.length; i++) {
            var author = publication.authors[i];
            if (i > 0) {
                authors += ", ";
            }
            if (author.initials) {
                authors += author.initials + " ";
            }
            authors += author.lastName;
        }
        tooltip += "<tr><td><strong>" + i18n.publication.authors + ":</strong></td><td>" + authors + "</td></tr>";
    }
    if (publication.journal) {
        tooltip += "<tr><td><strong>" + i18n.publication.journal + ":</strong></td><td>" + publication.journal + "</td></tr>";
    }
    if (publication.issue) {
        tooltip += "<tr><td><strong>" + i18n.publication.issue + ":</strong></td><td>" + publication.issue + "</td></tr>";
    }
    if (publication.volume) {
        tooltip += "<tr><td><strong>" + i18n.publication.volume + ":</strong></td><td>" + publication.volume + "</td></tr>";
    }
    if (publication.pages) {
        tooltip += "<tr><td><strong>" + i18n.publication.pages + ":</strong></td><td>" + publication.pages + "</td></tr>";
    }
    if (publication.year) {
        tooltip += "<tr><td><strong>" + i18n.publication.date + ":</strong></td><td>" + publication.year;
        if (publication.month) {
            tooltip += " " + publication.month;
            if (publication.day) {
                tooltip += " " + publication.day;
            }
        }
        tooltip += "</td></tr>";
    }
    if (publication.affiliation) {
        tooltip += "<tr><td><strong>" + i18n.publication.affiliation + ":</strong></td><td>" + publication.affiliation + "</td></tr>";
    }
    if (publication.synopsis) {
        tooltip += "<tr><td><strong>" + i18n.publication.synopsis + ":</strong></td><td>&nbsp;</td></tr><tr><td colspan='2'>" + publication.synopsis + "</td></tr>";
    }
    return '<div id="' + publicationTooltipId(publication.link) + '" style="display: none"><table><thead/><tbody>' + tooltip + '</tbody></table></div>';
}

/**
 * Creates HTML markup for a hyperlink to citexplore referencing a PubMed Id.
 * The hyperlink has a class "tooltip", a title and rel attribute referencing a tooltip.
 * The following information from the JSON structure is used:
 * @li link: The PubMed ID
 * @li title: The title of the publication
 * @li journal: The name of the Journal (optional)
 * @li year: The year of the publication (optional)
 * @li month: The month of the publication (optional)
 * @li issue: The Journal issue (optional)
 * @li volume: The volume of the journal issue (optional)
 * @li pages: The pages in the journal (optional)
 * @param publication JSON object describing the publication
 */
function createPubMedLink(publication) {
    return '<a class="tooltip" href="http://www.ebi.ac.uk/citexplore/citationDetails.do?dataSource=MED&externalId=' + publication.link + '" title="' + publication.title + '" rel="#' + publicationTooltipId(publication.link) + '">' + createPublicationLinkTitle(publication) + '</a>';
}

/**
 * Creates HTML markup for a hyperlink to a DOI resource.
 * The hyperlink has a class "tooltip", a title and rel attribute referencing a tooltip.
 * The following information from the JSON structure is used:
 * @li link: The DOI link
 * @li title: The title of the publication
 * @li journal: The name of the Journal (optional)
 * @li year: The year of the publication (optional)
 * @li month: The month of the publication (optional)
 * @li issue: The Journal issue (optional)
 * @li volume: The volume of the journal issue (optional)
 * @li pages: The pages in the journal (optional)
 * @param publication JSON object describing the publication
 */
function createDoiLink(publication) {
    return '<a class="tooltip" href="http://dx.doi.org/' + publication.link + '" title="' + publication.title + '" rel="#' + publicationTooltipId(publication.link) + '">' + createPublicationLinkTitle(publication) + '</a>';
}

/**
 * Creates an id for a publication tooltip from the PubMed or DOI id.
 * @param id The PubMed or DOI id.
 */
function publicationTooltipId(id) {
    var linkId = id.replace('.', '');
    linkId = linkId.replace('/', '');
    linkId = linkId.replace('(', '');
    linkId = linkId.replace(')', '');
    return "publication-tooltip-" + linkId;
}

/**
 * Creates the visible title for a publication link.
 * The following information from the JSON structure is used:
 * @li title: The title of the publication
 * @li journal: The name of the Journal (optional)
 * @li year: The year of the publication (optional)
 * @li month: The month of the publication (optional)
 * @li issue: The Journal issue (optional)
 * @li volume: The volume of the journal issue (optional)
 * @li pages: The pages in the journal (optional)
 * @param publication JSON object describing the publication
 */
function createPublicationLinkTitle(publication) {
    var title = "";
    if (publication.journal) {
        title += publication.journal;
    }
    if (publication.year) {
        title += " " + publication.year;
        if (publication.month) {
            title += " " + publication.month;
        }
    }
    if (publication.volume) {
        title += ";" + publication.volume;
    }
    if (publication.issue) {
        title += "(" + publication.issue + ")";
    }
    if (publication.pages) {
        title += ": " + publication.pages;
    }
    return title
}

/**
 * Global document initialization.
 * Connects all the global events like login/logout.
 */
$(document).ready(function() {
    $(document).bind("logout", function() {
        switchUserInformation(false);
    });
    $(document).bind("login", function(event, username) {
        switchUserInformation(true, username);
    });
    // create Ajax Login Dialog
    // TODO: maybe delay till first time used?
    $("#ajaxLoginDialog").dialog({
        autoOpen: false,
        width: 400, // need a slightly larger dialog
        title: i18n.login.authenticate,
        buttons: [
            {
                text: i18n.login.authenticate,
                click: authAjax
            },
            {
                text: i18n.login.cancel,
                click: function() { $(this).dialog("close")}
            }
        ]
    });
    createModelDataTable();
});
