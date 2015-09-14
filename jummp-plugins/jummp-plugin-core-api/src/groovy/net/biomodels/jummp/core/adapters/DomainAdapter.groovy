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

package net.biomodels.jummp.core.adapters
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Publication
import net.biomodels.jummp.model.PublicationLinkProvider
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.security.Person

/**
 * @short Adapter class for use with externally defined domain objects. Use
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
public abstract class DomainAdapter {
    public static DomainObjectComposite getAdapter(def domainObject) {
        return new DomainObjectComposite(domainObject, selectAppropriateAdapter(domainObject))
    }
    
    private static DomainAdapter selectAppropriateAdapter(def domainObject) {
        switch(domainObject) {
            case Model: return new ModelAdapter(model: domainObject)
            case ModelFormat: return new ModelFormatAdapter(format: domainObject)
            case Publication: return new PublicationAdapter(publication: domainObject)
            case PublicationLinkProvider: return new PublicationLinkProviderAdapter(linkProvider: domainObject)
            case Revision: return new RevisionAdapter(revision: domainObject)
            case Person: return new PersonAdapter(person: domainObject)
        }
        throw new IllegalArgumentException("Domain object ${domainObject} does not have a known adapter")
    }
}
