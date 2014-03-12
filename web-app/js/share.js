var toJSON = function(form) {
    return form.serializeArray().reduce(function(json,kv) {
      json[kv.name] = kv.value;
      return json;
    },{});
  };
var source = $('#collaborator-list-template').html();
var template = Handlebars.compile(source);
var Collaborator = Backbone.Model.extend({
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
var lookupURL="";
var submitURL="";
var autoURL="";
var showURL="";
collaborators = new Collaborators;
CollaboratorTable = Backbone.View.extend({
    events: {
      "click .remove": "remove",
      "click .updateCollab": "updateCollab",
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
    submitData: function(evt) {
    	evt.preventDefault();
    	var collabString=JSON.stringify(this.collection);
    	$.post(submitURL, { collabMap: collabString }, 
      	  	function(returnedData){
      	  			if (returnedData.success) {
      	  				showNotification("Model sharing permissions updated");
    	  				window.location=showURL;
    	  			}
     	  			else {
     	  				showNotification(returnedData.message);
     	  				scheduleHide();
     	  			}
      	  	}
      );
    },
    create: function(evt) {
      evt.preventDefault();
      var objectCreated=toJSON($("#collaboratorAddForm"));
      objectCreated.read=makeBoolean(objectCreated.read);
      objectCreated.write=makeBoolean(objectCreated.write);
      if (objectCreated.write) {
				objectCreated.read=true;
	  }
      console.log(JSON.stringify(collaborators));
      console.log(JSON.stringify(objectCreated));
      var that = this;
      $.post(lookupURL, { name: objectCreated.name }, 
      	  	function(returnedData){
      	  			if (returnedData.found) {
      	  				objectCreated.id=returnedData.username;
      	  				collaborators.add(objectCreated);
      	  			}
     	  			else {
     	  				showNotification("Could not find a user by that name");
     	  				scheduleHide();
     	  			}
      	  	}
      );
    },
    updateCollab: function(evt) {
      evt.preventDefault();
      evt.stopPropagation();
      var buttonElement=$("#"+evt.currentTarget.id);
      var id=buttonElement.data("person");
      var field=buttonElement.data("field");
      var collab=this.collection.get(id);
      collab.set(field, buttonElement.is(':checked'));
      if (field==="write") {
      	  if (collab.get("write")) {
      	  	  collab.set("read", true);
      	  }
      }
      else {
      	  if (!collab.get("read")) {
      	  	  collab.set("write", false)
      	  }
      }
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
      this.render();
    },
    render: function() {
      console.log('inside render with '+JSON.stringify(this.collection));
      var html = template(this.collection.toJSON());
      this.$el.html(html);
      addButtonEvents();
    }
});
collaboratorList = new CollaboratorTable({
    collection: collaborators
});

function addButtonEvents() {
	  if (collaborators.length < 2) {
      	  $("#collabCreate").removeClass('hideRightBorder');
      	  $("#currentCollabs").addClass('hideLeftBorder');
      	  $("#collabUI").height($("#collabCreate").height());
      }
      else {
      	  $("#collabCreate").addClass('hideRightBorder');
      	  $("#currentCollabs").removeClass('hideLeftBorder');
      	  $("#collabUI").height($("#currentCollabs").height());
      }
 }


function s4() {
  return Math.floor((1 + Math.random()) * 0x10000)
             .toString(16)
             .substring(1);
};


function main(existing, contURL, submit, autoComp, show) {
	lookupURL=contURL
	submitURL=submit
	autoURL=autoComp
	showURL=show
	console.log(submitURL);
	console.log(collaboratorList.el);
	$('.containUI').html(collaboratorList.$el);
	_.each(existing, function(collab) {
			console.log(JSON.stringify(collab))
			if (collab.write) {
				collab.read=true;
			}
			collaborators.add(collab);
	});
	addButtonEvents();
	$( "#nameSearch" ).autocomplete({
    						source: autoURL,
    						minLength: 2
      });
      $( "#SaveCollabs" ).click(function(event){
    		collaboratorList.submitData(event);
      });
      $( "#AddButton" ).click(function(event){
    		collaboratorList.create(event);
      });
      
}

