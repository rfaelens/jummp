var Jummp = Jummp || {};

Jummp.addRdfStatement = function(subject, predicate, object) {
    if (undefined === Jummp.data.existingAnnotations.subjects.theSubject.predicates) {
        Jummp.data.existingAnnotations.subjects.theSubject.predicates = []
    }
    var existing = Jummp.data.existingAnnotations;
    var existingSubject = existing.subjects.theSubject || {};
    var subjectPredicates = existingSubject.predicates || [];
    var predicateIdx = _.findIndex(subjectPredicates, { predicate: predicate });
    if (!object) {
        if (-1 != predicateIdx) {
            var removed = Jummp.data.existingAnnotations.subjects.theSubject.predicates.splice(
                    predicateIdx, 1);
            return;
        } else {
            return;
        }
    }
    var thisPredicate;
    if (-1 == predicateIdx) {
        thisPredicate = {
            predicate: predicate,
            object: object
        };
        Jummp.data.existingAnnotations.subjects.theSubject.predicates.push(thisPredicate);
    } else {
        thisPredicate = subjectPredicates[predicateIdx];
        if (!thisPredicate) {
            return;
        }
        thisPredicate.object = object;
        Jummp.data.existingAnnotations.subjects.theSubject.predicates[predicateIdx] = thisPredicate;
    }
    console.log("done with this stmt", Jummp.data.existingAnnotations.subjects.theSubject.predicates);
};

Jummp.convertToRdfObject = function(item) {
    return {
        "object": item
    };
};

Jummp.annoEditor = new Mn.Application();
Jummp.annoEditor.addRegions({
    mainRegion: "#leftContainer"
});

Jummp.annoEditor.on("start", function() {
    Backbone.history.start();
});

var AnnotationSection = Backbone.Model.extend({
    defaults: {
        name: 'example',
        id: 'ex'
    }
});

var AnnotationSections = Backbone.Collection.extend({ model: AnnotationSection });

var AnnotationProperty = Backbone.Model.extend({});

var AnnotationProperties = Backbone.Collection.extend({ model: AnnotationProperty });

var Value = Backbone.Model.extend({});
var Values = Backbone.Collection.extend({ model: Value });

// has to work with properties rather than values due to information structure.
var ValueContainerView = Mn.ItemView.extend({
    getTemplate: function() {
        var p = this.model;
        var range = p.get('range').name;
        switch(range) {
            case "SINGLE_UNCONSTRAINED_CHOICE":
                return "#single-unconstrained-value-template";
            case "MULTIPLE_UNCONSTRAINED_CHOICE":
                //FIXME return "#multiple-unconstrained-value-template";
                return "#single-unconstrained-value-template";
            case "SINGLE_CONSTRAINED_CHOICE":
                return "#single-constrained-value-template";
            case "MULTIPLE_CONSTRAINED_CHOICE":
                return "#multiple-constrained-value-template";
            default:
                var pName = p.get('value');
                console.warn("Sorry, I don't know how to display property", pName, "with range", range);
                console.warn("Rendering", pName, "as a free text field.");
                return "#single-unconstrained-value-template";
        }
    },

    ui: {
        multipleConstrainedValueOption: "select",
        singleUnconstrainedValueOption: "input[type=text]"
    },

    events: {
        "change @ui.multipleConstrainedValueOption": "multipleConstrainedValueChanged",
        "focusout @ui.singleUnconstrainedValueOption": "singleUnconstrainedValueChanged"
    },

    singleUnconstrainedValueChanged: function() {
        // get the value from the text box
        var newValue = this.$el.children().get(0).value;
        //  get the qualifier uri
        var uri = this.model.get('uri');
        console.log(uri, newValue);
        var rdfObject;
        if (!newValue) {
            rdfObject = newValue;
        } else {
            rdfObject = [Jummp.convertToRdfObject(newValue)]
        }
        Jummp.addRdfStatement("theSubject", uri, rdfObject);
    },

    multipleConstrainedValueChanged: function() {
        var uri = this.model.get('uri');
        var selectedOptions = this.$el.find('select option:selected');
        var objects = _.map(selectedOptions, function(option) {
            return Jummp.convertToRdfObject(option.value);
        });
        console.log(uri, objects);
        Jummp.addRdfStatement("theSubject", uri, objects);
    }
});

