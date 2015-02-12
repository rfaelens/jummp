package net.biomodels.jummp.plugins.sbml

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(SbmlService)
class JSBMLSpec extends Specification {

    void "valid SBML models do not throw errors"(String fileName, def result) {
        expect:
        service != null

        when:
        def f = new File("test/files/$fileName".toString())
        def err = []
        def doc = service.getFileAsValidatedSBMLDocument(f, err)

        then:
        f.exists()
        err == []
        doc != null

        where:
        fileName                | result
        "BIOMD0000000272.xml"   | _
        "fbc_example1.xml"      | _
    }

    void "we can extract FBC-related stuff without errors"() {
        when:
        def f = new File("test/files/fbc_example1.xml")
        def doc = service.getFileAsValidatedSBMLDocument(f, [])
        def m = doc.getModel()

        then:
        m.isSetPlugin("fbc")

        when:
        def plugin = m.getPlugin("fbc")

        then:
        plugin != null

        when:
        def fluxBounds = plugin.getListOfFluxBounds()
        def fb = fluxBounds.get(0)

        then:
        1 == fluxBounds.size()
        "bound1" == fb.getId()
        "J0" == fb.getReaction()
        "EQUAL" == fb.getOperation().name()
        10 == fb.getValue()
    }
}
