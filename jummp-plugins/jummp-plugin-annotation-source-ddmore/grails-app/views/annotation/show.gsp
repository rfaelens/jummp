<head>
    <meta name="layout" content="modelAnnotation"/>
    <script>
        var Jummp = Jummp || {};
        Jummp.data = {
            objectModel: ${objectModel},
            existingAnnotations: ${existingAnnotations}
        }
    </script>

    <script id="single-unconstrained-value-template" type="text/x-handlebars-template">
        {{renderSingleUnconstrainedValue}}
    </script>
    <script id="multiple-unconstrained-value-template" type="text/x-handlebars-template">
        <div>your multiple input fields go here</div>
    </script>
    <script id="single-constrained-value-template" type="text/x-handlebars-template">
        <div>your radio goes here</div>
    </script>
    <script id="multiple-constrained-value-template" type="text/x-handlebars-template">
        {{renderMultipleConstrainedValues}}
    </script>
    <script id="property-template" type="text/x-handlebars-template">
        <h3>{{this.value}}</h3>
        <div class="valueContainer">{{this.uri}} : {{this.range.name}}</div>
        {{#if tooltip}}
            <div class="ui-state-highlight ui-state-cornerall info">
                <p>
                <span class="ui-icon ui-icon-info infoLabel"></span>
                    {{this.tooltip}}
                </p>
            </div>
        {{/if}}
    </script>
    <script type="text/x-handlebars-template" id="properties-template"></script>
    <script id="tab-heading-template" type="text/x-handlebars-template">
        <a href="#{{this.id}}">{{this.name}}</a>
    </script>
    <script id="tab-headings-template" type="text/x-handlebars-template"></script>
    <script id="section-template" type="text/x-handlebars-template">
        <div id="{{this.id}}">
            <div class="properties">
            </div>
        </div>
    </script>
    <script id="sections-template" type="text/x-handlebars-template"></script>
    <script id="root-section-template" type="text/x-handlebars-template">
        <div id="sectionRoot">
            <div id="tabHeadings"></div>
            <div id="tabContent"></div>
        </div>
    </script>
</head>
<body>
    <div id="message"></div>
    <div id="report"></div>
    <h1>Annotate Model ${revision.name} (${revision.model.publicationId ?: revision.model.submissionId})</h1>
    <div id="toolbar" class="ui-corner-all">
        <button id="saveButton" title="Save model properties" class="action">Save</button>
        <button id="validateButton" title="Validate model properties" class="action">Validate</button>
        <button id="backButton" title="Return to the model display page" class="action" onclick="return $.jummp.openPage('${g.createLink(controller: 'model', action: 'show', id: modelId)}')">Return to model display page</button>
    </div>
    <div id="annotationEditor">
        <div id="leftContainer">
        </div>
        <div id="rightContainer"></div>
        <div class="resetFloat"></div>
    </div>
    <script type="text/javascript">
        var Jummp = Jummp || {};
        Jummp.annoEditor.on("before:start", function(options) {
            var annotationSections = new AnnotationSections(Jummp.data.objectModel);
            Jummp.annotationSections = annotationSections;
            annotationSections.each(function(section) {
                var props = section.get('annotationProperties')
                var annoProperties = new AnnotationProperties(props);
                section.set('annotationProperties', annoProperties);
                annoProperties.each(function(p) {
                    var vals = p.get('values');
                    var pValues = new Values(vals);
                    pValues.each(function(v) {
                        var children = v.get('children');
                        if ( children != undefined) {
                            v.set('children', new Values(children));
                        }
                    });
                    p.set('values', pValues);
                });
            });
            var sectionTabs = new TabbedLayout({annotationSections: annotationSections});
            Jummp.annoEditor.mainRegion.show(sectionTabs);
        });

        Jummp.annoEditor.start();
</script>
<g:javascript>
    /* toolbar button icons */
    $('#saveButton').button({
        icons: {
            primary: "ui-icon-check"
        }
    }).on("click", function(event) {
        "use strict";
        event.preventDefault();
        $.ajax({
            dataType: "json",
            type: "GET",
            url: $.jummp.createLink("annotation", "save"),
            cache: false,
            data: {
                annotations: JSON.stringify(Jummp.data.existingAnnotations),
                revision: "${revision.model.publicationId ?: revision.model.submissionId}"
            },
            beforeSend: function() {
                console.log("Disable Save, Validate and Return to model display page (Back) buttons while saving annotations into database");
                $("#message").addClass("failure");
                $('#message').html('Annotations are being saved into database. Please waiting a while...');
                $('#saveButton').attr('disabled',true);
                $('#validateButton').attr('disabled',true);
                $('#backButton').attr('disabled',true);
            },
            error: function(jqXHR) {
                console.error("epic fail", jqXHR.responseText);
                $("#message").removeClass("success").addClass("failure");
                $('#message').html("There was an internal error while saving the information provided.");
                console.log("Enable Save, Validate and Return to model display page (Back) buttons - due to errors and let modify");
                $('#saveButton').removeAttr('disabled');
                $('#validateButton').removeAttr('disabled');
                $('#backButton').removeAttr('disabled');
            },
            success: function(response) {
                if ("200" == response.status) {
                    $("#message").removeClass("failure").addClass("success");
                } else {
                    $("#message").removeClass("success").addClass("failure");
                }
                $('#message').html(response.message);
                console.log("Enable Save, Validate and Return to model display page (Back) buttons - success but want to modify");
                $('#saveButton').removeAttr('disabled');
                $('#validateButton').removeAttr('disabled');
                $('#backButton').removeAttr('disabled');
            }
        });
        $('#report').empty();
    });
    $('#validateButton').button({
        icons: {
            primary: "ui-icon-star"
        }
    }).on("click", function(event) {
        "use strict";
        event.preventDefault();
        $.ajax({
            dataType: "json",
            type: "GET",
            url: $.jummp.createLink("annotation", "validate"),
            cache: false,
            data: {
                annotations: JSON.stringify(Jummp.data.existingAnnotations),
                revision: "${revision.model.publicationId ?: revision.model.submissionId}"
            },
            error: function(jqXHR) {
                console.error("epic fail", jqXHR.responseText);
                $("#message").removeClass("success").addClass("failure");
                $('#message').html("There was an internal error while validating the information provided.");
            },
            success: function(response) {
                if ("200" == response.status) {
                    $("#message").removeClass("failure").addClass("success");
                } else {
                    $("#message").removeClass("success").addClass("failure");
                    $("#message").removeClass("success").addClass("failure");
                }
                $('#message').html(response.message);
                if(response.errorReport!=null){
                    $('#report').html(response.errorReport);
                }else{
                    $('#report').empty();
                }

            }
        });
    });
    $('#backButton').button({
        icons: {
            primary: "ui-icon-close"
        }
    });
</g:javascript>
<script>
    /* display all options at all times by setting the size of the select box */
    var selects = $('select');
    var selectCount = selects.length;
    var i;
    for(i = 0; i < selectCount; i++) {
        var thisSelect = selects[i];
        var actualLength = thisSelect.length;
        thisSelect.setAttribute('size', "" + actualLength);
    }

</script>
</body>