var PropertyView = Mn.LayoutView.extend({
    template: "#property-template",
    initialize: function() {
        this.addRegion("valueContainerRegion", this.$el.find(".valueContainer"));
    },
    onShow: function() {
        var view = new ValueContainerView({model: this.model});
        this.valueContainerRegion.show(view);
    }
});

var PropertiesView = Mn.CollectionView.extend({
    template: "#properties-template",
    childView: PropertyView
});

// holds the contents of one tab (a section)
var SectionView = Mn.LayoutView.extend({
    template: "#section-template",
    className: "sectionContainer",
    tagName: "div",

    initialize: function(options) {
        this.addRegion("propertyRegion", this.$el.find(".properties"));
    },

    onShow: function() {
        var properties = new PropertiesView({
            collection: this.model.get('annotationProperties')
        });
        this.propertyRegion.show(properties);
    }
});

// container for sections
var SectionsView = Mn.CollectionView.extend({
    template: "#sections-template",
    childView: SectionView
});

// represents one jQuery tab heading
var TabHeadingView = Mn.ItemView.extend({
    template: "#tab-heading-template",
    tagName: "li"
});

// holds the jQuery UI tab headings
var TabHeadingContainerView = Mn.CollectionView.extend({
    template: "#tab-headings-template",
    childView: TabHeadingView,
    tagName: "ul"
});

// the main layout consisting of the tabHeadingContainer and the sectionsView
var TabbedLayout = Mn.LayoutView.extend({
    template: "#root-section-template",
    annotationSections: [],

    initialize: function(options) {
        var options = options || {};
        this.annotationSections = options.annotationSections;
        this.addRegion("tabHeadingRegion", this.$el.find("#tabHeadings"));
        this.addRegion("sectionRegion", this.$el.find("#tabContent"));
    },

    onShow: function() {
        var headings = new TabHeadingContainerView({ collection: this.annotationSections });
        this.tabHeadingRegion.show(headings);
        var contents = new SectionsView({collection: this.annotationSections});
        this.sectionRegion.show(contents);
    }
});

Marionette.TemplateCache.prototype.compileTemplate = function(rawTemplate, options) {
    return Handlebars.compile(rawTemplate);
};

$(function() {
    $("#leftContainer").tabs();
});

Handlebars.registerHelper("renderSingleUnconstrainedValue", function() {
    var isRequired = Handlebars.escapeExpression(this.isRequired);
    var values = this.values ? this.values.first() : '';
    var isReadOnly = this.readOnly ? true : false;

    var result = "<input type='text' size='50'";
    if (isRequired) {
        result += " required";
    }
    if (isReadOnly) {
        result += " readonly";
    }
    if (values) {
        var label = values.get('xref').name;
        if (!label) {
            label = values.get('xref').uri;
        }
        result += "value='" + label + "'";
    }
    result += " />";
    return new Handlebars.SafeString(result);
});

// builds a select box that allows multiple choice. nested values will appear indented.
Handlebars.registerHelper("renderMultipleConstrainedValues", function() {
    var values = this.values;
    var result = "<select multiple >";
    values.each(function(value) {
        result = Jummp.buildValueTree(value, result);
    });
    result += "</select>";
    var escapedResult = new Handlebars.SafeString(result);
    return escapedResult;
});

Jummp.buildValueTree = function(item, partialValueTree, isLeaf) {
    partialValueTree = partialValueTree || "";
    isLeaf = isLeaf || false;
    var isSelected = item.get('options').selected || false;
    var optionValue = item.get('uri');
    var annotationLabel = item.get('value');
    var subValues = item.get('children') || [];

    var thisItem = "<option value='" + optionValue + "'";
    if (isSelected) {
        thisItem += " selected='selected'";
    }
    if (isLeaf) {
        annotationLabel = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + annotationLabel;
    }
    thisItem += ">" + annotationLabel + "</option>\n";
    partialValueTree += thisItem;
    if (subValues.length > 0) {
        subValues.each(function(child) {
            partialValueTree = Jummp.buildValueTree(child, partialValueTree, true);
        });
    }
    return partialValueTree;
};

