/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.core

import net.biomodels.jummp.core.miriam.GeneOntology
import net.biomodels.jummp.core.miriam.GeneOntologyRelationship
import net.biomodels.jummp.core.miriam.GeneOntologyTreeLevel
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
 * @author Raza Ali <raza.ali@ebi.ac.uk>
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
