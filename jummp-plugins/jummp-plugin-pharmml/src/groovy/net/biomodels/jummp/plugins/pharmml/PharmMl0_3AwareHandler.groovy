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

import eu.ddmore.libpharmml.*
import eu.ddmore.libpharmml.dom.PharmML
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinitionType
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.trialdesign.PopulationType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
import eu.ddmore.libpharmml.impl.*
import net.biomodels.jummp.core.IPharmMlService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.perf4j.aop.Profiled

/**
 * IPharmMlService implementation for PharmML 0.3.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMl0_3AwareHandler extends AbstractPharmMlHandler {
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)
    /* lazy-loaded instance of this class.*/
    private static PharmMl0_3AwareHandler instance = null

    /* Enforce the singleton pattern by keeping the constructor private. */
    private PharmMl0_3AwareHandler() {}

    /*
     * Returns the singleton instance of this class.
     *
     * Uses a double-check approach to avoid unnecessary blocking in concurrent calls.
     */
    protected static PharmMl0_3AwareHandler getInstance() {
        if (instance == null) {
            synchronized(PharmMl0_3AwareHandler.class) {
                if (instance == null) {
                    instance = new PharmMl0_3AwareHandler()
                }
            }
        }
        return instance
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getIndependentVariable")
    String getIndependentVariable(PharmML dom) {
        return dom?.independentVariable.symbId
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getFunctionDefinitions")
    List getFunctionDefinitions(PharmML dom) {
        return dom?.functionDefinition
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getModelDefinition")
    ModelDefinitionType getModelDefinition(PharmML dom) {
        return dom.getModelDefinition()
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getCovariateModel")
    List getCovariateModel(ModelDefinitionType definition) {
        return definition?.getCovariateModel()
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getVariabilityLevel")
    List getVariabilityModel(ModelDefinitionType definition) {
        return definition?.getVariabilityModel()
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getParameterModel")
    List getParameterModel(ModelDefinitionType definition) {
        return definition?.getParameterModel()
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getStructuralModel")
    List getStructuralModel(ModelDefinitionType definition) {
        return definition?.getStructuralModel()
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getObservationModel")
    List getObservationModel(ModelDefinitionType definition) {
        return definition?.getObservationModel()
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getTrialDesign")
    TrialDesignType getTrialDesign(PharmML dom) {
        return dom?.trialDesign
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getTrialDesignStructure")
    TrialStructureType getTrialDesignStructure(TrialDesignType design) {
        return design?.structure
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getIndividualDosing")
    List getIndividualDosing(TrialDesignType design) {
        return design?.individualDosing
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getPopulation")
    PopulationType getPopulation(TrialDesignType design) {
        return design?.population
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getModellingSteps")
    ModellingStepsType getModellingSteps(PharmML dom) {
        return dom?.modellingSteps
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getCommonModellingSteps")
    List getCommonModellingSteps(ModellingStepsType steps) {
        return steps?.commonModellingStep?.value ?: []
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getSimulationSteps")
    List getSimulationSteps(ModellingStepsType steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof SimulationStepType} : []
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getEstimationSteps")
    List getEstimationSteps(ModellingStepsType steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof EstimationStepType} : []
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getStepDependencies")
    StepDependencyType getStepDependencies(ModellingStepsType steps) {
        return steps?.stepDependencies
    }
}
