/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.core

import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
/**
 * @short Job for checking the status of the modelfileformatconfig which was getting
 * lost (so long as it was in the service). While its running anyway, it updates the 
 * search provider's index, ensuring we have (relatively) recent results.
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class RegistrationCheckerJob {
    
    def grailsApplication
	
    static triggers = {
    	    //run on startup, and then every six hours.
    	    simple name: 'regCheckTrigger', startDelay: 30000, repeatInterval: 5*60000  
    }
  
    def execute() {
    	    grailsApplication.mainContext.getBean("modelFileFormatConfig").status()
    	    //grailsApplication.mainContext.getBean("searchEngine").refreshIndex()
    }

}
