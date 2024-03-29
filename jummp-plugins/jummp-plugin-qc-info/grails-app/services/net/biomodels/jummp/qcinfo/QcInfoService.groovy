package net.biomodels.jummp.qcinfo

import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import org.perf4j.aop.Profiled
import org.springframework.security.access.AccessDeniedException

class QcInfoService {
    private final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()
    /**
    * Dependency Injection of Spring Security Service
    */
    def springSecurityService
    /**
     * Dependency Injection of certificationAuthorisationService
     */
    def certificationAuthorisationService
    /**
     * Dependency Injection of SearchService
     */
    def searchService

    public QcInfo createQcInfo(FlagLevel flagLevel, String comment) {
        QcInfo qcInfo = new QcInfo()
        qcInfo.flag = flagLevel
        qcInfo.comment = comment
        return qcInfo
    }

    @Profiled(tag="qcInfoService.addQcInfo")
    public boolean addQcInfo(Revision revision, QcInfo qcInfo) {
        if (!certificationAuthorisationService.isAllowed()) {
            throw new AccessDeniedException("You cannot certify this model.")
        }
        if (!revision) {
            throw new IllegalArgumentException("Cannot add QC information about a null revision")
        }
        if (revision.deleted) {
            throw new IllegalArgumentException("Cannot add QC information to deleted revision ${revision.id}.")
        }

        Revision.withTransaction {status ->
            try {
                revision.qcInfo = qcInfo
                qcInfo.save()
                revision.save()
                searchService.setCertified revision
                return true
            } catch (Exception ex) {
                final long id = revision.id
                log.error("Failed to save ${qcInfo.properties} for revision $id: ${ex.message}", ex)
                try {
                    status.setRollbackOnly()
                    searchService.setCertified(revision, false)
                } catch (Exception ex2) {
                    log.error("Could not roll back adding QCinfo to revision $id: ${ex2.message}", ex2)
                }
                return false
            }
        }
    }

    @Profiled(tag="qcInfoService.canCertify")
    public boolean canCertify(Model model) {
        if (!model) {
            throw new IllegalArgumentException("Model may not be null")
        }
        if (model.deleted) {
            return false
        }
        certificationAuthorisationService.isAllowed()
    }

    @Profiled(tag="qcInfoService.getQcInfo")
    public QcInfo getQcInfo(Revision revision) {
        return revision.qcInfo
    }
}
