package net.biomodels.jummp.core

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext

/**
 * @short Job initializing and executing a trigger removing authentication hashes.
 *
 * The authentication hashes are getting removed when a configurable period of inactivity is expired.
 * The job uses the Grails Quartz Plugin.
 *
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class AuthenticationOutdaterJob {
    def authenticationHashService

    static triggers = {
        // TODO: find a solution for ConfigurationHolder as it is deprecated
        simple name: 'authenticationRemoveTrigger', startDelay: Long.valueOf(ConfigurationHolder.config.jummp.authenticationHash.startRemoveOffset), repeatInterval: Long.valueOf(ConfigurationHolder.config.jummp.authenticationHash.removeInterval)
    }

    /**
     * Triggers removal of inactive authentications.
     * @param context the JobExecutionContext
     * @throws JobExecutionException
     */

    def execute(JobExecutionContext context) throws JobExecutionException {
        authenticationHashService.checkAuthenticationExpired()
    }

}
