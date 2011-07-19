package net.biomodels.jummp.plugins.bives

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import net.biomodels.jummp.plugins.bives.DiffDataProvider

import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Provides the data from the {@link DiffDataProvider} for the view and creates a new thread
 * for the generation of a diff, in case it's not been created yet.
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 05.07.2011
 * @year 2011
 */
public class DiffDataService {

	/**
	 * Dependency Injection of ModelDelegateService
	 */
	def modelDelegateService
	/**
	 * DependencyInjection of ExecutorService
	 */
	def executorService

	static transactional = true

	private final List<String> lockedDiffs = []

	/**
	 * Lock to protect the access to the identifiersToBeResolved
	 */
	private final Lock lock = new ReentrantLock()

	/**
	 * Provides the data from a generated diff for the view if present or starts a thread
	 * for the creation of a non-existing diff
	 * @param modelId the id of the model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return a Map containing the different types of changes extracted from the diff
	 */
	Map generateDiffData(long modelId, int previousRevision, int recentRevision) {
		try {
			DiffDataProvider diffData = ApplicationHolder.application.mainContext.getBean("diffDataProvider") as DiffDataProvider
			if(!diffData.getDiffInformation(modelId, previousRevision, recentRevision)) {
				Runnable runnable = null
				String diff = modelId + ";" + previousRevision + ";" + recentRevision
				// prevents the multiple generation of the same diff by locking this process and
				// storing the information about the currently generated diff
				lock.lock();
				if(lockedDiffs.contains(diff)) {
					return [moves:[:], updates:[:], inserts:[:], deletes:[:]]
				}
				runnable = ApplicationHolder.application.mainContext.getBean("createDiff", modelId, previousRevision, recentRevision) as Runnable
				if(runnable) {
					println("setting lock: " + diff)
					lockedDiffs << diff
				}
				executorService.submit(runnable)
				return [moves:[:], updates:[:], inserts:[:], deletes:[:]]
			}
			return [moves: diffData.moves, updates: diffData.updates, inserts: diffData.inserts, deletes: diffData.deletes]
		} catch (Exception e) {

		} finally {
			lock.unlock();
		}
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
				println("unlocking: " + diff)
				lockedDiffs.remove(diff)
			}
		} catch (Exception e) {
			e.printStackTrace()
		} finally {
			lock.unlock()
		}
	}
}
