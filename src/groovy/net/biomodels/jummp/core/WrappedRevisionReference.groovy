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
**/





package net.biomodels.jummp.core
import java.lang.ref.WeakReference
import java.lang.ref.ReferenceQueue

/**
 * @short Class for tracking no-longer-in-use revision TCs, storing the location of the
 * folder associated with the revision, so it can be deleted by the garbage collection
 * based exchange cleaner quartz job.
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
 
class WrappedRevisionReference extends WeakReference {
	String revisionFolder
	
	WrappedRevisionReference(Object referent, String folder, ReferenceQueue queue) {
		super(referent, queue);
		revisionFolder=folder;
	}
	
	boolean deleteFolder() {
		return (new File(revisionFolder)).deleteDir()
	}
	
	String id() {
		return revisionFolder
	}
	
}
