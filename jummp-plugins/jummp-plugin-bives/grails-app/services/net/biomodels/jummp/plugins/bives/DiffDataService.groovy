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
* Spring Framework (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Spring Framework used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.bives

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import net.biomodels.jummp.core.bives.DiffNotExistingException;

import org.springframework.beans.factory.InitializingBean

/**
 * Provides the data from the DiffDataProvider for the view and creates a new thread
 * for the generation of a diff, in case it's not been created yet.
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
public class DiffDataService implements InitializingBean {

	/**
	 * Dependency Injection of ModelDelegateService
	 */
	def modelDelegateService
	/**
	 * DependencyInjection of ExecutorService
	 */
	def executorService
    /**
     * Dependency Injection of Grails Application
     */
    @SuppressWarnings("GrailsStatelessService")
    def grailsApplication
    /**
     * Dependency Injection of Servlet Context
     */
    @SuppressWarnings("GrailsStatelessService")
    def servletContext

	static transactional = true

	private final List<String> lockedDiffs = []

	/**
	 * Lock to protect the access to the identifiersToBeResolved
	 */
	private final Lock lock = new ReentrantLock()

    void afterPropertiesSet() throws Exception {
        if (!(grailsApplication.config.jummp.plugins.bives.diffdir instanceof String) || grailsApplication.config.jummp.plugins.bives.diffdir.isEmpty()) {
            grailsApplication.config.jummp.plugins.bives.diffdir = servletContext.getRealPath("/resource/diffDir")
        }
    }

    /**
     * @returns Path to the diff directory
     */
    public String diffDirectory() {
        return grailsApplication.config.jummp.plugins.bives.diffdir
    }

	/**
	 * Provides the data from a generated diff for the view if present or starts a thread
	 * for the creation of a non-existing diff
	 * @param modelId the id of the model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return a Map containing the different types of changes extracted from the diff
	 */
	Map generateDiffData(long modelId, int previousRevision, int recentRevision) throws DiffNotExistingException {
		DiffDataProvider diffData = grailsApplication.mainContext.getBean("diffDataProvider") as DiffDataProvider
		if(!diffData.getDiffInformation(modelId, previousRevision, recentRevision)) {
			Runnable runnable = null
			String diff = modelId + ";" + previousRevision + ";" + recentRevision
			// prevents the multiple generation of the same diff by locking this process and
			// storing the information about the currently generated diff
			lock.lock();
			try {
				if(lockedDiffs.contains(diff)) {
					throw new DiffNotExistingException()
				}
				runnable = grailsApplication.mainContext.getBean("createDiff", modelId, previousRevision, recentRevision) as Runnable
				if(runnable) {
					lockedDiffs << diff
				}
				executorService.submit(runnable)
				throw new DiffNotExistingException()
//			} catch (Exception e) {
			} 
				finally {
				lock.unlock();
			}
		}
		return [moves: diffData.moves, updates: diffData.updates, inserts: diffData.inserts, deletes: diffData.deletes]
	}

	/**
	 * Removes a diff from the queue
	 * @param modelId the id of the model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 */
	void unqueueDiff(long modelId, int previousRevision, int recentRevision) {
		try {
			String diff = modelId + ";" + previousRevision + ";" + recentRevision
			lock.lock();
			if(lockedDiffs.contains(diff)) {
				lockedDiffs.remove(diff)
			}
		} catch (Exception e) {
			e.printStackTrace()
		} finally {
			lock.unlock()
		}
	}
}
