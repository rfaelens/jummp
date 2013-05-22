package net.biomodels.jummp.core

import net.biomodels.jummp.core.miriam.GeneOntology
import net.biomodels.jummp.core.miriam.GeneOntologyRelationship
import net.biomodels.jummp.core.miriam.GeneOntologyTreeLevel
import org.springframework.security.acls.domain.BasePermission
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import net.biomodels.jummp.model.ModelVersion
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
        def geneOntologies
        if (geneOntology) {
            geneOntologies = nextLevel(geneOntology)
        } else {
            geneOntologies = rootLevel()
        }
        GeneOntologyTreeLevel level = new GeneOntologyTreeLevel()
        if (geneOntology) {
            geneOntologies.each { it ->
                level.addOntology(it[0].id, it[0].description.identifier, it[0].description.name, it[1])
            }
            versionsForGeneOntology(geneOntology).each {
                level.addVersion(it.toCommandObject())
            }
        } else {
            geneOntologies.each { go ->
                level.addOntology(go.id, go.description.identifier, go.description.name, null)
            }
        }
        return level
    }

    /**
     * Searches for all GeneOntologies matching the search term.
     * Search is performed on the GO identifier and name in case insensitive way adding wildcards
     * before and after.
     * @param term The search term
     * @return List of Gene Ontologies matching the search term.
     */
    @Profiled(tag="GeneOntologyTreeService.searchOntologies")
    List<GeneOntology> searchOntologies(String term) {
        return GeneOntology.createCriteria().list {
            description {
                or {
                    ilike('name', "%${term}%")
                    ilike('identifier', "%${term}%")
                }
            }
        }
    }

    /**
     * Constructs a path from root node to the Gene Ontology with the specified id.
     * The path consists of slashes as separators and the id of the gene ontology on the specific
     * node. An example path string looks like "/1/2/4/6" with the last element being the passed
     * in gene ontology id.
     * @param id The id of the GeneOntology for which the path has to be constructed.
     * @return Path to the specified GeneOntology
     */
    @Profiled(tag="GeneOntologyTreeService.findPath")
    String findPath(Long goId) {
        GeneOntology geneOntology = GeneOntology.get(goId)
        String path = ""
        while (geneOntology) {
            path = "/${geneOntology.id}" + path
            geneOntology = getParent(geneOntology)
        }
        return path
    }

    /**
     * @return Parent GeneOntology of given GeneOntology or null if already root element.
     **/
    private GeneOntology getParent(geneOntology) {
        return GeneOntologyRelationship.findByFrom(geneOntology)?.to
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

    private List<ModelVersion> versionsForGeneOntology(GeneOntology go) {
        // First retrieve all IDs of the Revision which are not deleted
        List<Long> ids = ModelVersion.executeQuery("SELECT ver.id FROM GeneOntology AS go LEFT JOIN go.versions AS ver WHERE ver.deleted = false AND go=:go", [go: go]) as List<Long>
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
                    className: ModelVersion.class.getName(),
                    objectIds: ids,
                    permissions: [BasePermission.READ.getMask(), BasePermission.ADMINISTRATION.getMask()],
                    roles: roles]) as List
        }
        if (ids.isEmpty()) {
            return []
        } else if (ids.size() == 1) {
            return [ModelVersion.get(ids[0])]
        } else {
            // third: only one revision per Model
            // restricts on the maximum revisionNumber per Model
            // and on the Ids present in the list
            return ModelVersion.executeQuery('''
                SELECT ver
                FROM ModelVersion AS ver
                JOIN ver.model.versions AS versions
                WHERE
                ver.id IN (:ids)
                GROUP BY ver.model, ver.id
                HAVING ver.versionNumber = max(version.versionNumber)
                ORDER BY ver.model.name''', [ids: ids])
        }
    }
}
