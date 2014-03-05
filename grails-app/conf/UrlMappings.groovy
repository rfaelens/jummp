/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/





class UrlMappings {

	static mappings = {
		/*"/$controller/$action?/$id?(.${format})?"{
            constraints {
                // apply constraints here
            }*/
            "/$controller/$action?/$id?"{
            	constraints {
            		// apply constraints here
            	}
            }

	"/"(view:"/index")
	"/maintenance"(controller: 'maintenance')
	"/maintenance/turnOn"(controller: 'maintenance', action: 'turnOn')
	"403"(controller: "errors", action: "error403")
        "404"(controller: "errors", action: "error404")
        "500"(controller: "errors", action: "error500")
        "500"(controller: "errors", action: "error403", exception: org.springframework.security.access.AccessDeniedException)
        "/models"(controller: "search", action: "list")
	"/search"(controller: "search", action: "search")
	"/share"(controller: "model", action: "share")
	"/archive"(controller: "search", action: "archive")
	"/feedback"(controller:"jummp", action:"feedback")
	"/help"(controller:"jummp", action:"help")
	"/registration"(controller:"usermanagement", action:"create")
	"/forgotpassword"(controller:"usermanagement", action:"forgot")
	"/user/editUser"(controller:"usermanagement", action:"edit")
	"/user/editPassword"(controller:"usermanagement", action:"editPassword")
	"/user"(controller:"usermanagement", action:"show")
	"/model/create"(controller: "model", action: "create")
	"/model/$id"(controller:"model", action:"show")
	"/model/update"(controller: "model", action: "update")
	"/model/publish"(controller: "model", action: "publish")
	/*"/model/$id"(controller:"model") {
       action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
    }*/
	
        }
}
