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





package net.biomodels.jummp.webapp.ast

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @short Marker Annotation to generate a verifyMessage block in a JMS Queue method.
 *
 * The annotation may only be used for methods in classes annotated with @link JmsAdapter.
 *
 * If present the AST Transformation generates an if block in the method to verify if the message
 * matches the types specified by this annotation.
 *
 * The annotation takes two parameters:
 * @li isAuthenticate to indicate whether the generated code has to check for the AuthenticationHash, if @c true
 * the code will also contain a method call to set the Authentication from the message
 * @li arguments Array of Classes of parameters <b>without</b> the AuthenticationHash 
 *
 * @see JmsAdapter
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
public @interface JmsQueueMethod {
    boolean isAuthenticate()
    Class[] arguments()
}
