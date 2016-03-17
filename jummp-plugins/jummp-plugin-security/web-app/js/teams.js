var RETURN_KEY = 13;
/**
 * Variable that captures the tuple, consisting of the collaborator's name, id and email address,
 * that was selected by the user from the auto-complete field.
 * See share.js
 */
var selectedItem = selectedItem || ""

var collaborators = new Collaborators;
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

    initialize: function(opts) {
        //make options available in the view.
        this.options = opts || {};
    },

    render: function() {
        this.$el.html(this.memberTpl(this.model.attributes));
        // be nice and allow chaining.
        return this;
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
        this.listenTo(this.collection, "add", this.render);
        this.listenTo(this.collection, "remove", this.render);
        this.listenTo(this.collection,"update",this.render);
        /**
         * We issue a reset when the team information is first displayed.
         */
        this.listenTo(this.collection, "reset", this.addAll);
    },

    addMember: function(member) {
        var m = new TeamMember({ model: member });
        $('#membersTableBody').append(m.render().el);
       	$('#nameLabel').show();
    },

    addAll: function() {
        this.$('#membersTableBody').html('');
        collaborators.each(this.addMember, this);
    },

    render: function() {
    	$('#membersTableBody').html('');
    	if (this.collection.length == 0) {
    		$('#nameLabel').hide();
    	}
    	else {
    		$('#nameLabel').show();
    		var that = this;
    		this.collection.forEach(function(c) {
    			var memberView = new TeamMember({model: c});
				$('#membersTableBody').append(memberView.render().el);
				$("#remove-"+c.attributes.userId).click(function(e) {
					that.remove(e);
        		});
			});
			
		}
    }, 
    remove: function(event) {
    	var buttonId = event.currentTarget.id;
        var collaboratorId = buttonId.substring("remove-".length);
        var toRemove  = this.collection.find(function(element) {
        		return element.attributes.userId == collaboratorId
        });
        this.collection.remove(toRemove);
        this.render();
    }
});

// create a view.
teamMembers = new Team({collection: collaborators});
// add a new collaborator by pressing the return key (magic code 13) or by the add button
function startTeams(teamsUrl, successUrl, existing) {
	$('#nameSearch').keypress(function(e) {
        if (e.which == RETURN_KEY && $('#nameSearch').val().trim()) {
        	addCollab(e);
        }
    });
    $('#add').click(function(e) {
    	if ($('#nameSearch').val().trim()) {
        	addCollab(e);
        }
    });
    $('.submitButton').click(function(e) {
    		e.preventDefault();
    		var teamData = {};
    		teamData['name']=$("#teamName").val();
    		teamData['description']=$("#teamDescription").val();
    		teamData['members']=collaborators;
    		$.post(teamsUrl, {teamData: JSON.stringify(teamData)}, function(returnedData) {
    			if (returnedData.indexOf("Error") == -1) {
                	window.location = successUrl+"/"+returnedData;
            	}
            	else {
                	showNotification(returnedData);
                }
            });
    });
    _.each(existing, function(collab) {
        if (collab.write) {
            collab.read=true;
        }
        collaborators.add(collab);
    });
    teamMembers.render();
}

function addCollab(e) {
    e.preventDefault();
    var thisCollaborator = {};
    thisCollaborator.name = selectedItem[2];
    thisCollaborator.userId = selectedItem[1];
    // triggers Team.addMember()
    collaborators.add(thisCollaborator);
}