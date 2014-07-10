/**
var source = $('#team-members-template').html();
var template = Handlebars.compile(source);
var User = Backbone.Model.extend({
    initialize: function(){
        console.log('This model has been initialized.');
        this.on('change', function(){
            console.log('- Values for this model have changed.');
        });
    }
});
var Users = Backbone.Collection.extend({
    model: User
});
userTable = new Users();
*/

TeamMembersView = Backbone.View.extend({
    events: {
        "click .remove": "remove",
    },
    render: function() {
        return this;
    }
});

collaborators = new Collaborators();
userList = new TeamMembersView({
    collection: collaborators
});
