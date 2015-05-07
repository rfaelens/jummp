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

import eu.ddmore.libpharmml.dom.trialdesign.Observations
import eu.ddmore.libpharmml.dom.trialdesign.StudyEvent
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
/**
 * Domain-specific language helper for representing ObservationsEvents in PharmML.
 * @see eu.ddmore.libpharmml.dom.trialdesign.StudyEvent
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public class ObservationEventsMap {
    private List<StudyEvent> studyEvents
    // [epochRef_armRef : <occasion_ref, variabilityLevel>]
    private Map<String, Map<String, String>> observationMap

    private static final Log log = LogFactory.getLog(this)

    public ObservationEventsMap( List<StudyEvent> studyEvents)
            throws IllegalArgumentException {
        if (!studyEvents) {
            throw new IllegalArgumentException("The studyEvent must be specified.")
        }
        this.studyEvents = studyEvents
        observationMap = new TreeMap<String, TreeMap<String, String>>()
        studyEvents.each { jaxbStudy ->
            def s = jaxbStudy.value
            final String VAR = s.variabilityReference.symbRef.symbIdRef
            def arms = s.armRef.collect {it.oidRef}
            if (s instanceof Observations) {
                s.observationGroup.each { g ->
                    final String OID = g.oid
                    String e = g.epochRef.oidRef
                    arms.each { a ->
                        // using OID instead of ${OID} does not work.
                        observationMap["${e}_${a}"] =
                                new TreeMap([("${OID}".toString()) : VAR])
                    }
                }
            }
        }
    }

    public Map<String, Map<String, String>> getObservationMap() {
        return observationMap
    }

    public List<String> getArms() {
        return observationMap.keySet().collect{it.split("_")[1]}.unique()
    }

    public List<String> getEpochs() {
        return observationMap.keySet().collect{it.split("_")[0]}.unique()
    }

    public List<TreeMap<String, String>> findOccasionsByArm(String arm) {
        def result = []
        if (!arm) {
            return result
        }
        result.addAll(observationMap.findAll{it.key.contains("_${arm}")}.values())
        return result
    }
}
