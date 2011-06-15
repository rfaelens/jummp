package net.biomodels.jummp.dbus;

import net.biomodels.jummp.core.IModelService;
import net.biomodels.jummp.core.ModelException;
import net.biomodels.jummp.core.model.*;
import net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException;
import net.biomodels.jummp.dbus.model.DBusModel;
import net.biomodels.jummp.dbus.model.DBusPublication;
import net.biomodels.jummp.dbus.model.DBusRevision;
import net.biomodels.jummp.dbus.model.ModelDBusException;
import org.apache.commons.io.FileUtils;
import org.springframework.security.access.AccessDeniedException;

import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod;

/**
 * @short Concrete Implementation of ModelDBusAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@DBusAdapter(interfaceName="ModelDBusAdapter", serviceName="modelService")
public class ModelDBusAdapterImpl extends AbstractDBusAdapter implements ModelDBusAdapter {
    /**
     * Dependency Injection of ModelService
     */
    private IModelService modelService;

    /**
     * Empty default constructor.
     */
    public ModelDBusAdapterImpl() {}
    public List<String> getAllModelsByOffsetCountSortOrderAndSortColumn(String authenticationHash, int offset, int count, boolean sortOrder, String sortColumn) {
        try {
            setAuthentication(authenticationHash);
            return modelService.getAllModels(offset, count, sortOrder, ModelListSorting.valueOf(ModelListSorting.class, sortColumn)).collect { it.id.toString() }
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
            return modelService.getAllModels(offset, count, ModelListSorting.valueOf(ModelListSorting.class, sortColumn)).collect { it.id.toString() }
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
            return modelService.getAllModels(ModelListSorting.valueOf(ModelListSorting.class, sortColumn)).collect { it.id.toString() }
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

    public DBusModel uploadModel(String authenticationHash, String fileName, DBusModel meta) {
        try {
            setAuthentication(authenticationHash);
            File file = new File(fileName);
            DBusModel model = DBusModel.fromModelTransportCommand(modelService.uploadModel(file, meta));
            FileUtils.deleteQuietly(file);
            return model;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (ModelException e) {
            throw new ModelDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public DBusModel uploadModelWithPublication(String authenticationHash, String fileName, DBusModel meta, DBusPublication publication) {
        meta.setPublication(publication);
        return uploadModel(authenticationHash, fileName, meta);
    }

    public DBusRevision addRevision(String authenticationHash, long modelId, String fileName, String format, String comment) {
        try {
            setAuthentication(authenticationHash);
            File file = new File(fileName);
            ModelFormatTransportCommand modelFormat = new ModelFormatTransportCommand();
            modelFormat.setIdentifier(format);
            DBusRevision revision = DBusRevision.fromRevisionTransportCommand(modelService.addRevision(modelId, file, modelFormat, comment));
            FileUtils.deleteQuietly(file);
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

    public String retrieveModelFileByRevision(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            RevisionTransportCommand revision = new RevisionTransportCommand();
            revision.setId(id);
            byte[] bytes = modelService.retrieveModelFile(revision);
            File file = File.createTempFile("jummp", "model");
            FileOutputStream out = new FileOutputStream(file);
            try {
                out.write(bytes);
            } catch (IOException e) {
                throw new ModelDBusException(e.getMessage());
            } finally {
                out.close();
            }
            return file.getAbsolutePath();
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

    public String retrieveModelFileByModel(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            byte[] bytes = modelService.retrieveModelFile(id);
            File file = File.createTempFile("jummp", "model");
            FileOutputStream out = new FileOutputStream(file);
            try {
                out.write(bytes);
            } catch (IOException e) {
                throw new ModelDBusException(e.getMessage());
            } finally {
                out.close();
            }
            return file.getAbsolutePath();
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

    public void setModelService(IModelService modelService) {
        this.modelService = modelService;
    }
}
