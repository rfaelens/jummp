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
* Spring Framework, Perf4j, Spring Security (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0 the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Spring Framework, Perf4j, Spring Security used as well as
* that of the covered work.}
**/

package net.biomodels.jummp.plugins.security

import grails.transaction.Transactional
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.perf4j.aop.Profiled
import org.springframework.security.access.prepost.PreAuthorize
import net.biomodels.jummp.core.events.LoggingEventType
import net.biomodels.jummp.core.events.PostLogging

@Transactional
class TeamService {

    def create() {

    }
    
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="teamService.getUsersFromTeam")
    @PreAuthorize("isAuthenticated()") //used to be: authentication.name==#username
    List<String> getUsersFromTeam(Long teamID) {
    	Team team = Team.get(teamID)
    	def usersInTeam = UserTeam.findAllByTeam(team)
    	return usersInTeam.collect {[
    			"email": it.user.email,
    			"username": it.user.username,
    			"userRealName": it.user.person.userRealName
    	]};
    }
    
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="teamService.getTeamsForUser")
    @PreAuthorize("isAuthenticated()") //used to be: authentication.name==#username
    List<Team> getTeamsForUser(User user) {
    	def teamsIveCreated = Team.findAllByOwner(user)
		def teamsImAMemberOf = UserTeam.findAllByUser(user).collect { it.team }
		return teamsIveCreated.plus(teamsImAMemberOf)
    }
  
}
