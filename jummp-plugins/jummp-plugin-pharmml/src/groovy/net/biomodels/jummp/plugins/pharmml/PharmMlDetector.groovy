/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
* Apache Tika, Apache Commons (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Tika, Apache Commons used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.pharmml

import java.nio.file.Files
import java.nio.file.Path
import net.biomodels.jummp.core.RunnableModelFormatDetector
import net.biomodels.jummp.core.util.JummpXmlUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata

public class PharmMlDetector implements RunnableModelFormatDetector {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()
    /**
     * The file to perform the checks on.
     */
    private final File modelFile
    private boolean isPharmML
    private boolean hasBeenRun

    /**
     * Constructs a PharmMlDetector for @p file
     * @param file The file whose model format should be determined.
     */
    public PharmMlDetector(final File file) {
        modelFile = file
    }

    /*
     * Keep default constructor private.
     */
    private PharmMlDetector() {
    }

    /**
     * @see java.lang.Runnable#run()
     */
    void run() {
        isRecognisedFormat(modelFile)
    }

    /**
     * Determines whether @p theFile appears to be a model in PharmML.
     * This method makes no guarantee that the file is a valid PharmML document.
     * @param theFile the file on which to perform the test.
     * @return true if the file contains a PharmML document, false otherwise.
     * @see net.biomodels.jummp.core.RunnableModelFormatDetector#isRecognisedFormat(File)
     */
    public boolean isRecognisedFormat(final File theFile) {
        if (!theFile || !theFile.canRead()) {
            hasBeenRun = true
            isPharmML = false
            return false
        }
        return hasBeenRun ? isPharmML : determineFormat(theFile)
    }

    private boolean determineFormat(final File theFile) {
        //validation has already been done
        assert theFile && theFile.canRead()
        def sherlock = new DefaultDetector()
        final String CONTENT_TYPE = sherlock.detect(new BufferedInputStream(
                new FileInputStream(theFile)), new Metadata()).toString()
        if ("application/xml".equals(CONTENT_TYPE)) {
            String acceptedNs = JummpXmlUtils.findModelAttribute(theFile, "PharmML", "xmlns")
            if (!acceptedNs) {
                if (IS_INFO_ENABLED) {
                    log.info "File ${theFile.name} does not include PharmML namespace declaration."
                }
                hasBeenRun = true
                isPharmML = false
                return false
            }
            //set these to these true so that we don't need to run multiple times
            hasBeenRun = true
            isPharmML = true
            return true
        } else {
            if (IS_INFO_ENABLED) {
                log.info "File ${theFile.name} is not of type application/xml, but ${CONTENT_TYPE}."
            }
        }
        hasBeenRun = true
        isPharmML = false
        return false
    }
}
