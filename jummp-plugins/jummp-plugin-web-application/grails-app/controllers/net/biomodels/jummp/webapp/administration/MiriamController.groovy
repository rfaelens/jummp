/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Grails, Spring Security (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Grails, Spring Security used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.webapp.administration

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import net.biomodels.jummp.core.miriam.MiriamUpdateException

@Secured('ROLE_ADMIN')
class MiriamController {
    def miriamService

    def index = { }

    def updateResources = { UpdateResourcesCommand cmd ->
        Map data = [:]
        if (cmd.hasErrors()) {
            data.put("error", true)
            if (cmd.errors.getFieldError("miriamUrl")) {
                String error = cmd.errors.getFieldError("miriamUrl").code
                if (error == "blank" || error == "url.invalid") {
                    data.put("miriamUrl", g.message(code: "miriam.update.url.invalid"))
                } else {
                    data.put("miriamUrl", g.message(code: "error.unknown", args: ["miriamUrl"]))
                }
            }
            render data as JSON
        } else {
            try {
                miriamService.updateMiriamResources(cmd.miriamUrl, cmd.force)
                data.put("success", true)
            } catch (MiriamUpdateException e) {
                data.put("error", g.message(code: "miriam.update.error"))
            }
            render data as JSON
        }
    }

    def updateMiriamData = {
        miriamService.updateAllMiriamIdentifiers()
        Map data = [success: true]
        render data as JSON
    }

    def updateModels = {
        miriamService.updateModels()
        Map data = [success: true]
        render data as JSON
    }
}

class UpdateResourcesCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String miriamUrl
    Boolean force

    static constraints = {
        miriamUrl(nullable: false, blank: false, url: true)
    }
}
