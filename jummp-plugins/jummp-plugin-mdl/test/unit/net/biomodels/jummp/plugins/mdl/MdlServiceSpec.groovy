package net.biomodels.jummp.plugins.mdl

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(MdlService)
class MdlServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "areFilesThisFormat filters rubbish input"() {
        when:
        def files = null

        then:
        service.areFilesThisFormat(files) == false

        when:
        files = []

        then:
        service.areFilesThisFormat(files) == false

        when:
        files = [new File('non-existent.mdl')]

        then:
        files[0].exists() == false
        service.areFilesThisFormat(files) == false

    }

    void "areFilesThisFormat correctly recognises the target format"() {
        when:
        def files = [new File("../../test/files/BIOMD0000000272.xml")]

        then:
        files[0].exists() == true
        service.areFilesThisFormat(files) == false

        when:
        def fakeMdl = new File("test/files/fake.mdl")
        fakeMdl.append(new File("../../test/files/BIOMD0000000272.xml").text)

        then:
        service.areFilesThisFormat([fakeMdl]) == false
        fakeMdl.delete() == true

        when:
        def names = ["Alzheimer.mdl", "alzheimer_stub.csv"]
        files = names.collect { new File("test/files/alzheimer", it) }

        then:
        files.each { it.exists() == true }
        service.areFilesThisFormat(files) == true

    }

    void "filterMdlFiles works as expected"() {
        when:
        def files = null

        then:
        service.filterMdlFiles(files) == []

        when:
        files = []

        then:
        service.filterMdlFiles(files) == []

        when:
        def locations = ["alzheimer/Alzheimer.mdl",
                "alzheimer/alzheimer_stub.csv",
                "ogtt_igi/OGTT_IGI.mdl",
                "ogtt_igi/ogtt_igi_stub.csv",
                "warfarin_pk_bov/warfarin_PK_BOV.mdl",
                "warfarin_pk_bov/warfarin_conc_pca.csv",
        ]
        files = locations.collect { new File("test/files/${it}".toString()) }

        then:
        [
            "test/files/alzheimer/Alzheimer.mdl",
            "test/files/ogtt_igi/OGTT_IGI.mdl",
            "test/files/warfarin_pk_bov/warfarin_PK_BOV.mdl"
        ] == service.filterMdlFiles(files).collect { it.path }
    }

    void "filterDataFiles works as expected"() {
        when:
        def files = null

        then:
        service.filterDataFiles(files) == []

        when:
        files = []

        then:
        service.filterDataFiles(files) == []

        when:
        def locations = ["alzheimer/Alzheimer.mdl",
                "alzheimer/alzheimer_stub.csv",
                "ogtt_igi/OGTT_IGI.mdl",
                "ogtt_igi/ogtt_igi_stub.csv",
                "warfarin_pk_bov/warfarin_PK_BOV.mdl",
                "warfarin_pk_bov/warfarin_conc_pca.csv",
        ]
        files = locations.collect { new File("test/files/${it}".toString()) }

        then:
        [
            "test/files/alzheimer/alzheimer_stub.csv",
            "test/files/ogtt_igi/ogtt_igi_stub.csv",
            "test/files/warfarin_pk_bov/warfarin_conc_pca.csv"
        ] == service.filterDataFiles(files).collect { it.path }
    }
}
