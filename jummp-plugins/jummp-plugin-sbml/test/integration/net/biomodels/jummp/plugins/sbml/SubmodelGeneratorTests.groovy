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
* JSBML, Grails (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, GNU LGPL v2.1, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of JSBML, Grails used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.sbml

import grails.test.GrailsUnitTestCase
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.sbml.jsbml.*

class SubmodelGeneratorTests extends GrailsUnitTestCase {
    def sbmlService
    protected void setUp() {
        super.setUp()
        mockLogging(SubmodelGenerator)
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGenerateSubModel() {
        // generate new sub model
        Model subModel = new Model("testSubModel")
        // test sub model
        assertNotNull(subModel)
        assertEquals(subModel, new Model("testSubModel"))
        SBMLDocument subModelSbmlDocument = new SBMLDocument()
        assertNotNull(subModelSbmlDocument)
        subModelSbmlDocument.setLevel(2)
        assertEquals(2, subModelSbmlDocument.level)
        subModelSbmlDocument.setVersion(3)
        assertEquals(3, subModelSbmlDocument.version)
        subModelSbmlDocument.setModel(subModel)

        // example sbml model with component stubs to be tested
        String sbmlInput = '''<?xml version="1.0" encoding="UTF-8"?>
    <sbml xmlns="http://www.sbml.org/sbml/level2/version3" level="2" version="3">
      <model>
        <listOfCompartments>
          <compartment metaid="_274086" id="gut" size="1" sboTerm="SBO:0000290"></compartment>
          <compartment metaid="_274088" id="Vdopa" name="V_L_Dopa" size="0.496" sboTerm="SBO:0000290"></compartment>
        </listOfCompartments>
        <listOfSpecies>
          <species metaid="_274091" id="C_dopa" compartment="Vdopa" initialConcentration="0" sboTerm="SBO:0000247"></species>
          <species metaid="_274090" id="A_dopa" compartment="gut" hasOnlySubstanceUnits="true" sboTerm="SBO:0000247"></species>
        </listOfSpecies>
        <listOfParameters>
          <parameter metaid="metaid_0000003" id="env_M_ACT" name="env_M_ACT" value="60.05"/>
          <parameter metaid="metaid_0000004" id="env_M_GLC" name="env_M_GLC" value="180.156"/>
          <parameter metaid="metaid_0000005" id="env_uc" name="env_uc" value="9.5e-07"/>
        </listOfParameters>
        <listOfRules>
            <assignmentRule metaid="metaid_0000265" variable="k_bm_PG3"></assignmentRule>
          </listOfRules>
        <listOfReactions>
          <reaction metaid="_274093" id="l_dopa_absorption" name="L_Dopa absorption from gut" reversible="false" sboTerm="SBO:0000185"></reaction>
        </listOfReactions>
        <listOfReactants>
        <listOfEvents>
          <event metaid="_533425" name="first shift"></event>
          <event metaid="_533426" name="second shift">
            <listOfEventAssignments>
              <eventAssignment variable="GLC"></eventAssignment>
              <eventAssignment variable="ACT"></eventAssignment>
              <eventAssignment variable="BM"></eventAssignment>
            </listOfEventAssignments>
          </event>
        </listOfEvents>
          <speciesReference species="A_dopa"/>
        </listOfReactants>
        <listOfProducts>
          <speciesReference species="C_dopa"/>
        </listOfProducts>
      </model>
    </sbml>'''
        // put the example sbml into the cache
        RevisionTransportCommand revision = new RevisionTransportCommand(id: 1)
        SBMLDocument document = (new SBMLReader()).readSBMLFromStream(new ByteArrayInputStream(sbmlInput.bytes))
        sbmlService.cache.put(revision, document)
        SBMLDocument sbmlDocument = sbmlService.getFromCache(revision)
        Model existingModel = sbmlDocument.model
        assertEquals(existingModel.level, subModel.level)
        assertEquals(existingModel.version, subModel.version)

        // add species to new sub model
        List speciesIds = ["A_dopa", "C_dopa"]
        speciesIds.each { speciesId ->
            Species relatedSpecies = existingModel.getSpecies(speciesId)
            subModel.addSpecies(relatedSpecies)
        }
        // test if species are correctly inserted to new sub model
        assertNotNull(subModel.listOfSpecies)
        assertEquals(2, subModel.listOfSpecies.size())
        assertEquals("A_dopa", subModel.getSpecies("A_dopa").id.toString())
        assertEquals(subModel.getSpecies("A_dopa"), existingModel.getSpecies("A_dopa"))
        assertEquals("C_dopa", subModel.getSpecies("C_dopa").id.toString())
        assertEquals(subModel.getSpecies("C_dopa"), existingModel.getSpecies("C_dopa"))

        // add compartments to new sub model
        List compartmentIds = ["gut", "Vdopa"]
        compartmentIds.each { compartmentId ->
            Compartment relatedCompartment = existingModel.getCompartment(compartmentId)
            subModel.addCompartment(relatedCompartment)
        }
        // test if compartments are correctly inserted to new sub model
        assertNotNull(subModel.listOfCompartments)
        assertEquals(2, subModel.listOfCompartments.size())
        assertEquals("gut", subModel.getCompartment("gut").id.toString())
        assertEquals(subModel.getCompartment("gut"), existingModel.getCompartment("gut"))
        assertEquals("Vdopa", subModel.getCompartment("Vdopa").id.toString())
        assertEquals(subModel.getCompartment("Vdopa"), existingModel.getCompartment("Vdopa"))

        // add reaction to new sub model
        List reactionIds = ["l_dopa_absorption"]
        reactionIds.each { reactionId ->
            Reaction relatedReaction = existingModel.getReaction(reactionId)
            subModel.addReaction(relatedReaction)
        }
        // test if reaction is correctly inserted to new sub model
        assertNotNull(subModel.listOfReactions)
        assertEquals(1, subModel.listOfReactions.size())
        assertEquals("l_dopa_absorption", subModel.getReaction("l_dopa_absorption").id.toString())
        assertEquals(subModel.getReaction("l_dopa_absorption"), existingModel.getReaction("l_dopa_absorption"))

        // add rule to new sub model
        for(int i=0; i < existingModel.listOfRules.size(); i++) {
            Rule rule = existingModel.getRule(i)
            subModel.addRule(rule)
        }
        // test if rule is correctly inserted to new sub model
        assertNotNull(subModel.listOfRules)
        assertEquals(1, subModel.listOfRules.size())
        assertEquals("k_bm_PG3", subModel.getRule("k_bm_PG3").getVariable().toString())
        assertEquals(subModel.getRule("k_bm_PG3"), existingModel.getRule("k_bm_PG3"))

        // add events to new sub model
        for(int i=0; i < existingModel.listOfEvents.size(); i++) {
            Event event = existingModel.getEvent(i)
            subModel.addEvent(event)
        }
        // test if events are correctly inserted to new sub model
        assertNotNull(subModel.listOfEvents)
        assertEquals(2, subModel.listOfEvents.size())
        assertEquals(subModel.getEvent(0).id.toString(), existingModel.getEvent(0).id.toString())
        assertEquals(subModel.getEvent(0), existingModel.getEvent(0))
        assertEquals(subModel.getEvent(0).listOfEventAssignments, existingModel.getEvent(0).listOfEventAssignments)
        assertEquals(subModel.getEvent(1).id.toString(), existingModel.getEvent(1).id.toString())
        assertEquals(subModel.getEvent(1), existingModel.getEvent(1))
        assertEquals(subModel.getEvent(1).listOfEventAssignments, existingModel.getEvent(1).listOfEventAssignments)

        // add parameters of model in cache to new sub model
        existingModel.listOfParameters.each { parameter ->
            subModel.addParameter(parameter)
        }
        // test if parameters are correctly inserted to new sub model
        assertNotNull(subModel.listOfParameters)
        assertEquals(3, subModel.listOfParameters.size())
        assertEquals(subModel.getParameter(0).id.toString(), existingModel.getParameter(0).id.toString())
        assertEquals(subModel.getParameter(0), existingModel.getParameter(0))
        assertEquals(subModel.getParameter(1).id.toString(), existingModel.getParameter(1).id.toString())
        assertEquals(subModel.getParameter(1), existingModel.getParameter(1))
        assertEquals(subModel.getParameter(2).id.toString(), existingModel.getParameter(2).id.toString())
        assertEquals(subModel.getParameter(2), existingModel.getParameter(2))

        // add parameters of model in cache to new sub model (there are none)
        existingModel.listOfUnitDefinitions.each { unitDefinition ->
            subModel.addUnitDefinition(unitDefinition)
        }
        assertEquals(0, subModel.listOfUnitDefinitions.size())

        // add function definitions of model in cache to new sub model (there are none)
        existingModel.listOfFunctionDefinitions.each { functionDefinition ->
            subModel.addFunctionDefinition(functionDefinition)
        }
        assertEquals(0, subModel.listOfFunctionDefinitions.size())

        SBMLWriter sbmlWriter = new SBMLWriter()
        assertNotNull(sbmlWriter)
    }
}
