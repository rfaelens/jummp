class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		"/models"(controller: "search", action: "list")
		"/models/model"(controller:"model", action="model")

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
