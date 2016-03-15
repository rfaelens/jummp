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

package net.biomodels.jummp.core.model

import org.grails.databinding.BindUsing

class PublicationLinkProviderTransportCommand implements Serializable {
    String linkType
    /*
     * As of Grails 2.3, empty strings are coerced to null by the default data binder.
     * We define a custom binding policy for this field that does not modify empty strings
     * because we're trying to register a publication link provider (manual entry) which has
     * an empty pattern and null patterns fail validation.
     *
     * See http://grails.github.io/grails-doc/2.3.11/api/org/grails/databinding/BindUsing.html
     */
    @BindUsing({
        target, dataBindingSource -> dataBindingSource['pattern']
    })
    String pattern
    String identifiersPrefix
}
