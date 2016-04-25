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

package net.biomodels.jummp.search

import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.InitializingBean

class IndexingRoute extends RouteBuilder implements InitializingBean {
    def grailsApplication
    final String DEBUG_CFG =
            "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6005"
    final String JAR_ARGS = '-jar ${body[jarPath]} ${body[jsonPath]}'
    String CLI_ARGS

    @Override
    void configure() {
        //from("seda:exec")
        from("direct:exec")
        .setHeader("CamelExecCommandArgs", simple(CLI_ARGS))
        .to("exec:java")
    }

    void afterPropertiesSet() {
        def config = grailsApplication.config
        def shouldDebugIndexingProcess = config.jummp.search.index.attachDebugger
        CLI_ARGS = shouldDebugIndexingProcess ?
                new StringBuilder(DEBUG_CFG).append(' ').append(JAR_ARGS).toString() :
                JAR_ARGS
    }
}
