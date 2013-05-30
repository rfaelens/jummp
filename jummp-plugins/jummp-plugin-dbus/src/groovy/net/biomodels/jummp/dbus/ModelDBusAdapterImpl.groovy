package net.biomodels.jummp.dbus;

import net.biomodels.jummp.core.IModelService;
import net.biomodels.jummp.core.ModelException;
import net.biomodels.jummp.core.model.*;
import net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException;
import net.biomodels.jummp.dbus.model.DBusModel;
import net.biomodels.jummp.dbus.model.DBusPublication;
import net.biomodels.jummp.dbus.model.DBusRevision;
import net.biomodels.jummp.dbus.model.ModelDBusException;
import org.springframework.security.access.AccessDeniedException;

import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod;
import java.util.List
import java.util.LinkedList
/**
 * @short Concrete Implementation of ModelDBusAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
@DBusAdapter(interfaceName="ModelDBusAdapter", serviceName="modelDelegateService")
public class ModelDBusAdapterImpl extends AbstractDBusAdapter implements ModelDBusAdapter {
    /**
     * Dependency Injection of ModelService
     */
    private IModelService modelDelegateService;

    /**
     * Empty default constructor.
     */
    public ModelDBusAdapterImpl() {}
    public List<String> getAllModelsByOffsetCountSortOrderAndSortColumn(String authenticationHash, int offset, int count, boolean sortOrder, String sortColumn) {
        try {
            setAuthentication(authenticationHash);
            return modelDelegateService.getAllModels(offset, count, sortOrder, ModelListSorting.valueOf(ModelListSorting.class, sortColumn)).collect { it.id.toString() }
        } finally {
            restoreAuthentication();
        }
    }

    @DBusMethod(isAuthenticate = true, collect = "id", delegate = "getAllModels")
    public List<String> getAllModelsByOffsetCountAndSortOrder(String authenticationHash, int offset, int count, boolean sortOrder) {
    }

    public List<String> getAllModelsByOffsetCountAndSortColumn(String authenticationHash, int offset, int count, String sortColumn) {
        try {
            setAuthentication(authenticationHash);
            return modelDelegateService.getAllModels(offset, count, ModelListSorting.valueOf(ModelListSorting.class, sortColumn)).collect { it.id.toString() }
        } finally {
            restoreAuthentication();
        }
    }

    @DBusMethod(isAuthenticate = true, collect = "id", delegate = "getAllModels")
    public List<String> getAllModelsByOffsetAndCount(String authenticationHash, int offset, int count) {
    }

    public List<String> getAllModelsBySortColumn(String authenticationHash, String sortColumn) {
        try {
            setAuthentication(authenticationHash);
            return modelDelegateService.getAllModels(ModelListSorting.valueOf(ModelListSorting.class, sortColumn)).collect { it.id.toString() }
        } finally {
            restoreAuthentication();
        }
    }

    @DBusMethod(isAuthenticate = true, collect = "id")
    public List<String> getAllModels(String authenticationHash) {
    }

    @DBusMethod(isAuthenticate = true)
    public int getModelCount(String authenticationHash) {

    }

    @DBusMethod(isAuthenticate = true)
    public DBusModel getModel(String authenticationHash, long id) {
    }

    @DBusMethod(isAuthenticate = true)
    public DBusRevision getLatestRevision(String authenticationHash, long id) {
    }

    @DBusMethod(isAuthenticate = true, collect = "revisionNumber")
    public List<String> getAllRevisions(String authenticationHash, long id) {
    }

    @DBusMethod(isAuthenticate = true)
    public DBusRevision getRevision(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true)
    public DBusPublication getPublication(String authenticationHash, long id) {
    }

    private List<File> getAsFileList(List<String> fileNames)
    {
        List<File> files=new LinkedList<File>();
        fileNames.each
        {
            file.add(new File(it))
        }
        return files;
    }
    
    public DBusModel uploadModel(String authenticationHash, List<String> fileNames, DBusModel meta) {
        try {
            setAuthentication(authenticationHash);
            DBusModel model = DBusModel.fromModelTransportCommand(modelDelegateService.uploadModel(getAsFileList(files), meta));
            return model;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (ModelException e) {
            throw new ModelDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public DBusModel uploadModelWithPublication(String authenticationHash, List<String> fileNames, DBusModel meta, DBusPublication publication) {
        meta.setPublication(publication);
        return uploadModel(authenticationHash, getAsFileList(fileName), meta);
    }

    public DBusRevision addRevision(String authenticationHash, long modelId, String fileName, String format, String comment) {
        try {
            setAuthentication(authenticationHash);
            File file = new File(fileName);
            ModelFormatTransportCommand modelFormat = new ModelFormatTransportCommand();
            modelFormat.setIdentifier(format);
            DBusRevision revision = DBusRevision.fromRevisionTransportCommand(modelDelegateService.addRevision(modelId, file, modelFormat, comment));
            return revision;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (ModelException e) {
            throw new ModelDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    @DBusMethod(isAuthenticate = true)
    public boolean canAddRevision(String authenticationHash, long id) {
    }

    private String writeFilesToTempDirectory(Map<String,byte[]> bytes)
    {
        File directory = File.createTempDir();
        for (Map.Entry entry : bytes.entrySet())
        {
            File newFile=new File(directory.absolutePath + System.getProperty("file.separator") + entry.getKey());
            newFile.setBytes(entry.getValue())
        }
    }
    
    public String retrieveModelFilesByRevision(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            RevisionTransportCommand revision = new RevisionTransportCommand();
            revision.setId(id);
            Map<String,byte[]> bytes = modelDelegateService.retrieveModelFiles(revision);
            return writeFilesToTempDirectory(bytes);
            // TODO: change ModelService to return File handle instead of byte array
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (ModelException e) {
            throw new ModelDBusException(e.getMessage());
        }catch (IOException e) {
            throw new ModelDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public String retrieveModelFilesByModel(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            Map<String,byte[]> bytes = modelDelegateService.retrieveModelFiles(id);
            return writeFilesToTempDirectory(bytes);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (ModelException e) {
            throw new ModelDBusException(e.getMessage());
        } catch (IOException e) {
            throw new ModelDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    @DBusMethod(isAuthenticate = true)
    public void grantReadAccess(String authenticationHash, long id, DBusUser user) {
    }

    @DBusMethod(isAuthenticate = true)
    public void grantWriteAccess(String authenticationHash, long id, DBusUser user) {
    }

    @DBusMethod(isAuthenticate = true)
    public void revokeReadAccess(String authenticationHash, long id, DBusUser user) {
    }

    @DBusMethod(isAuthenticate = true)
    public void revokeWriteAccess(String authenticationHash, long id, DBusUser user) {
    }

    @DBusMethod(isAuthenticate = true)
    public void transferOwnerShip(String authenticationHash, long id, DBusUser collaborator) {
    }

    @DBusMethod(isAuthenticate = true)
    public boolean deleteModel(String authenticationHash, long id) {
    }

    @DBusMethod(isAuthenticate = true)
    public boolean restoreModel(String authenticationHash, long id) {
    }

    @DBusMethod(isAuthenticate = true, getRevision = [1, 2])
    public boolean deleteRevision(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getRevision = [1, 2])
    public void publishModelRevision(String authenticationHash, long modelId, int revisionNumber) {
    }

    public void setModelDelegateService(IModelService modelDelegateService) {
        this.modelDelegateService = modelDelegateService;
    }
}
