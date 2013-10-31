/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


<%=packageName ? "package ${packageName}\n\n" : ''%>

import org.junit.*
import grails.test.mixin.*

@TestFor(${className}Controller)
@Mock(${className})
class ${className}ControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/$propertyName/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.${propertyName}InstanceList.size() == 0
        assert model.${propertyName}InstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.${propertyName}Instance != null
    }

    void testSave() {
        controller.save()

        assert model.${propertyName}Instance != null
        assert view == '/${propertyName}/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/${propertyName}/show/1'
        assert controller.flash.message != null
        assert ${className}.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/${propertyName}/list'

        populateValidParams(params)
        def ${propertyName} = new ${className}(params)

        assert ${propertyName}.save() != null

        params.id = ${propertyName}.id

        def model = controller.show()

        assert model.${propertyName}Instance == ${propertyName}
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/${propertyName}/list'

        populateValidParams(params)
        def ${propertyName} = new ${className}(params)

        assert ${propertyName}.save() != null

        params.id = ${propertyName}.id

        def model = controller.edit()

        assert model.${propertyName}Instance == ${propertyName}
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/${propertyName}/list'

        response.reset()

        populateValidParams(params)
        def ${propertyName} = new ${className}(params)

        assert ${propertyName}.save() != null

        // test invalid parameters in update
        params.id = ${propertyName}.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/${propertyName}/edit"
        assert model.${propertyName}Instance != null

        ${propertyName}.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/${propertyName}/show/\$${propertyName}.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        ${propertyName}.clearErrors()

        populateValidParams(params)
        params.id = ${propertyName}.id
        params.version = -1
        controller.update()

        assert view == "/${propertyName}/edit"
        assert model.${propertyName}Instance != null
        assert model.${propertyName}Instance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/${propertyName}/list'

        response.reset()

        populateValidParams(params)
        def ${propertyName} = new ${className}(params)

        assert ${propertyName}.save() != null
        assert ${className}.count() == 1

        params.id = ${propertyName}.id

        controller.delete()

        assert ${className}.count() == 0
        assert ${className}.get(${propertyName}.id) == null
        assert response.redirectedUrl == '/${propertyName}/list'
    }
}
