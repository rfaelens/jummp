<head>
    <meta name="layout" content="modelAnnotation"/>
    <script>
        var Jummp = Jummp || {};
        Jummp.data = {
            objectModel: ${objectModel},
            existingAnnotations: ${existingAnnotations},
            annoPropsMap: ${annoPropsMap}
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
    <h1>Annotate Model ${revision.model.publicationId ?: revision.model.submissionId}</h1>
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
            var setSelectedForList = function(values, s) {
                var v = values.findWhere({uri: s.uri});
                if (v != undefined) {
                    v.get('options').selected = true;
                }
            }

            var bind = function (prop, selection) {
                var propRange = prop.get('range').name;
                switch (propRange) {
                    case "SINGLE_UNCONSTRAINED_CHOICE":
                    case "MULTIPLE_UNCONSTRAINED_CHOICE":
                        _.each(selection, function(s) {
                            prop.get('values').push(new Value({xref: s}));
                        });
                        break;
                    case "SINGLE_CONSTRAINED_CHOICE":
                    case "MULTIPLE_CONSTRAINED_CHOICE":
                        _.each(selection, function(s) {
                            var values = prop.get('values');
                            setSelectedForList(values, s);
                            values.each(function(parent) {
                                var children = parent.get('children');
                                if (children != undefined) {
                                    setSelectedForList(children, s);
                                }
                            });
                        });
                        break;
                    default:
                        console.warn("unsupported property range type", propRange);
                }
            };
            annotationSections.each(function(section) {
                var props = section.get('annotationProperties');
                var annoProperties = new AnnotationProperties(props);
                section.set('annotationProperties', annoProperties);
                annoProperties.each(function(p) {
                    var uri = p.get('uri');
                    var vals = p.get('values');
                    var pValues = new Values(vals);
                    pValues.each(function(v) {
                        var children = v.get('children');
                        if ( children != undefined) {
                            v.set('children', new Values(children));
                        }
                    });
                    p.set('values', pValues);
                    var selection = Jummp.data.annoPropsMap[p.get('uri')];
                    Jummp.selection = selection;
                    console.log("selection for p ", p.get('uri'), selection);
                    if (selection) {
                        bind(p,selection);
                    }
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
            url: $.jummp.createLink("annotation", "update"),
            cache: false,
            data: {
                annotations: JSON.stringify(Jummp.data.existingAnnotations),
                revision: "${revision.model.publicationId ?: revision.model.submissionId}"
            },
            error: function(jqXHR) {
                console.error("epic fail", jqXHR.responseText);
                $("#message").addClass("failure");
                $("#message").removeClass("success");
                $('#message').html("There was an internal error while saving the updated annotations provided.");
            },
            success: function(response) {
                if ("200" == response.status) {
                    $("#message").addClass("success");
                    $("#message").removeClass("failure");
                } else {
                    $("#message").removeClass("success");
                    $("#message").addClass("failure");
                }
                $('#message').html(response.message);
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
                $("#message").addClass("failure");
                $("#message").removeClass("success");
                $('#message').html("There was an internal error while validating the information provided.");
            },
            success: function(response) {
                if ("200" == response.status) {
                    $("#message").addClass("success");
                } else {
                    $("#message").addClass("failure");
                }
                $('#message').html(response.message);
                if(response.errorReport!=null)
                    $('#report').html(response.errorReport);
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

