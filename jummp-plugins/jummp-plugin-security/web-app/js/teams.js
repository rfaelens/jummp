var RETURN_KEY = 13;
/**
 * Variable that captures the tuple, consisting of the collaborator's name, id and email address,
 * that was selected by the user from the auto-complete field.
 * See share.js
 */
var selectedItem = selectedItem || ""

var collaborators = new Collaborators();
var memberSource = $('#team-member-template').html();
var teamSource = $('#team-members-template').html();

/**
 * The logic to present a collaborator.
 */
TeamMember = Backbone.View.extend({
     /** each collaborator is displayed as a row with class 'team-member'. */
    tagName: "tr",
    className: "team-member",
    /** the Mustache template used by this view */
    memberTpl: Handlebars.compile(memberSource),

    events: {
        /** When the button with class 'remove' is clicked, call the remove function. */
        "click .remove": "remove",
    },

    initialize: function(opts) {
        //make options available in the view.
        this.options = opts || {};
    },

    render: function() {
        this.$el.html(this.memberTpl(this.model.attributes));
        // be nice and allow chaining.
        return this;
    },

    remove: function(event) {
        var buttonId = event.currentTarget.id;
        //the id of the button is rmBtn-<id>
        var collaboratorId = buttonId.substring("rmBtn-".length);
        // TODO remove it from collection too.
        // this.collection.remove(id)
        this.parent().parent().remove();
        this.render();
    }
});

/**
 * Handles the presentation of a team.
 */
Team = Backbone.View.extend({
    el: '#members',
    tpl: Handlebars.compile(teamSource),

    initialize: function() {
        this.$membersTableBody = $('#membersTableBody');
        this.$nameSearch = $('#nameSearch');
        this.listenTo(collaborators, "add", this.addMember);
        /**
         * We issue a reset when the team information is first displayed.
         */
        this.listenTo(collaborators, "reset", this.addAll);
    },

    addMember: function(member) {
        var m = new TeamMember({ model: member });
        $('#membersTableBody').append(m.render().el);
    },

    addAll: function() {
        this.$('#membersTableBody').html('');
        collaborators.each(this.addMember, this);
    },

    render: function() {
        _.each(this.collection, function(c) {
            var memberView = new TeamMember();
            $('#membersTableBody').html(this.tpl(memberView.render().el));
        });
    }
});

// create a view.
teamMembers = new Team();
// add a new collaborator by pressing the return key (magic code 13)
$(function() {
    $('#nameSearch').keypress(function(e) {
        if (e.which == RETURN_KEY && $('#nameSearch').val().trim()) {
            e.preventDefault();
            var thisCollaborator = {};
            thisCollaborator.name = selectedItem[2];
            thisCollaborator.userId = selectedItem[1];
            // triggers Team.addMember()
            collaborators.add(thisCollaborator);
        }
    });
});
