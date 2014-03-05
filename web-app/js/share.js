var userNameList=[];
var toJSON = function(form) {
    return form.serializeArray().reduce(function(json,kv) {
      json[kv.name] = kv.value;
      return json;
    },{});
  };
var source = $('#collaborator-list-template').html();
var template = Handlebars.compile(source);
var Collaborator = Backbone.Model.extend({
	validate: function(attrs) {
		console.log(JSON.stringify(userNameList)+"..."+attrs.name);
		var findMe=_.where(userNameList, {"name": attrs.name}); 
		if (findMe.length==0) {
			alert("There is no user by that name");
			return "User not found";
		}
    }
});
var Collaborators = Backbone.Collection.extend({
		model: Collaborator
});
function makeBoolean(value) {
	value=value||false;
	if (value==="on") {
		value=true;
	}
	return value;
}

collaborators = new Collaborators;
CollaboratorTable = Backbone.View.extend({
    events: {
      "click .remove": "remove",
      "click .updateCollab": "updateCollab",
      "submit #collaboratorAddForm": "create"
    },
    initialize: function() {
      _.bindAll(this,"render","error");
      this.listenTo(this.collection,"add", this.render);
      this.listenTo(this.collection,"error",this.error);
      this.listenTo(this.collection,"update",this.render);
      this.render();
    },
    error: function(model,msg){
      alert(msg);
    },
    form: function() {
      return this.$("#collaboratorAddForm");
    },
    create: function(evt) {
      evt.preventDefault();
      objectCreated=toJSON(this.form());
      objectCreated.id=s4();
      objectCreated.read=makeBoolean(objectCreated.read);
      objectCreated.write=makeBoolean(objectCreated.write);
      console.log(JSON.stringify(collaborators));
      console.log(JSON.stringify(objectCreated));
      this.collection.add(objectCreated,{
        validate: true,
        error: this.error
      });
      console.log(this.collection);
      userNameList=_.reject(userNameList, function(checkMe){ return checkMe.name === objectCreated.name; });
    },
    updateCollab: function(evt) {
      evt.preventDefault();
      evt.stopPropagation();
      var buttonElement=$("#"+evt.currentTarget.id);
      var id=buttonElement.data("person");
      var field=buttonElement.data("field");
      var collab=this.collection.get(id);
      collab.set(field, buttonElement.is(':checked'));
      this.render();
    },
    update: function(evt) {
      evt.preventDefault();
      evt.stopPropagation();
      var form = $(evt.currentTarget);
      var id = form.attr("data-id");
      this.collection.get(id).save(toJSON(form),{
        error:this.error
      });
    },
    remove: function(evt) {
      evt.preventDefault();
      var buttonElement=$("#"+evt.currentTarget.id);
      var id=buttonElement.data("person");
      var nameRemoved=buttonElement.data("name");
      var collaborator=buttonElement.parent().parent();
      var removed=this.collection.remove(id);
      userNameList.push({name: nameRemoved});
      this.render();
    },
    render: function() {
      console.log('inside render with '+JSON.stringify(this.collection));
      var html = template(this.collection.toJSON());
      this.$el.html(html);
      _.each(this.collection, function(num) { 
      		  console.log(JSON.stringify(num))
      });
    }
});
collaboratorList = new CollaboratorTable({
    collection: collaborators
});

function s4() {
  return Math.floor((1 + Math.random()) * 0x10000)
             .toString(16)
             .substring(1);
};


function main(existing) {
	names=[{name:'mike'}, {name:'jim'},{name:'raza'}];
	userNameList=names;
	console.log(collaboratorList.el);
	$('#ui').html(collaboratorList.$el);
	_.each(existing, function(collab) {
			console.log(JSON.stringify(collab))
			collab.id=s4();
			collaborators.add(collab);
	});
}

