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
* Apache Tika, Apache Commons, LibPharmml, Perf4j (or a modified version of these
* libraries), containing parts covered by the terms of Apache License v2.0,
* the licensors of this Program grant you additional permission to convey the
* resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Tika, Apache Commons,
* LibPharmml, Perf4j used as well as that of the covered work.}
**/

package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.PharmML
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingSteps
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.trialdesign.PopulationType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.perf4j.aop.Profiled

/**
 * IPharmMlService implementation for PharmML 0.2.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMl0_2AwareHandler extends AbstractPharmMlHandler {
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)
    /* lazy-loaded instance of this class.*/
    private static PharmMl0_2AwareHandler instance = null

    /* Enforce the singleton pattern by keeping the constructor private. */
    private PharmMl0_2AwareHandler() {}

    /*
     * Returns the singleton instance of this class.
     *
     * Uses a double-check approach to avoid unnecessary blocking in concurrent calls.
     */
    protected static PharmMl0_2AwareHandler getInstance() {
        if (instance == null) {
            synchronized(PharmMl0_2AwareHandler.class) {
                if (instance == null) {
                    instance = new PharmMl0_2AwareHandler()
                }
            }
        }
        return instance
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getIndependentVariable")
    String getIndependentVariable(PharmML dom) {
        return dom?.independentVariable.symbId
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getFunctionDefinitions")
    List getFunctionDefinitions(PharmML dom) {
        return dom?.getListOfFunctionDefinition()
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getModelDefinition")
    ModelDefinition getModelDefinition(PharmML dom) {
        return dom.getModelDefinition()
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getCovariateModel")
    List getCovariateModel(ModelDefinition definition) {
        return definition?.getListOfCovariateModel()
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getVariabilityLevel")
    List getVariabilityModel(ModelDefinition definition) {
        return definition?.getListOfVariabilityModel()
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getParameterModel")
    List getParameterModel(ModelDefinition definition) {
        return definition?.getListOfParameterModel()
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getStructuralModel")
    List getStructuralModel(ModelDefinition definition) {
        return definition?.getListOfStructuralModel()
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getObservationModel")
    List getObservationModel(ModelDefinition definition) {
        return definition?.getListOfObservationModel()
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getTrialDesign")
    TrialDesignType getTrialDesign(PharmML dom) {
        return dom?.trialDesign
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getTrialDesignStructure")
    TrialStructureType getTrialDesignStructure(TrialDesignType design) {
        return design?.structure
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getIndividualDosing")
    List getIndividualDosing(TrialDesignType design) {
        return design?.individualDosing
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getPopulation")
    PopulationType getPopulation(TrialDesignType design) {
        return design?.population
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getModellingSteps")
    ModellingSteps getModellingSteps(PharmML dom) {
        return dom?.modellingSteps
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getCommonModellingSteps")
    List getCommonModellingSteps(ModellingSteps steps) {
        return steps?.commonModellingStep?.value ?: []
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getSimulationSteps")
    List getSimulationSteps(ModellingSteps steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof SimulationStepType} : []
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getEstimationSteps")
    List getEstimationSteps(ModellingSteps steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof EstimationStepType} : []
    }

    @Profiled(tag="pharmMl0_2AwareHandler.getStepDependencies")
    StepDependencyType getStepDependencies(ModellingSteps steps) {
        return steps?.stepDependencies
    }
}
