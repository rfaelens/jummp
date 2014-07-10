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
 */

package net.biomodels.jummp.plugins.security

import grails.plugins.springsecurity.Secured

/**
 * @short Controller class for interacting with user teams.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
@Secured(["isAuthenticated()"])
class TeamController {
    static allowedMethods = [update: "POST"]
    /**
     * Dependency Injection of Spring Security Service
     */
    def springSecurityService
    /**
     * Dependency Injection of Team Service
     */
    def teamService

    /**
     * Renders the form to create new teams.
     */
    def create() {
        render view: "create"
    }

    def save() {
        def team = new Team(params)
        team.owner = springSecurityService.getCurrentUser()
        if (!team.validate()) {
            render view: "create", model: [team: team]
            return
        }
        team.save(flush: true)
        flash.message = "Team ${team.name} created successfully."
        redirect(action: "show", id: team.id)
    }

    /**
     * Lists the teams belonging to the current user.
     */
    def index() {
        def user = springSecurityService.getCurrentUser()
        Set<Team> teams = Team.findAllByOwner(user) as Set
        [teams: teams]
    }

    //TODO
    def update() {
    }

    //TODO
    def edit() {
    }

    // TODO secure this action to ensure that the user has access to the team being accessed
    def show(Long id) {
        Team team = Team.get(id)
        if (!team) {
            flash.message = "Could not find that team. Please select one from the list below."
            redirect(action: 'index')
            return
        }
        [team: team]
    }
}
