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
* Apache Tika, Apache Commons, LibPharmml, Perf4j (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Tika, Apache Commons, LibPharmml, Perf4j used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.*
import eu.ddmore.libpharmml.dom.PharmML
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.trialdesign.PopulationType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
import eu.ddmore.libpharmml.impl.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicBoolean
import net.biomodels.jummp.core.IPharmMlService
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.util.JummpXmlUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.perf4j.aop.Profiled

/**
 * Service class containing the logic to handle models encoded in PharmML.
 * @see net.biomodels.jummp.core.model.FileFormatService
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMlService implements FileFormatService, IPharmMlService {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    @Profiled(tag="pharmMlService.validate")
    public boolean validate(List<File> model) {
        long start = System.nanoTime()
        if (!model) {
            if (IS_INFO_ENABLED) {
                log.info "Refusing to validate an undefined list of files as a PharmML submission."
            }
            return false
        }
        model = model.findAll{it && it.canRead()}
        if (IS_INFO_ENABLED) {
            log.info "Validating ${model.inspect()} as a PharmML submission."
        }
        File pharmMlFile = findPharmML(model)
        if (!pharmMlFile) {
            if (IS_INFO_ENABLED) {
                log.info("No PharmML file found in ${model.inspect()}, hence nothing to validate.")
            }
            return false
        }
        if (IS_INFO_ENABLED) {
            log.info "Asking libPharmML to validate ${pharmMlFile}."
        }
        assert pharmMlFile!= null && pharmMlFile.exists() && pharmMlFile.canRead()
        long step1 = System.nanoTime()
        if (IS_INFO_ENABLED) {
            log.info"Pre-validation checks completed in ${(step1-start)/1000000.0}ms."
        }
        LibPharmMLImpl api = PharmMlFactory.getInstance().createLibPharmML()
        def stream = null
        IPharmMLResource resource = null
        try {
            stream = new BufferedInputStream(new FileInputStream(pharmMlFile))
            resource = api.createDomFromResource(stream)
        } catch(IOException x) {
            log.error(x.message, x)
        } finally {
            stream?.close()
        }
        long step2 = System.nanoTime()
        if (IS_INFO_ENABLED) {
            log.info("libPharmML validation took ${(step2-step1)/1000000.0}ms.")
        }
        IValidationReport report = resource.getCreationReport()
        if (IS_INFO_ENABLED) {
            final int ERR_COUNT = report.numErrors()
            if (ERR_COUNT) {
                def err = []
                Iterator<IValidationError> iErr = report.errorIterator()
                while (iErr.hasNext()) {
                    IValidationError e = iErr.next()
                    err << new StringBuffer(e.getRuleId()).append(':')
                            .append(e.getErrorMsg()).append("\n").toString()
                }
                log.info(err.inspect())
            }
        }
        if (IS_INFO_ENABLED) {
            log.info "Validation report check took ${(System.nanoTime()-step2)/1000000.0}ms."
        }
        return report.isValid()
    }

    @Profiled(tag="pharmMlService.extractName")
    public String extractName(List<File> model) {
        if (!model) {
            return ""
        }
        def sherlock = new DefaultDetector()
        def noMetadata = new Metadata()
        model = model.findAll{ f ->
            if (!f.canRead()) {
                log.error "Cannot read file ${f.inspect()} while extracting PharmML model name."
                return false
            }
            String mime = sherlock.detect(new BufferedInputStream(new FileInputStream(f)), noMetadata).toString()
            return "application/xml".equals(mime)
        }
        String theName = model.inject(new StringBuilder()) { name, f ->
            name.append(JummpXmlUtils.findModelElement(f, "Name")).append(" ")
        }.toString().trim()
        if (IS_INFO_ENABLED) {
            log.info("PharmML model ${model.inspect()} is entitled ${theName}")
        }
        return theName
    }

    @Profiled(tag="pharmMlService.getSearchIndexingContent")
    public String getSearchIndexingContent(RevisionTransportCommand revision) {
        return ""
    }

    /**
     * Retrieves the description element from a file encoded PharmML.
     *
     * The implementation may be subject to change if the description contains additional XML elements.
     * @see net.biomodels.jummp.core.model.FileFormatService#extractDescription(List)
     */
    @Profiled(tag="pharmMlService.extractDescription")
    public String extractDescription(final List<File> model) {
        if (!model) {
            return ""
        }
        String theDescription = model.inject(new StringBuilder()) { desc, m ->
            desc.append(getDomFromPharmML(m).description?.value ?: "").append(
                    System.properties["line.separator"])
        }.toString().trim()
        if (IS_INFO_ENABLED) {
            log.info("PharmML model ${model.inspect()} has description ${theDescription}.")
        }
        return theDescription
    }

    @Profiled(tag="pharmMlService.getAllAnnotationURNs")
    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) {
        return []
    }

    @Profiled(tag="pharmMlService.getPubMedAnnotation")
    public List<String> getPubMedAnnotation(RevisionTransportCommand revision) {
        return []
    }

    /**
     * Detects whether the supplied files are in the format supported by this Service
     * @param files a list of files that should be checked
     * @return true if all files are supported, false otherwise.
     * @see net.biomodels.jummp.core.model.FileFormatService#areFilesThisFormat(List files)
     */
    @Profiled(tag="pharmMlService.areFilesThisFormat")
    public boolean areFilesThisFormat(List<File> files) {
        if (!files) {
            return false
        }
        files = files.findAll {it && it.canRead()}
        def fileQueue = new ConcurrentLinkedQueue(files)
        def outcomes = new AtomicBoolean(false)
        def iFiles = fileQueue.iterator()
        def threadFactory = Executors.defaultThreadFactory()
        while (iFiles.hasNext()) {
            final File theFile = iFiles.next()
            final PharmMlDetector detector = new PharmMlDetector(theFile)
            def modelDetectorThread = threadFactory.newThread(detector)
            if (modelDetectorThread) {
                if (IS_INFO_ENABLED) {
                    log.info("Testing ${theFile.name} for pharmML.")
                }
                modelDetectorThread.setName("pharmML detector for ${theFile.name}")
                modelDetectorThread.start()
                modelDetectorThread.join()
                if (detector.isRecognisedFormat(theFile)) {
                    if (IS_INFO_ENABLED) {
                        log.info("Found PharmML namespace in ${theFile.name}")
                    }
                    return true
                }
            } else {
                //todo retry
                log.error("Cannot start a PharmML detector thread for ${theFile.properties}")
            }
        }
        return false
    }

    @Profiled(tag="pharmMlService.getFormatVersion")
    public String getFormatVersion(RevisionTransportCommand revision) {
        if (!revision) {
            log.error "Cannot get PharmML model format version from undefined revision."
            return ""
        }
        assert revision.format.identifier == "PharmML"
        List<File> revisionFiles = fetchMainFilesFromRevision(revision)
        final File pharmML = findPharmML(revisionFiles)
        PharmML dom =  getDomFromPharmML(pharmML)
        return dom?.writtenVersion
    }

    @Profiled(tag="pharmMlService.getDomFromRevision")
    PharmML getDomFromRevision(RevisionTransportCommand revision) {
        if (!revision) {
            log.error "Cannot get PharmML model definition from undefined revision."
            return ""
        }
        assert revision.format.identifier == "PharmML"
        List<File> revisionFiles = fetchMainFilesFromRevision(revision)
        final File pharmML = findPharmML(revisionFiles)
        return getDomFromPharmML(pharmML)
    }

    @Profiled(tag="pharmMlService.getIndependentVariable")
    String getIndependentVariable(PharmML dom) {
        return dom?.independentVariable.symbId
    }

    //todo change display format
    @Profiled(tag="pharmMlService.getFunctionDefinitions")
    List getFunctionDefinitions(PharmML dom) {
        return dom?.functionDefinition
    }

    @Profiled(tag="pharmMlService.getModelDefinition")
    List getModelDefinition(RevisionTransportCommand revision) {
        PharmML dom = getDomFromRevision(revision)
        return dom.getModelDefinition()
    }

    @Profiled(tag="pharmMlService.getCovariateModel")
    List getCovariateModel(PharmML dom) {
        return dom?.getModelDefinition().getCovariateModel()
    }

    @Profiled(tag="pharmMlService.getVariabilityLevel")
    List getVariabilityModel(PharmML dom) {
        return dom?.getModelDefinition().getVariabilityModel()
    }

    @Profiled(tag="pharmMlService.getParameterModel")
    List getParameterModel(PharmML dom) {
        return dom?.getModelDefinition().getParameterModel()
    }

    @Profiled(tag="pharmMlService.getStructuralModel")
    List getStructuralModel(PharmML dom) {
        return dom?.getModelDefinition().getStructuralModel()
    }

    @Profiled(tag="pharmMlService.getObservationModel")
    List getObservationModel(PharmML dom) {
        return dom?.getModelDefinition().getObservationModel()
    }

    @Profiled(tag="pharmMlService.getTrialDesign")
    TrialDesignType getTrialDesign(RevisionTransportCommand revision) {
        PharmML dom = getDomFromRevision(revision)
        return dom?.trialDesign
    }

    @Profiled(tag="pharmMlService.getTrialDesignStructure")
    TrialStructureType getTrialDesignStructure(TrialDesignType design) {
        return design?.structure
    }

    @Profiled(tag="pharmMlService.getIndividualDosing")
    List getIndividualDosing(TrialDesignType design) {
        return design?.individualDosing
    }

    @Profiled(tag="pharmMlService.getPopulation")
    PopulationType getPopulation(TrialDesignType design) {
        return design?.population
    }

    @Profiled(tag="pharmMlService.getModellingSteps")
    ModellingStepsType getModellingSteps(RevisionTransportCommand revision) {
        PharmML dom = getDomFromRevision(revision)
        return dom?.modellingSteps
    }

    @Profiled(tag="pharmMlService.getCommonModellingSteps")
    List getCommonModellingSteps(ModellingStepsType steps) {
        return steps?.commonModellingStep?.value ?: []
    }

    @Profiled(tag="pharmMlService.getSimulationSteps")
    List getSimulationSteps(ModellingStepsType steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof SimulationStepType} : []
    }

    @Profiled(tag="pharmMlService.getEstimationSteps")
    List getEstimationSteps(ModellingStepsType steps) {
        def allSteps = getCommonModellingSteps(steps)
        return allSteps ? allSteps.findAll {it instanceof EstimationStepType} : []
    }

    @Profiled(tag="pharmMlService.getStepDependencies")
    StepDependencyType getStepDependencies(ModellingStepsType steps) {
        return steps?.stepDependencies
    }

    /*
     * Helper function that finds the PharmML file from a selection of files.
     *
     * @param  submission the list of files containing a PharmML file.
     * @return the PharmML file, or null if @p submission had no PharmML files.In the case of
     * multiple PharmML files being present, only the first one is returned.
     */
    @Profiled(tag="pharmMlService.findPharmML")
    private File findPharmML(List<File> submission) {
        def fileQueue = new ConcurrentLinkedQueue(submission)
        AtomicBoolean stillLooking = new AtomicBoolean(true)
        ThreadFactory threadFactory = Executors.defaultThreadFactory()
        def iFiles = fileQueue.iterator()
        final File pharmMlFile = null
        while (iFiles.hasNext() && stillLooking.get()) {
            final File file = iFiles.next()
            PharmMlDetector detective = new PharmMlDetector(file)
            Thread detectiveThread = threadFactory.newThread(detective)
            if (detectiveThread) {
                detectiveThread.setName("pharmML validation for ${file.name}")
                detectiveThread.start()
                detectiveThread.join()
                if (detective.isRecognisedFormat(file)) {
                    stillLooking.set(true)
                    pharmMlFile = file
                }
            } else {
                log.error "pharmMlService.validate: Cannot start detection thread for file ${file.name}"
                return pharmMlFile
            }
        }

        if (!pharmMlFile && IS_INFO_ENABLED) {
            log.info "No PharmML to validate in ${submission.inspect()}"
        }
        return pharmMlFile
    }

    @Profiled(tag="pharmMlService.fetchMainFilesFromRevision")
    private List<Files> fetchMainFilesFromRevision(RevisionTransportCommand rev) {
        List<File> files = []
        List<File> locations = []
        rev?.files?.findAll{it.mainFile}.each{locations << it.path}
        locations.each { l ->
            File f = new File(l)
            if (f && f.exists() && f.canRead()) {
                files << f
            }
        }
        return files
    }

    @Profiled(tag="pharmMlService.getDomFromPharmML")
    private PharmML getDomFromPharmML(File f) {
        LibPharmMLImpl api = PharmMlFactory.getInstance().createLibPharmML()
        def stream = null
        IPharmMLResource resource = null
        try {
            stream = new BufferedInputStream(new FileInputStream(f))
            resource = api.createDomFromResource(stream)
        } catch(IOException x) {
            log.error(x.message, x)
        } finally {
            stream?.close()
        }
        return resource.getDom()
    }
}
