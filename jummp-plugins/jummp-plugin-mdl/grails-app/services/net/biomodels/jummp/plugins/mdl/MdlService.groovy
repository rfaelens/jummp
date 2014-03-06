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
**/

package net.biomodels.jummp.plugins.mdl

import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.util.JummpXmlUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.perf4j.aop.Profiled

/**
 * Service class containing the logic to handle models encoded in MDL.
 * @see net.biomodels.jummp.core.model.FileFormatService
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class MdlService implements FileFormatService {
    /**
     * Disable the default transactional behaviour of Grails Services.
     */
    static transactional = false
    /**
     * The logger for this class.
     */
    private static final Log log = LogFactory.getLog(this)
    /**
     * Indicates whether the granularity of the log level is high or low.
     */
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    /**
     *
     */
    @Override
    public boolean areFilesThisFormat(List<File> modelFiles) {
        return !!(modelFiles?.find { f -> f.exists() && isMdlFile(f) })
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatVersion(RevisionTransportCommand revision) { return "5.0.8" }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<File> modelFiles) { return true }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractName(List<File> modelFiles) { return "" }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractDescription(List<File> modelFiles) { return "" }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSearchIndexingContent(RevisionTransportCommand revision) { return "" }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) { return [] }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPubMedAnnotation(RevisionTransportCommand revision) { return [] }

    /**
     * Filters the MDL files from a given revision.
     * @param REVISION the revision from which to extract the MDL files.
     * @return The matching list of files, or an empty list if there were no matches.
     */
    public List<File> getMdlFilesFromRevision(final RevisionTransportCommand REVISION) {
        return filterMdlFiles(extractFilesFromRevision(REVISION))
    }

    /**
     * Filters the MDL files from a given list of @p FILES.
     * @param FILES the list from which to extract the MDL files.
     * @return The matching list of files, or an empty list if there were no matches.
     */
    public List<File> filterMdlFiles(final List<File> FILES) {
        def mdlFiles = []
        if (!FILES) {
            return mdlFiles
        }
        FILES.inject(mdlFiles) { list, f ->
            if (f && f.exists() && f.canRead() && isMdlFile(f)) {
                list.add(f)
            }
            return list
        }
        return mdlFiles
    }

    /**
     * Filters the CSV files from a given @p REVISION.
     * @param REVISION the revision from which to extract the CSV files.
     * @return The matching list of files, or an empty list if there were no matches.
     */
    public List<File> getDataFilesFromRevision(final RevisionTransportCommand REVISION) {
        return filterDataFiles(extractFilesFromRevision(REVISION))
    }

    /**
     * Filters the CSV files from the supplied @p FILES.
     * @param FILES the list of files which should be filtered.
     * @return The matching list of files, or an empty list if there were no matches.
     */
    public List<File> filterDataFiles(final List<File> FILES) {
        def csvFiles = []
        if (!FILES) {
            return csvFiles
        }
        FILES.inject(csvFiles) { result, f ->
            if (f && f.exists() && f.canRead() && isDataFile(f)) {
                result.add(f)
            }
            return result
        }
        return csvFiles
    }

    /*
     * Simple filter to detect whether @p FILE is an MDL file or not.
     * The criteria include an ".mdl" extension for the @p FILE, and the mime type
     */
    private boolean isMdlFile(final File FILE) {
        return FILE.name.endsWith(".mdl") && "text/plain" == detectMimeType(FILE)
    }

    /*
     * Detects the mime type for a given @p FILE
     */
    private String detectMimeType(final File FILE) {
        def detector = new DefaultDetector()
        final String CONTENT_TYPE = detector.detect(new BufferedInputStream(
                new FileInputStream(FILE)), new Metadata()).toString()
        return CONTENT_TYPE
    }

    /*
     * Convenience method for retrieving the files from a supplied @p REVISION.
     */
    private List<File> extractFilesFromRevision(final RevisionTransportCommand REVISION) {
        List<File> files = REVISION?.files?.collect { rf ->
            final File FILE = new File(rf.path)
            if (FILE && FILE.exists() && FILE.canRead()) {
                return FILE
            }
        }
        return files
    }

    /*
     * Checks whether the supplied @p FILE adheres to the CSV "standard".
     */
    private boolean isDataFile(final File FILE) {
        final String FORMAT = detectMimeType(FILE)
        return ("text/csv" == FORMAT || "text/plain" == FORMAT) && FILE.name.endsWith(".csv")
    }
}
