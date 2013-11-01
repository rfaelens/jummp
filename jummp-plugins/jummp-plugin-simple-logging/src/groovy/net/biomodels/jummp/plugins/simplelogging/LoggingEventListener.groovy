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
* Log4j, Spring Framework (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Log4j, Spring Framework used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.simplelogging

import net.biomodels.jummp.core.events.LoggingEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.apache.log4j.Logger

/**
 * @short Listener for LoggingEvents.
 *
 * This is an example class for showing how a plugin can be notified about
 * LoggingEvents and handle them. This listener just logs all events in a not
 * very useful manner.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class LoggingEventListener implements ApplicationListener {
    /**
     * The logger for this class
     */
    Logger log = Logger.getLogger(getClass())

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof LoggingEvent) {
            log.info("$event.user did $event.source")
        }
    }
}
