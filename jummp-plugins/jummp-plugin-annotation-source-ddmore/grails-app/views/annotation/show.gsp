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
    <h1>Annotate Model ${revision.model.publicationId ?: revision.model.submissionId}</h1>
    <div id="toolbar" class="ui-corner-all">
        <button id="saveButton" title="Save model properties" class="action">Save</button>
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
            error: function(jqXHR) {
                console.error("epic fail", jqXHR.responseText);
                $("#message").addClass("failure");
                $('#message').html("There was an internal error while saving the information provided.");
            },
            success: function(response) {
                if ("200" == response.status) {
                    $("#message").addClass("success");
                } else {
                    $("#message").addClass("failure");
                }
                $('#message').html(response.message);
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
        var actualLength = 1 + thisSelect.length;
        thisSelect.setAttribute('size', "" + actualLength);
    }

</script>
</body>

