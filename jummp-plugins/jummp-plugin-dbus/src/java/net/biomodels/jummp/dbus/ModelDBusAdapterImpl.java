package net.biomodels.jummp.dbus;

import net.biomodels.jummp.core.IModelService;
import net.biomodels.jummp.core.model.ModelListSorting;
import net.biomodels.jummp.core.model.ModelTransportCommand;
import net.biomodels.jummp.core.model.PublicationTransportCommand;
import net.biomodels.jummp.core.model.RevisionTransportCommand;
import net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException;
import net.biomodels.jummp.dbus.model.DBusModel;
import net.biomodels.jummp.dbus.model.DBusPublication;
import net.biomodels.jummp.dbus.model.DBusRevision;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;

/**
 * @short Concrete Implementation of ModelDBusAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
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
            List<ModelTransportCommand> models = modelService.getAllModels(offset, count, sortOrder, ModelListSorting.valueOf(ModelListSorting.class, sortColumn));
            List<String> returnList = new ArrayList<String>();
            for (ModelTransportCommand model : models) {
                returnList.add(model.getId().toString());
            }
            return returnList;
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getAllModelsByOffsetCountAndSortOrder(String authenticationHash, int offset, int count, boolean sortOrder) {
        try {
            setAuthentication(authenticationHash);
            List<ModelTransportCommand> models = modelService.getAllModels(offset, count, sortOrder);
            List<String> returnList = new ArrayList<String>();
            for (ModelTransportCommand model : models) {
                returnList.add(model.getId().toString());
            }
            return returnList;
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getAllModelsByOffsetCountAndSortColumn(String authenticationHash, int offset, int count, String sortColumn) {
        try {
            setAuthentication(authenticationHash);
            List<ModelTransportCommand> models = modelService.getAllModels(offset, count, ModelListSorting.valueOf(ModelListSorting.class, sortColumn));
            List<String> returnList = new ArrayList<String>();
            for (ModelTransportCommand model : models) {
                returnList.add(model.getId().toString());
            }
            return returnList;
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getAllModelsByOffsetAndCount(String authenticationHash, int offset, int count) {
        try {
            setAuthentication(authenticationHash);
            List<ModelTransportCommand> models = modelService.getAllModels(offset, count);
            List<String> returnList = new ArrayList<String>();
            for (ModelTransportCommand model : models) {
                returnList.add(model.getId().toString());
            }
            return returnList;
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getAllModelsBySortColumn(String authenticationHash, String sortColumn) {
        try {
            setAuthentication(authenticationHash);
            List<ModelTransportCommand> models = modelService.getAllModels(ModelListSorting.valueOf(ModelListSorting.class, sortColumn));
            List<String> returnList = new ArrayList<String>();
            for (ModelTransportCommand model : models) {
                returnList.add(model.getId().toString());
            }
            return returnList;
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getAllModels(String authenticationHash) {
        try {
            setAuthentication(authenticationHash);
            List<ModelTransportCommand> models = modelService.getAllModels();
            List<String> returnList = new ArrayList<String>();
            for (ModelTransportCommand model : models) {
                returnList.add(model.getId().toString());
            }
            return returnList;
        } finally {
            restoreAuthentication();
        }
    }

    public int getModelCount(String authenticationHash) {
        try {
            setAuthentication(authenticationHash);
            return modelService.getModelCount();
        } finally {
            restoreAuthentication();
        }
    }

    public DBusModel getModel(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            return DBusModel.fromModelTransportCommand(modelService.getModel(id));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public DBusRevision getLatestRevision(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            return DBusRevision.fromRevisionTransportCommand(modelService.getLatestRevision(model));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getAllRevisions(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            List<String> revisionNumbers = new ArrayList<String>();
            List<RevisionTransportCommand> revisions = modelService.getAllRevisions(model);
            for (RevisionTransportCommand revision : revisions) {
                revisionNumbers.add(revision.getRevisionNumber().toString());
            }
            return revisionNumbers;
        } finally {
            restoreAuthentication();
        }
    }

    public DBusRevision getRevision(String authenticationHash, long modelId, int revisionNumber) {
        try {
            setAuthentication(authenticationHash);
            return DBusRevision.fromRevisionTransportCommand(modelService.getRevision(modelId, revisionNumber));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public DBusPublication getPublication(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            PublicationTransportCommand publication = modelService.getPublication(model);
            if (publication == null) {
                publication = new PublicationTransportCommand();
            }
            return DBusPublication.fromPublicationTransportCommand(publication);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    /*public DBusModel uploadModel(String authenticationHash, String fileName, DBusModel meta) {
        try {
            setAuthentication(authenticationHash);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DBusRevision addRevision(String authenticationHash, DBusModel model, String fileName, String format, String comment) {
        try {
            setAuthentication(authenticationHash);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }*/

    public boolean canAddRevision(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            return modelService.canAddRevision(model);
        } finally {
            restoreAuthentication();
        }
    }

    /*public String retrieveModelFileByRevision(String authenticationHash, DBusRevision revision) {
        try {
            setAuthentication(authenticationHash);
            // TODO: change ModelService to return File handle instead of byte array
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String retrieveModelFileByModel(String authenticationHash, DBusModel model) {
        try {
            setAuthentication(authenticationHash);
            // TODO: change ModelService to return File handle instead of byte array
            //return modelService.retrieveModelFile()
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }*/

    public void grantReadAccess(String authenticationHash, long id, DBusUser user) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            modelService.grantWriteAccess(model, user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void grantWriteAccess(String authenticationHash, long id, DBusUser user) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            modelService.grantWriteAccess(model, user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void revokeReadAccess(String authenticationHash, long id, DBusUser user) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            modelService.revokeReadAccess(model, user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void revokeWriteAccess(String authenticationHash, long id, DBusUser user) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            modelService.revokeWriteAccess(model, user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void transferOwnerShip(String authenticationHash, long id, DBusUser collaborator) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            modelService.transferOwnerShip(model, collaborator.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public boolean deleteModel(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            return modelService.deleteModel(model);
        } finally {
            restoreAuthentication();
        }
    }

    public boolean restoreModel(String authenticationHash, long id) {
        try {
            setAuthentication(authenticationHash);
            ModelTransportCommand model = new ModelTransportCommand();
            model.setId(id);
            return modelService.restoreModel(model);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public boolean isRemote() {
        return false;
    }

    public void setModelService(IModelService modelService) {
        this.modelService = modelService;
    }
}
