package net.biomodels.jummp.plugins.bives;

import java.io.File;

import de.unirostock.bives.diff.model.Diff;
import de.unirostock.bives.diff.util.FileFunctions;
import de.unirostock.bives.fwk.messages.FileExtensions;
import de.unirostock.bives.fwk.repository.AbstractRepositoryManager;

/**
 * @short Manager class for diff files
 * 
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 27.06.2011
 * @year 2011
 */
class JummpRepositoryManager extends AbstractRepositoryManager {
	
	public static final String PREFIX = "model";
	public static final String XML = FileExtensions.XML;
	
	/**
	 * Default constructor
	 */
	public JummpRepositoryManager() {
		super();
	}
	
	/**
	 * Constructor
	 * @param repository the path of the diff repository
	 */
	public JummpRepositoryManager(String repository) {
		super(repository);
	}
	
	@Override
	public boolean createNewRepository(String folder) {
		return createNewRepository(folder, "");
	}

	@Override
	public boolean createNewRepository(String parentFolder, String name) {
		setRepositoryPath(parentFolder + SLASH + name);
		File repository = new File(getRepositoryPath());
		boolean success = true;
		if(!repository.exists()) {
			success = success && repository.mkdirs();
		}
		return success;
	}

	@Override
	public File getDiffFile(String name) {
		if(new File(name).exists()) {
			return new File(name);
		} else {
			return null;
		}
	}

	/**
	 * Retrieves a diff file from the configured directory
	 * @param modelId the id of the corresponding model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return
	 */
	public File getDiffFile(long modelId, int predecessorRevision, int successorRevision) {
		return getDiffFile(getRepositoryPath() + SLASH + getDiffName(modelId, predecessorRevision, successorRevision));
	}
	
	@Override
	public File getModelFile(String arg0) {
		// do nothing
		return null;
	}

	@Override
	public File uploadDiff(Diff arg0) {
		// do nothing
		return null;
	}

	/**
	 * Stores a {@link Diff} in the configured directory
	 * @param diff the {@link Diff} between both revisions
	 * @param modelId the id of the corresponding model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return the {@link Diff} {@link File} which has been stored
	 */
	public File uploadDiff(Diff diff, long modelId, int predecessorRevision, int successorRevision) {
		File file = new File(getRepositoryPath() + SLASH + getDiffName(modelId, predecessorRevision, successorRevision));
		return uploadDiff(diff, file);
	}
	
	@Override
	public File uploadDiff(Diff diff, File file) {
		FileFunctions.diffToFile(diff, file);
		return null;
	}

	@Override
	public File uploadModel(File arg0) {
		// do nothing
		return null;
	}
	
	/**
	 * Generates the name for a diff file to be stored or retrieved.
	 * @param modelId the id of the corresponding model
	 * @param previousRevision the number of a previous model revision
	 * @param recentRevision a successor revision (in relation to the previous revision)
	 * @return a {@link String}
	 */
	protected String getDiffName(long modelId, int predecessorRevision, int successorRevision) {
		return new String(PREFIX + modelId + "_diff_" + predecessorRevision + "_" + successorRevision + XML);
	}

	@Override
	public Diff getDiff(String name) {
		return FileFunctions.fileToDiff(getDiffFile(name));
	}

	@Override
	public Diff getDiff(File file) {
		return FileFunctions.fileToDiff(file);
	}
}
