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
     * The individual parameters associated with the random effects from this matrix.
     */
    private Map<String, String> individualParameters
    /**
     * A two-dimensional square matrix of Strings representing this correlation matrix.
     */
    private String[][] matrix = null

    private Map<String, String> matrixMap

    /*
     * Public constructor.
     *
     * @param t - the matrix type.
     * @param l - the variability level.
     */
    public CorrelationMatrix(String t, String l) {
        type = t
        variabilityLevel = l
        individualParameters = new HashMap<String, String>()
        matrixMap = new HashMap<String, String>()
    }

    /**
     * Public constructor.
     *
     * @param type - the matrix type.
     * @param level - the variability level.
     * @param indParams - the individual parameters which are used in this matrix.
     * @param mtrx - the matrix representation
     */
    public CorrelationMatrix(String type, String level, Map<String, String> indParams,
                String[][] mtrx) {
        this.type = type
        variabilityLevel = level
        if (indParams) {
            individualParameters.addAll indParams
        } else {
            individualParameters = new HashMap<String, String>()
        }
        if (mtrx) {
            matrix = mtrx
        } else {
            matrixMap = new HashMap<String, String>()
        }
    }

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

    protected String get(String key) {
        if (key) {
            return matrixMap[key]
        }
        return null
    }

    protected int size() {
        return individualParameters?.size()
    }

    protected void addIndividualParameter(String re, String ip) {
        if (re && ip && !individualParameters[re]) {
            individualParameters[re] = ip
        } else {
            log.error "rejected $re,$ip in param map $individualParameters of $this"
        }
    }

    // use def for the parameter to avoid casting issues
    public void setMatrix(def m) {
        if (!m || !m.getClass().isArray()) {
            return
        }
        final int SIZE = m.size()
        println "size: $SIZE"
        if (!matrix) {
            matrix = new String[SIZE][SIZE]
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                matrix[i][j] = m[i][j]
            }
        }
    }

    public String toString() {
        return """\
CorrelationMatrix(type:$type,variability:$variabilityLevel,indivParams:$individualParameters,matrix:$matrix,matrixMap:$matrixMap)"""
    }
}
