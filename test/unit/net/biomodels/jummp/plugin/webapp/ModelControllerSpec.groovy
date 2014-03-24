package net.biomodels.jummp.plugin.webapp

import net.biomodels.jummp.core.ModelDelegateService
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PermissionTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.model.Revision

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(net.biomodels.jummp.webapp.ModelController)
@Mock([Revision])
class ModelControllerSpec extends Specification {
    def setup() {
    }

    def cleanup() {
    }

    void "share throws exception when called without a model revision id"() {
        when:
            controller.share()
        then:
            thrown(Exception)
    }

    void "share can handle rubbish input"() {
        given:
            def mds = mockFor(ModelDelegateService)
            mds.demand.getRevisionDetails() { skel ->
                new RevisionTransportCommand(
                    id: 1234,
                    name: "foobar",
                    model: new ModelTransportCommand(id: 1)
                )
            }
            controller.modelDelegateService = mds.createMock()
        when:
            controller.request.parameters = [id: "one"]
            controller.share()
        then:
            thrown(IllegalArgumentException)
    }

    void "share returns a model if there exists a Revision with the supplied id "() {
        given:
            def mds = mockFor(ModelDelegateService)
            mds.demand.getRevisionDetails() { skel ->
                new RevisionTransportCommand(
                    id: skel.id,
                    name: "mock model version",
                    model: new ModelTransportCommand(id: 1)
                )
            }
            mds.demand.getPermissionsMap() { id ->
                [ new PermissionTransportCommand(id: "0", name: "Me", read: true, write: true),
                    new PermissionTransportCommand(id: "1", name: "Myself", read: true),
                    new PermissionTransportCommand(id: "2", name: "I", read: true),
                ]
            }
            controller.modelDelegateService = mds.createMock()
        when:
            controller.request.parameters = [id: "1"]
            def model = controller.share()
        then:
            model != null
            def rev = model.revision
            rev.id == 1
            rev.name == "mock model version"
            grails.converters.JSON perms = model.permissions
            String jsonPerms = perms.toString(false)
            String expected = """\
[{"id":"0","name":"Me","read":true,"write":true},{"id":"1","name":"Myself","read":true,"write":false},{"id":"2","name":"I","read":true,"write":false}]"""
            jsonPerms == expected
    }
}
