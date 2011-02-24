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
            $.ajax({
                "dataType": 'json',
                "type": "POST",
                "url": sSource,
                "data": aoData,
                "error": function(jqXHR, textStatus, errorThrown) {
                    handleError($.parseJSON(jqXHR.responseText));
                    // clear the table
                    fnCallback({aaData: [], iTotalRecords: 0, iTotalDisplayRecords: 0});
                },
                "success": function(json) {
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
                        rowData[0] = '<a href="#" onclick="showModel(\'' + id + '\');">' + id + '</a>';
                        // the format/download column
                        rowData[4] = rowData[4] + '&nbsp;<a href="' + createLink('model', 'download', id) + '">' + i18n.model.list.download + '</a>';
                    }
                    fnCallback(json);
                    $('a.tooltip').cluetip({local: true, width: 550});
                }});
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
 * Loads the view to show a list of models.
 */
function showModelList() {
    $("#body").block();
    $.get(createLink("model", "index"), function(data) {
        $("#body").html(data);
        createModelDataTable();
        $("#body").unblock();
    });
}

/**
 * Loads the view to show a Model and replaces.
 * @param id The id of the Model to show
 */
function showModel(id) {
    $("#body").block();
    $.ajax({url: createLink("model", "show", id),
        success: function(data) {
            $("#body").html(data);
            $("#modelTabs").tabs({disabled: [1, 2, 3, 4, 5]});
            $("#modelTabs").show();
            $("#body").unblock();
        },
        error: function(jqXHR) {
            $("#body").unblock();
            handleError($.parseJSON(jqXHR.responseText));
        }});
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
 * Change listener for the upload model view.
 * It gets called whenever the Publication type radio button changes.
 * It adjusts the view to show/hide fields and enable/disable fields.
 */
function uploadModelPublicationChangeListener() {
    var value = $("input:radio[name=publicationType]:checked").val();
    switch (value) {
    case "PUBMED":
        enableElement("#model-upload-pubmed", true);
        enableElement("#model-upload-doi", false);
        enableElement("#model-upload-url", false);
        $("#model-upload-publication-table").fadeOut("fast");
        break;
    case "DOI":
        enableElement("#model-upload-pubmed", false);
        enableElement("#model-upload-doi", true);
        enableElement("#model-upload-url", false);
        $("#model-upload-publication-table").fadeIn("fast");
        break;
    case "URL":
        enableElement("#model-upload-pubmed", false);
        enableElement("#model-upload-doi", false);
        enableElement("#model-upload-url", true);
        $("#model-upload-publication-table").fadeIn("fast");
        break;
    case "UNPUBLISHED":
        enableElement("#model-upload-pubmed", false);
        enableElement("#model-upload-doi", false);
        enableElement("#model-upload-url", false);
        $("#model-upload-publication-table").fadeOut("fast");
        break;
    }
}

/**
 * Enable/Disable DOM element(s).
 * Adds/Removes the disabled attribute and sets appropriate CSS classes.
 * @param selector The selector string to identify the element(s)
 * @param enable @c true to enable it, @c false to disable it.
 */
function enableElement(selector, enable) {
    var element = $(selector);
    element.attr("disabled", !enable);
    if (enable) {
        element.removeClass("ui-state-disabled");
    } else {
        element.addClass("ui-state-disabled");
    }
}

/**
 * Callback when model upload form needs to be submitted.
 */
function uploadModel() {
    $("#model-upload-form").block();
    var data = $("#model-upload-form");
    $("#model-upload-form").ajaxSubmit({type: 'POST',
        url: createLink("model", "save"),
        // needs to be an iframe as we send a file
        iframe: true,
        dataType: 'json',
        success: function(data) {
        $("#model-upload-form").unblock();
        if (handleError(data)) {
            // TODO: with jquery 1.5 should be handled by status code function
            return;
        }
        if (data.error) {
            clearErrorMessages();
            showErrorMessage([data.model, data.name, data.comment, data.pubmed, data.doi, data.url]);
            setErrorState("#model-upload-file", data.model);
            setErrorState("#model-upload-name", data.name);
            setErrorState("#model-upload-comment", data.comment);
            setErrorState("#model-upload-pubmed", data.pubmed);
        } else if (data.success) {
            clearErrorMessages();
            showInfoMessage(i18n.model.upload.success.replace(/_ID_/, data.model.id), 20000);
            // TODO: change to Model view?
        }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            $("#model-upload-form").unblock();
            // the form is not submitted as AJAX (file upload) because of that we receive an html page
            // we need to extract the status code and error code from the html
            // and construct a proper object to pass to handleError()
            var response = $(jqXHR.responseText);
            var errorCode = $("#error-code", response).text();
            var statusCode = parseInt($("#status-code", response).text());
            handleError({error: statusCode, code: errorCode});
        }
    });
}

/**
 * Sets the state of widgets to error or removes the error state.
 * @param selector Selector string to identify the widget(s)
 * @param error @c true to set to error, @c false to remove error state
 */
function setErrorState(selector, error) {
    if (error) {
        $(selector).addClass("ui-state-error");
        if (selector[0] == '#') {
            $("label[for=\"" + selector.substring(1) + "\"]").addClass("ui-state-error-text");
        }
    } else {
        $(selector).removeClass("ui-state-error");
        if (selector[0] == '#') {
            $("label[for=\"" + selector.substring(1) + "\"]").removeClass("ui-state-error-text");
        }
    }
}

/**
 * Removes all shown error messages of the site error message div and hides it.
 * This method can be used to clear the current state before adding new error
 * messages with showErrorMessage().
 */
function clearErrorMessages() {
    $("#site-error-messages ul li").remove();
    $("#site-error-messages").hide();
}

/**
 * Adds @p message to the site info message div and shows it.
 * @param messages The message to show, String or Array of Strings
 * @param timeout Time in msec until the message is automatically removed. If not specified the message does not get removed.
 */
function showInfoMessage(messages, timeout) {
    showMessage(messages, timeout, $("#site-info-messages"));
}

/**
 * Adds an error message to the site error message div and shows it.
 * @param messages The message to show, String or Array of Strings
 * @param timeout Time in msec until the message is automatically removed. If not specified the message does not get removed.
 */
function showErrorMessage(messages, timeout) {
    showMessage(messages, timeout, $("#site-error-messages"));
}

/**
 * Adds @p message to the @p container and shows the @p container.
 * @param messages The message to show, String or Array of Strings
 * @param timeout Time in msec until the message is automatically removed. If not specified the message does not get removed.
 * @param container A jQuery object identifying the container, e.g. the site-error-messages or site-info-messages
 */
function showMessage(messages, timeout, container) {
    if (!messages) {
        return;
    }
    if (!(messages instanceof Array)) {
        messages = new Array(messages);
    }
    for (var i=0; i<messages.length; i++) {
        var message = messages[i];
        if (!message) {
            continue;
        }
        var error = $("<li>" + message + "</li>");
        // add close button to the error message
        var close = $("<a href=\"#\"></a>").button({text: false});
        close.button("option", "icons", {primary:'ui-icon-closethick'});
        error.append(close);
        // close button removes the error and if it was the last one hides the container
        close.click(function() {
            var parent = $(this).parent();
            parent.fadeOut("fast", function() {
                parent.remove();
                $("span[rel=icon]", container).position({my: "left", at: "left", of: container});
            });
            if ($("ul li", container).length == 1) {
                container.fadeOut("fast");
            }
        });
        if (timeout) {
            setTimeout(function() {close.trigger("click");}, timeout);
        }
        $("ul", container).append(error);
        close.hide();
    }

    // show the errors
    container.fadeIn("fast");
    $("span[rel=icon]", container).position({my: "left", at: "left", of: container});
    var closeButtons = $("ul li a", container);
    for (i=0; i<closeButtons.length; i++) {
        var button = $(closeButtons[i]);
        button.show();
        button.position({my: "right", at: "right", of: $(closeButtons[i]).parent(), collision: "flip"});
    }
}

/**
 * This method checks whether the passed in @p data describes a returned
 * error. If that is the case it will show an error message and return @c true.
 * @param data The JSON object
 * @returns @c true if the request contains an error, @c false otherwise
 * @todo With JQuery 1.5 we should use the statusCode property
 */
function handleError(data) {
    if (data.error && typeof(data.error) == "number") {
        clearErrorMessages();
        var errorMessage = "";
        switch (data.error) {
            case 403:
                errorMessage = i18n.error.denied;
                if (data.authenticated != undefined && data.authenticated == false) {
                    switchUserInformation(false);
                    showLoginDialog();
                }
                break;
            case 500:
                errorMessage = i18n.error.unexpected.replace(/_CODE_/, data.code);
                break;
        }
        showErrorMessage(errorMessage);
        return true;
    }
    return false;
}

/**
 * Global document initialization.
 * Connects all the global events like login/logout.
 */
$(document).ready(function() {
    $(document).bind("logout", function() {
        showInfoMessage(i18n.logout.successful, 20000);
        switchUserInformation(false);
    });
    $(document).bind("login", function(event, username) {
        showInfoMessage(i18n.login.successful, 20000);
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
    showModelList();
    $("#model-upload-form").ajaxForm();
    $("input:radio[name=publicationType]").change(uploadModelPublicationChangeListener);
});
