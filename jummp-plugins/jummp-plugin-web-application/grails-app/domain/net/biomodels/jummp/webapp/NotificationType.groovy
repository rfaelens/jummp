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





package net.biomodels.jummp.webapp

/**
 * @short Representation of one NotificationType.
 * This enum represents the types of notications available 
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
enum NotificationType { 
   PUBLISH (1, "PUBLISH"), 
   VERSION_CREATED (2, "VERSION_CREATED"), 
   ACCESS_GRANTED (3, "ACCESS_GRANTED"), 
   DELETED (4, "MODEL_DELETED"), 
   
   final int id 
   final String textRepresentation 

   private NotificationType(int id, String textRepresentation) { 
      this.id = id 
      this.textRepresentation = textRepresentation 
   } 
   
   String toString() { name } 
} 
