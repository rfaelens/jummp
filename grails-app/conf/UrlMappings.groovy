class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
	"/"(view:"/index")
	"403"(controller: "errors", action: "error403")
        "404"(controller: "errors", action: "error404")
        "500"(controller: "errors", action: "error500")
        "500"(controller: "errors", action: "error403", exception: org.springframework.security.access.AccessDeniedException)
        "/models"(controller: "search", action: "list")
	"/models/model"(controller:"model", action="model")
	"/feedback"(controller:"jummp", action="feedback")
	"/"(view:"/index")
	"500"(view:'/error')
	
	
        }
}
