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
 **/

/**
 * Script to mark a number of database changesets as ran.
 *
 * It works exactly like the database migration plugin's dbm-mark-next-changeset-ran,
 * with the sole exception that it handles multiple changesets, not just one.
 *
 * Usage: grails mark-next-changesets-ran --count=<a_strictly_positive_integer>
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */

/*
 * In the script binding there is a variable of the form <CamelCasePluginName>PluginDir pointing
 * to each plugin's location. See
 * https://github.com/grails/grails-core/blob/2.3.x/grails-bootstrap/src/main/groovy/org/codehaus/groovy/grails/cli/support/ScriptBindingInitializer.java#L117
 */
File dbmScript = new File("$databaseMigrationPluginDir/scripts/DbmMarkNextChangesetRan.groovy")

includeTargets << grailsScript('_GrailsBootstrap')
includeTargets << dbmScript

target(main: "Marks the next migrations as ran in the database changelog") {
    depends dbmInit
    count = new Integer(argsMap.count) ?: 0
    if (count < 1) {
        errorAndDie """\
Don't know how many changesets to mark as ran. Run the script with --count=<some_integer>."""
    }
    doAndClose {
        count.times {
            printMessage  "Started migration ${it+1}/${count}."
            liquibase.markNextChangeSetRan contexts
            printMessage "... finished migration ${it+1}."
        }
    }
    return 0
}

setDefaultTarget(main)
