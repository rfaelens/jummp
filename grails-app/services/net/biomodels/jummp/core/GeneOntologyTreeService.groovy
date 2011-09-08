package net.biomodels.jummp.core

import net.biomodels.jummp.core.miriam.GeneOntology
import net.biomodels.jummp.core.miriam.GeneOntologyRelationship
import net.biomodels.jummp.core.miriam.GeneOntologyTreeLevel
import net.biomodels.jummp.core.miriam.GeneOntologyRelationshipType
import org.springframework.security.acls.domain.BasePermission
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import net.biomodels.jummp.model.Revision
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.springframework.security.core.userdetails.UserDetails
import org.perf4j.aop.Profiled

/**
 * @short Service to retrieve Gene Ontology Tree information.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class GeneOntologyTreeService {
    /**
     * Dependency Injection of Spring Security Service
     */
    def springSecurityService
    /**
     * Dependency Injection of AclUtilService
     */
    def aclUtilService

    static transactional = true

    /**
     * Retrieves the next GO tree level under the Gene Ontology with the internal @p goId.
     *
     * The returned GeneOntologyTreeLevel contains all the information about the child
     * Gene Ontologies and the Revisions linked to this GeneOntology identified by @p goId.
     *
     * In case @p goId is @c null or @c 0, the root level is retrieved.
     *
     * @param goId The internal id, may be @c null or @c 0 to retrieve the Root level
     * @return Information about the next tree level.
     */
    @Profiled(tag="GeneOntologyTreeService.treeLevel")
    GeneOntologyTreeLevel treeLevel(Long goId) {
        GeneOntology geneOntology = GeneOntology.get(goId)
        List geneOntologies
        if (geneOntology) {
            geneOntologies = nextLevel(geneOntology)
        } else {
            geneOntologies = rootLevel()
        }
        GeneOntologyTreeLevel level = new GeneOntologyTreeLevel()
        if (geneOntology) {
            geneOntologies.each { go, type ->
                level.addOntology(go.id, go.description.identifier, go.description.name, type)
            }
            revisionsForGeneOntology(geneOntology).each {
                level.addRevision(it.toCommandObject())
            }
        } else {
            geneOntologies.each { go ->
                level.addOntology(go.id, go.description.identifier, go.description.name, null)
            }
        }
        return level
    }

    /**
     * Retrieves the root level
     * @return
     */
    private List<GeneOntology> rootLevel() {
        return GeneOntologyRelationship.createCriteria().list {
            to {
                isEmpty('relationships')
            }
            projections {
                distinct("to")
            }
        }
    }

    /**
     * Retrieves all GeneOntologies which have @p go as a parent.
     * @param go The parent GeneOntology
     * @return List of child GeneOntology
     */
    private List nextLevel(GeneOntology go) {
        if (!go) {
            return []
        }
        return GeneOntology.executeQuery("SELECT DISTINCT rel.from, rel.type FROM GeneOntologyRelationship rel WHERE rel.to=:go", [go: go])
    }

    private List<Revision> revisionsForGeneOntology(GeneOntology go) {
        // First retrieve all IDs of the Revision which are not deleted
        List<Long> ids = Revision.executeQuery("SELECT rev.id FROM GeneOntology AS go LEFT JOIN go.revisions AS rev WHERE rev.deleted = false AND go=:go", [go: go]) as List<Long>
        // second: restrict on the revisions the current user can see
        if (!ids.isEmpty() && SpringSecurityUtils.ifNotGranted("ROLE_ADMIN")) {
            Set<String> roles = SpringSecurityUtils.authoritiesToRoles(SpringSecurityUtils.getPrincipalAuthorities())
            if (springSecurityService.isLoggedIn()) {
                // anonymous users do not have a principal
                roles.add((springSecurityService.getPrincipal() as UserDetails).getUsername())
            }
            // checks whether the User has access to the given revisions
            // a user has access to it if a ACE is defined with read or administration right
            // the query assumes that there are no inheriting and no non-granting ACEs
            ids = AclObjectIdentity.executeQuery('''
                    SELECT DISTINCT aoi.objectId FROM AclEntry AS ace
                    INNER JOIN ace.aclObjectIdentity AS aoi
                    INNER JOIN ace.sid AS sid
                    INNER JOIN aoi.aclClass AS c
                    WHERE
                    c.className=:className
                    AND
                    aoi.objectId IN (:objectIds)
                    AND
                    ace.mask IN (:permissions)
                    AND
                    ace.granting=true
                    AND
                    sid.sid IN (:roles)
                    ''', [
                    className: Revision.class.getName(),
                    objectIds: ids,
                    permissions: [BasePermission.READ.getMask(), BasePermission.ADMINISTRATION.getMask()],
                    roles: roles]) as List
        }
        if (ids.isEmpty()) {
            return []
        } else if (ids.size() == 1) {
            return [Revision.get(ids[0])]
        } else {
            // third: only one revision per Model
            // restricts on the maximum revisionNumber per Model
            // and on the Ids present in the list
            return Revision.executeQuery('''
                SELECT rev
                FROM Revision AS rev
                JOIN rev.model.revisions AS revisions
                WHERE
                rev.id IN (:ids)
                GROUP BY rev.model, rev.id
                HAVING rev.revisionNumber = max(revisions.revisionNumber)
                ORDER BY rev.model.name''', [ids: ids])
        }
    }
}
