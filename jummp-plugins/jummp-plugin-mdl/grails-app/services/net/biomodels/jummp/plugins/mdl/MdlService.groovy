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
    static transactional = false
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    public boolean areFilesThisFormat(List<File> modelFiles) {
        return !!(modelFiles?.find { f -> f.exists() && isMdlFile(f) })
    }

    public String getFormatVersion(RevisionTransportCommand revision) { return "5.0.8" }

    public boolean validate(List<File> modelFiles) { return true }

    public String extractName(List<File> modelFiles) { return "" }

    public String extractDescription(List<File> modelFiles) { return "" }

    public String getSearchIndexingContent(RevisionTransportCommand revision) { return "" }

    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) { return [] }

    public List<String> getPubMedAnnotation(RevisionTransportCommand revision) { return [] }

    public List getMdlFilesFromRevision(final RevisionTransportCommand REVISION) {
        return filterMdlFiles(extractFilesFromRevision(REVISION))
    }

    public List filterMdlFiles(final List<File> FILES) {
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

    public List getDataFilesFromRevision(final RevisionTransportCommand REVISION) {
        return filterDataFiles(extractFilesFromRevision(REVISION))
    }

    public List filterDataFiles(final List<File> FILES) {
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

    private boolean isMdlFile(final File FILE) {
        return FILE.name.endsWith(".mdl") && "text/plain" == detectMimeType(FILE)
    }

    private String detectMimeType(final File FILE) {
        def detector = new DefaultDetector()
        final String CONTENT_TYPE = detector.detect(new BufferedInputStream(
                new FileInputStream(FILE)), new Metadata()).toString()
        return CONTENT_TYPE
    }

    private List<File> extractFilesFromRevision(final RevisionTransportCommand REVISION) {
        List<File> files = REVISION?.files?.collect { rf ->
            final File FILE = new File(rf.path)
            if (FILE && FILE.exists() && FILE.canRead()) {
                return FILE
            }
        }
        return files
    }

    private boolean isDataFile(final File FILE) {
        final String FORMAT = detectMimeType(FILE)
        return ("text/csv" == FORMAT || "text/plain" == FORMAT) && FILE.name.endsWith(".csv")
    }

    public void setName(String name, RevisionTransportCommand revision) {}

    public void setDescription(String description, RevisionTransportCommand revision) {}
}
