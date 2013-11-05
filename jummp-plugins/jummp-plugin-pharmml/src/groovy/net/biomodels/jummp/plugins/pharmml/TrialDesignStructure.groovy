package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.commontypes.OidRefType
import eu.ddmore.libpharmml.dom.trialdesign.ArmDefnType
import eu.ddmore.libpharmml.dom.trialdesign.CellDefnType
import eu.ddmore.libpharmml.dom.trialdesign.EpochDefnType
import eu.ddmore.libpharmml.dom.trialdesign.SegmentDefnType
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Domain-specific language helper for representing a Trial Design Structure in PharmML.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public class TrialDesignStructure {
    private List<ArmDefnType>               arms
    private List<EpochDefnType>             epochs
    private List<CellDefnType>              cells
    private List<SegmentDefnType>           segments
    private def cmp = [compare: { a,b -> a <=> b } ] as Comparator
    // [epochRef_armRef : segmentRef]
    private Map<String, List<String>>   trialDesignStructure

    private static final Log log = LogFactory.getLog(this)

    private TrialDesignStructure() {
        //no default constructor
        throw new UnsupportedOperationException("Do not use the default constructor.")
    }

    public TrialDesignStructure(final List ARMS, final List EPOCHS, final List CELLS,
            final List SEGMENTS) throws IllegalArgumentException {
        boolean shouldFail = !ARMS || !EPOCHS || !CELLS || !SEGMENTS
        if (shouldFail) {
            throw new IllegalArgumentException("Trial design structure cannot be empty.")
        }
        arms        = Collections.unmodifiableList(ARMS)
        epochs      = Collections.unmodifiableList(EPOCHS)
        cells       = Collections.unmodifiableList(CELLS)
        segments    = Collections.unmodifiableList(SEGMENTS)
        trialDesignStructure = new TreeMap<String, List<String>>(cmp)

        cells.each { c ->
            c.armRef.each { a ->
                String key = "${c.epochRef.oidRef}_${a.oidRef}"
                trialDesignStructure[key] = c.segmentRef.oidRef
            }
        }
        allCellsDefined()
    }

    public List<SegmentDefnType> findSegmentsByEpoch(String epoch) {
        if (!epoch) {
            log.error("Who is interested in the treatments over an undefined epoch?")
            return []
        }
        def refs = findMatchingReferences(epoch)
        return linkRefsToSegments(refs)
    }

    public List<String> findSegmentRefsByEpoch(String epoch) {
        if (!epoch) {
            log.error("Who is interested in treatment references over an undefined epoch?")
            return []
        }
        return findMatchingReferences(epoch)
    }

    public List<SegmentDefnType> findSegmentsByArm(String arm) {
        if (!arm) {
            log.error("Who is interested in the treatment over an undefined arm?")
            return []
        }
        def refs = findMatchingReferences(arm)
        return linkRefsToSegments(refs)
    }

    public List<String> findSegmentRefsByArm(String arm) {
        if (!arm) {
            log.error("Who is interested in treatment references over an undefined arm?")
            return []
        }
        return findMatchingReferences(arm)
    }

    public Iterator iterator() {
        return trialDesignStructure.entrySet().iterator()
    }

    public TreeSet<String> getArmRefs() {
        def result = new TreeSet(cmp)
        result.addAll(trialDesignStructure.keySet().collect{it.split("_")[1]})
        return result
    }

    public TreeSet<String> getEpochRefs() {
        def result = new TreeSet(cmp)
        result.addAll(trialDesignStructure.keySet().collect{it.split("_")[0]})
        return result
    }

    private boolean allCellsDefined() {
        if (trialDesignStructure.size() != epochs.size() * arms.size()) {
            log.error("The trial design does not cover all arms and all epochs.")
            return false
        }
        return true
    }

    private List findMatchingReferences(String elem) {
        return trialDesignStructure.findAll{it.key.contains(elem)}.values().flatten()
    }

    private List<SegmentDefnType> linkRefsToSegments(List segRefs) {
        return segRefs.collect { r -> segments.find { r.equals(it.oid) } }
    }
}
