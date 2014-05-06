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





package net.biomodels.jummp.core.miriam

/**
 * @short Thread to resolve the name of one Miriam Identifier.
 *
 * This thread is scheduled by the MiriamService when the name of a Miriam
 * Identifier has to be resolved. The thread is provided with the original
 * URN, the datatype the URN belongs to and the cleaned up identifier.
 *
 * The thread uses the available NameResolvers to resolve the name and passes
 * a resolved MiriamIdentifier to the MiriamService to persist.
 *
 * The MiriamService takes care of ensuring that never two threads are scheduled
 * for the same URN. It is important to not create this thread somewhere else as
 * this could result in undefined behavior.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class ResolveMiriamIdentifierThread implements Runnable {
    /**
     * Dependency Injection of Miriam Service
     */
    def miriamService
    /**
     * Dependency Injection of Grails Application
     */
    def grailsApplication

    /**
     * The Miriam Datatype this URN is for
     */
    private MiriamDatatype datatype
    /**
     * The original URN to be processed
     */
    private String urn
    /**
     * The identifier part of the URN (in human readable way)
     */
    private String identifier

    void run() {
        MiriamIdentifier miriamIdentifier = null
        try {
            Map<String, NameResolver> nameResolvers = grailsApplication.mainContext.getBeansOfType(NameResolver)
            for (NameResolver nameResolver in nameResolvers.values()) {
                if (nameResolver.supports(datatype)) {
                    String resolvedName = nameResolver.resolve(datatype, identifier)
                    if (resolvedName) {
                        miriamIdentifier = new MiriamIdentifier(identifier: identifier, datatype: datatype, name: resolvedName)
                        break
                    }
                }
            }
        } finally {
            miriamService.dequeueUrnForIdentifierResolving(urn, miriamIdentifier)
        }
    }

    static public ResolveMiriamIdentifierThread getInstance(String urn, String identifier, MiriamDatatype datatype) {
        ResolveMiriamIdentifierThread thread = new ResolveMiriamIdentifierThread()
        thread.urn = urn
        thread.identifier = identifier
        thread.datatype = datatype
        return thread
    }
}
