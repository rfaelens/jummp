package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.trialdesign.CellDefnType
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Decorates the TrialDesignMatrix class with Iterator functionality.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public class TrialDesignMatrixIterator extends TrialDesignMatrix
        implements Iterator<CellDefnType> {

    private int previousIndex = -1
    private final TrialDesignMatrix MATRIX

    private static final Log log = LogFactory.getLog(this)

    private TrialDesignMatrixIterator() {
    }

    public TrialDesignMatrixIterator(TrialDesignMatrix matrix) throws IllegalArgumentException {
        if (!matrix) {
            throw new IllegalArgumentException(
                "TrialDesignMatrixIterator cannot iterate over an undefined matrix.")
        }
        MATRIX = matrix
    }

    public boolean hasNext() {
        if (!MATRIX.trialDesign) {
            return false
        }
        if (previousIndex == super.EPOCH_COUNT * super.ARMS_COUNT - 1) {
            // reset
            previousIndex = -1
            return false
        }
        return true
    }

    public CellDefnType next() {
        previousIndex++
        return trialDesign[previousIndex / super.ARMS_COUNT][previousIndex % super.ARMS_COUNT]
    }

    public void remove() {
        throw UnsupportedOperationException("Not allowed to remove clinical trial elements.")
    }
}
