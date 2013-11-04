package net.biomodels.jummp.plugins.pharmml

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import eu.ddmore.libpharmml.dom.trialdesign.ArmDefnType
import eu.ddmore.libpharmml.dom.trialdesign.CellDefnType
import eu.ddmore.libpharmml.dom.trialdesign.EpochDefnType

/**
 * Domain-specific language helper for representing a Trial Structure in PharmML.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
protected class TrialDesignMatrix {
    private List<ArmDefnType>   arms
    private List<EpochDefnType> epochs
    private List<CellDefnType>  cells
    private CellDefnType[][]    trialDesign
    private final int EPOCH_COUNT
    private final int ARMS_COUNT
    private Map<String, List<String>> segments

    private static final Log log = LogFactory.getLog(this)

    protected TrialDesignMatrix() {
        //no default constructor
    }

    public TrialDesignMatrix(final List ARMS, final List EPOCHS, final List CELLS)
            throws IllegalArgumentException {
        boolean shouldFail = !ARMS || !EPOCHS || !CELLS
        if (shouldFail) {
            throw new IllegalArgumentException("Cannot construct trial design matrix with empty structure.")
        }
        arms        = Collections.unmodifiableList(ARMS)
        epochs      = Collections.unmodifiableList(EPOCHS)
        cells       = Collections.unmodifiableList(CELLS)
        EPOCH_COUNT = epochs.size()
        ARMS_COUNT  = arms.size()
        trialDesign = new String[EPOCH_COUNT][ARMS_COUNT]
        cells.each { c ->
            c.armRef.each { a ->
                trialDesign[c.epochRef][a] = c
            }
        }
        allCellsFilled(trialDesign)
    }

    public TrialDesignMatrixIterator iterator() {
        return new TrialDesignMatrixIterator(this)
    }

    private boolean allCellsFilled(CellDefnType[][] aMatrix) {
        boolean allFilled = true
        for (int i=0; i < EPOCH_COUNT; i++) {
            for (int j=0; j < ARMS_COUNT; j++) {
                if (!trialDesign[i][j]) {
                    final StringBuilder ERR = new StringBuilder()
                    ERR.append("Undefined cell in the trial design for arm ")
                    ERR.append(arms[j]).append(" in epoch ").append(epoch[i])
                    log.error(ERR.toString())
                    allFilled  = false
                }
            }
        }
        return allFilled
    }
}
