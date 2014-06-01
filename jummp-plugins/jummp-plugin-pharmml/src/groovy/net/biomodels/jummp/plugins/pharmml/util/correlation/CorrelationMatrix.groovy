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

package net.biomodels.jummp.plugins.pharmml.util.correlation

/**
 * Simple POGO for storing correlations between random effects.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public class CorrelationMatrix {
    /**
     * The type of this correlation matrix - e.g. correlation, variance-covariance
     */
    private String type
    /**
     * The variability level for the random effects used in this matrix.
     */
    private String variabilityLevel
    /**
     * The random effects for used in this matrix.
     */
    private Set<String> randomEffects
    /**
     * A two-dimensional square matrix of Strings representing this correlation matrix.
     */
    private String[][] matrix = null
    /**
     * A map for storing the correlations between random effects.
     */
    private Map<String, String> matrixMap

    /**
     * Public constructor.
     *
     * @param t - the matrix type.
     * @param l - the variability level.
     */
    public CorrelationMatrix(String t, String l) {
        type = t
        variabilityLevel = l
        randomEffects = new HashSet<String>()
        matrixMap = new HashMap<String, String>()
    }

    /**
     * Public constructor.
     *
     * @param type - the matrix type.
     * @param level - the variability level.
     * @param randEff - the random effects which are used in this matrix.
     * @param mtrx - the matrix representation
     */
    public CorrelationMatrix(String type, String level, Set<String> randEff, String[][] mtrx) {
        this.type = type
        variabilityLevel = level
        if (randEff) {
            randomEffects.addAll randEff
        } else {
            randomEffects = new HashSet<String>()
        }
        if (mtrx) {
            matrix = mtrx
        } else {
            matrixMap = new HashMap<String, String>()
        }
    }

    /**
     * Add a correlation between two random effects to the matrix map.
     * @param key String of form variabilityLevel|effect1|effect2
     * @param value the value of the correlation.
     */
    protected void put(String key, String value) {
        if (!matrixMap) {
            matrixMap = new HashMap<String, String>()
        }
        if (!matrixMap[key] && value) {
            matrixMap[key] = value
        } else {
            log.error """
Refused to add <$key, $value> pair to correlation matrix $matrix and map $matrixMap"""
        }
    }

    /**
     * Returns the value of a correlation between to random effects.
     *
     * @param key String of form variabilityLevel|effect1|effect2
     * @return the value defined for @p key, or null if it has not been defined.
     */
    protected String get(String key) {
        if (key) {
            return matrixMap[key]
        }
        return null
    }

    /**
     * Returns the size of the correlation matrix based on the number of random effects defined.
     */
    protected int size() {
        return randomEffects?.size()
    }

    /**
     * Defines a new random effect for this correlation matrix.
     * @param re the random effect to define.
     */
    protected void addRandomEffect(String re) {
        if (re) {
            boolean randomEffectAdded = randomEffects.add re
            if (!randomEffectAdded) {
                log.warn "Random effect $re is defined multiple times."
            }
        } else {
            log.error "rejected $re in random effects $randomEffects of $this"
        }
    }

    // use def for the parameter to avoid casting issues
    public void setMatrix(def m) {
        if (!m || !m.getClass().isArray()) {
            return
        }
        final int SIZE = m.size()
        if (!matrix) {
            matrix = new String[SIZE][SIZE]
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                matrix[i][j] = m[i][j]
            }
        }
    }

    /**
     * @return a String representation for objects of this class.
     */
    public String toString() {
        return """\
CorrelationMatrix(type:$type,variability:$variabilityLevel,randomEffects:$randomEffects,matrix:$matrix,matrixMap:$matrixMap)"""
    }
}
