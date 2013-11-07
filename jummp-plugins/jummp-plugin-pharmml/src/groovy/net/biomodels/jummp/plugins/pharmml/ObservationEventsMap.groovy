package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.trialdesign.ArmDefnType
import eu.ddmore.libpharmml.dom.trialdesign.CellDefnType
import eu.ddmore.libpharmml.dom.trialdesign.EpochDefnType
import eu.ddmore.libpharmml.dom.trialdesign.ObservationsType
import eu.ddmore.libpharmml.dom.trialdesign.SegmentDefnType
import eu.ddmore.libpharmml.dom.trialdesign.StudyEventType
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Domain-specific language helper for representing ObservationsEvents in PharmML.
 * @see eu.ddmore.libpharmml.dom.trialdesign.StudyEventType
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public class ObservationEventsMap {
    private List<StudyEventType> studyEvents
    // [epochRef_armRef : <occasion_ref, variabilityLevel>]
    private Map<String, Map<String, String>> observationMap

    private static final Log log = LogFactory.getLog(this)

    public ObservationEventsMap( List<StudyEventType> studyEvents)
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
            if (s instanceof ObservationsType) {
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
