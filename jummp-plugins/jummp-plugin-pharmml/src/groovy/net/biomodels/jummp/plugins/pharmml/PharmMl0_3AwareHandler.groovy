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
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition
import eu.ddmore.libpharmml.dom.modellingsteps.Estimation
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingSteps
import eu.ddmore.libpharmml.dom.modellingsteps.Simulation
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependency
import eu.ddmore.libpharmml.dom.trialdesign.Population
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesign
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructure
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
        return dom?.independentVariable?.symbId ?: "time"
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getFunctionDefinitions")
    List getFunctionDefinitions(PharmML dom) {
        return dom?.listOfFunctionDefinition
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getModelDefinition")
    ModelDefinition getModelDefinition(PharmML dom) {
        return dom.modelDefinition
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getCovariateModel")
    List getCovariateModel(ModelDefinition definition) {
        return definition?.listOfCovariateModel
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getVariabilityLevel")
    List getVariabilityModel(ModelDefinition definition) {
        return definition?.listOfVariabilityModel
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getParameterModel")
    List getParameterModel(ModelDefinition definition) {
        return definition?.listOfParameterModel
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getStructuralModel")
    List getStructuralModel(ModelDefinition definition) {
        return definition?.listOfStructuralModel
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getObservationModel")
    List getObservationModel(ModelDefinition definition) {
        return definition?.listOfObservationModel
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getTrialDesign")
    TrialDesign getTrialDesign(PharmML dom) {
        return dom?.trialDesign
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getTrialDesignStructure")
    TrialStructure getTrialDesignStructure(TrialDesign design) {
        return design?.structure
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getIndividualDosing")
    List getIndividualDosing(TrialDesign design) {
        return design?.individualDosing
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getPopulation")
    Population getPopulation(TrialDesign design) {
        return design?.population
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getModellingSteps")
    ModellingSteps getModellingSteps(PharmML dom) {
        return dom?.modellingSteps
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getCommonModellingSteps")
    List getCommonModellingSteps(ModellingSteps steps) {
        return steps?.commonModellingStep?.value ?: []
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getSimulationSteps")
    List getSimulationSteps(ModellingSteps steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof Simulation} : []
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getEstimationSteps")
    List getEstimationSteps(ModellingSteps steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof Estimation} : []
    }

    @Profiled(tag="pharmMl0_3AwareHandler.getStepDependencies")
    StepDependency getStepDependencies(ModellingSteps steps) {
        return steps?.stepDependencies
    }
}
