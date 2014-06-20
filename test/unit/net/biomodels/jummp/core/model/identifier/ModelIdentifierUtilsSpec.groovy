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
 **/

package net.biomodels.jummp.core.model.identifier

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import net.biomodels.jummp.core.model.identifier.decorator.DateAppendingDecorator
import net.biomodels.jummp.core.model.identifier.decorator.FixedLiteralAppendingDecorator
import net.biomodels.jummp.core.model.identifier.decorator.VariableDigitAppendingDecorator
import net.biomodels.jummp.core.model.identifier.generator.DefaultModelIdentifierGenerator
import net.biomodels.jummp.core.model.identifier.generator.NullModelIdentifierGenerator
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class ModelIdentifierUtilsSpec {

    void testParseSettingsExpectsPartsToBeConsecutive() {
        def conf = '''
            model {
                submission {
                    id {
                        part1 {
                            type = "literal"
                            suffix = "MODEL"
                        }
                        part2 {
                            type = 'date'
                            suffix = 'dd'
                        }
                        part3 {
                            type = 'numerical'
                            width = 12
                        }
                    }
                }
            }
            database { //TODO MOVE TO H2
                username = 'jummp'
                password = 'jummpHigher'
                type = 'MYSQL'
                server = 'localhost'
                port = '3306'
                database = 'ddmore-live'
            }'''
        ConfigObject settings = new ConfigSlurper().parse(conf)
        try {
            ModelIdentifierUtils.processGeneratorSettings(settings)
            fail("The previous call should have thrown an exception.")
        } catch(Exception e) {
            String expected = """\
The configuration settings lack the rules for generating model identifiers!"""
            assertEquals(expected, e.message)
        }
    }

    void testParseSettingsExpectsPartsToStartFromOne() {
        ConfigObject settings = new ConfigObject()
        settings.model.id.submission.part2.type = 'literal'
        settings.model.id.submission.part2.suffix = 'MODEL'
        settings.database.username = 'jummp'
        settings.database.password = 'jummpHigher'
        settings.database.server = 'localhost'
        settings.database.port = '3306'
        settings.database.database = 'ddmore-live'
        settings.database.type = null
        try {
            ModelIdentifierUtils.processGeneratorSettings(settings)
            fail("The previous call should have thrown an exception.")
        } catch(Exception e) {
            String expected = """\
Model id part order invalid: Expected part1, not part2. Please review the settings for jummp.model.id and ensure that the defined identifier parts are in consecutive order."""
            assertEquals(expected, e.message)
        }
    }

    void testParseSettingsPrunesInvalidPartTypes() {
        ConfigObject settings = new ConfigObject()
        settings.model.id.submission.part1.type = 'unknown'
        settings.model.id.submission.part1.suffix = 'MODEL'
        settings.database.username = 'jummp'
        settings.database.password = 'jummpHigher'
        settings.database.server = 'localhost'
        settings.database.port = '3306'
        settings.database.database = 'ddmore-live'
        settings.database.type = null
        try {
            ModelIdentifierUtils.processGeneratorSettings(settings)
            fail("The previous call should have thrown an exception.")
        } catch(Exception e) {
            String expected = "Unknown model id part type for part1: unknown"
            assertEquals(expected, e.message)
        }
    }

    void testParseSettingsCreatesTheCorrectDecorators() {
        def conf = '''
            model {
                id {
                    submission {
                        part1 {
                            type = "literal"
                            suffix = "MODEL"
                        }
                        part2 {
                            type = 'date'
                            format = 'yyMMdd'
                        }
                        part3 {
                            type = 'numerical'
                            fixed = 'false'
                            width = '12'
                        }
                    }
                }
            }
            database {
                username = 'sa'
                password = ''
                type = 'h2'
                // fall back to an in-memory H2 database instance
            }'''
        ConfigObject settings = new ConfigSlurper().parse(conf)
        def results = ModelIdentifierUtils.processGeneratorSettings(settings)
        assertNotNull results
        def generators = [ 'submissionIdGenerator' : DefaultModelIdentifierGenerator.class,
                    'publicationIdGenerator' : NullModelIdentifierGenerator.class
        ]
        assertEquals generators.keySet(), results.keySet()
        assertEquals generators.size(), results.size()
        results.each { name, generator ->
            Class clazz = generators[name]
            assertEquals clazz, generator.getClass()
        }
        def actualDecorators = results.values().toList().first().DECORATOR_REGISTRY
        assertEquals 3, actualDecorators.size()
        def literalDecorator = actualDecorators.first()
        assertTrue literalDecorator instanceof FixedLiteralAppendingDecorator
        assertEquals 0, literalDecorator.ORDER
        assertEquals 'MODEL', literalDecorator.nextValue
        def dateDecorator = actualDecorators.getAt(1)
        assertTrue dateDecorator instanceof DateAppendingDecorator
        assertEquals 1, dateDecorator.ORDER
        String FORMAT = 'yyMMdd'
        assertEquals FORMAT, dateDecorator.FORMAT
        assertEquals new Date().format(FORMAT), dateDecorator.nextValue
        def numericalDecorator = actualDecorators.last()
        assertTrue numericalDecorator instanceof VariableDigitAppendingDecorator
        assertEquals 2, numericalDecorator.ORDER
        final int WIDTH = 12
        assertEquals WIDTH, numericalDecorator.WIDTH
        assertEquals "1".padLeft(12, '0'), numericalDecorator.nextValue
    }

    void testParseSettingsHasMandatoryConfigAttribute() {
        try {
            def results = ModelIdentifierUtils.processGeneratorSettings(null)
            fail("I was expecting an exception to be thrown!!")
        } catch (Exception e) {
            assertTrue(e instanceof Exception)
            String firstLine = e.message.split(System.properties["line.separator"])[0]
            String expected = "The settings for the model identification scheme are missing."
            assertEquals expected, firstLine
        }
    }
}
