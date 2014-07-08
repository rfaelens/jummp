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

package net.biomodels.jummp.webapp.rest.marshaller

import grails.converters.XML
import grails.util.Holders as H
import net.biomodels.jummp.core.IModelService
import net.biomodels.jummp.webapp.rest.model.show.Model
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller

/**
 * @short Custom XML marshaller that takes into account perennial model identifiers.
 *
 * This class overrides the default XML renderer for a Model to ensure that its perennial
 * identifier or identifiers.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class ModelXmlMarshaller implements ObjectMarshaller<XML> {
    /* use a formatted output. */
    protected boolean prettyPrint = true

    @Override
    boolean supports(Object o) {
        o instanceof Model
    }

    @Override
    void marshalObject(Object o, XML converter) {
        final Model M = (Model) o

        def ctx = H.grailsApplication.mainContext
        IModelService modelDelegateService = ctx.getBean("modelDelegateService")
        final boolean MANY_IDENTIFIERS = modelDelegateService.haveMultiplePerennialIdentifierTypes()
        if (MANY_IDENTIFIERS) {
            converter.startNode 'identifiers'
            ID_TYPES.each { String t ->
                final String VALUE = M."$t"
                if (VALUE) {
                    converter.startNode(t.endsWith('Id') ? t.append('entifier') : t)
                    converter.chars VALUE
                    converter.end()
                }
            }
            converter.end()
        } else {
            converter.startNode 'identifier'
            converter.chars M.submissionId
            converter.end()
        }

        ['name', 'description', 'publication', "format", "files", "history" ].each { String f ->
            final String VALUE = M."$f"
            if (VALUE) {
                converter.startNode f
                converter.chars VALUE
                converter.end()
            }
        }
    }
}
