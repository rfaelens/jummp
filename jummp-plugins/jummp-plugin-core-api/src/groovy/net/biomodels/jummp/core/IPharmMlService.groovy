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
* LibPharmml (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of LibPharmml used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import eu.ddmore.libpharmml.dom.PharmML
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinitionType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.trialdesign.PopulationType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType

/**
 * @short Interface describing the service to access a model encoded in PharmML.
 *
 * An implementation of this interface is provided by the PharmML plugin, however
 * this interface can be used to provide an alternative one.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
interface IPharmMlService {

    /**
     * @param dom an instance of eu.ddmore.libpharmml.dom.PharmML
     */
    public String getIndependentVariable(PharmML dom)

    /**
     * @param dom an instance of eu.ddmore.libpharmml.dom.PharmML
     */
    public List getFunctionDefinitions(PharmML dom)

    ModelDefinitionType getModelDefinition(PharmML dom)

    /**
     * @param dom an instance of eu.ddmore.libpharmml.dom.PharmML
     */
    List getCovariateModel(PharmML dom)

    /**
     * @param dom an instance of eu.ddmore.libpharmml.dom.PharmML
     */
    List getVariabilityModel(PharmML dom)

    /**
     * @param dom an instance of eu.ddmore.libpharmml.dom.PharmML
     */
    List getParameterModel(PharmML dom)

    /**
     * @param dom an instance of eu.ddmore.libpharmml.dom.PharmML
     */
    List getStructuralModel(PharmML dom)

    /**
     * @param dom an instance of eu.ddmore.libpharmml.dom.PharmML
     */
    List getObservationModel(PharmML dom)

    /**
     * @return eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
     */
    TrialDesignType getTrialDesign(PharmML dom)

    /**
     * @param design - an instance of eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
     * @return eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
     */
    TrialStructureType getTrialDesignStructure(TrialDesignType design)

    /**
     * @param design - an instance of eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
     */
    List getIndividualDosing(TrialDesignType design)

    /**
     * @param design an instance of eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
     * return an instance of eu.ddmore.libpharmml.dom.trialdesign.PopulationType
     */
    PopulationType getPopulation(TrialDesignType design)

    /**
     * @return an instance of eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
     */
    ModellingStepsType getModellingSteps(PharmML dom)

    /**
     * @param steps an instance of eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
     */
    List getCommonModellingSteps(ModellingStepsType steps)

    /**
     * @param steps an instance of eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
     */
    List getSimulationSteps(ModellingStepsType steps)

    /**
     * @param steps an instance of eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
     */
    List getEstimationSteps(ModellingStepsType steps)

    /**
     * @param steps an instance of eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
     * @return an instance of eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
     */
    StepDependencyType getStepDependencies(ModellingStepsType steps)
}
