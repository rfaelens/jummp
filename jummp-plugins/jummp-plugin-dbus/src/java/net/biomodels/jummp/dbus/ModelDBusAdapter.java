package net.biomodels.jummp.dbus;

import net.biomodels.jummp.dbus.model.DBusModel;
import net.biomodels.jummp.dbus.model.DBusPublication;
import net.biomodels.jummp.dbus.model.DBusModelVersion;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

import java.util.List;

/**
 * @short DBus Interface for ModelService.
 *
 * This interface describes the DBus Interface for the service described by IModelService.
 * In opposite to IModelService all methods take an additional first parameter for the authentication
 * identifier. All methods can throw specific DBusExceptions for cases like AccessDeniedException,
 * AuthenticationHashNotFoundException plus wrappers for all exceptions derived from JummpException.
 * The return values are wrappers implementing the DBusSerializable interface but extended from the
 * respective TransportCommand, so they do not need to be unwrapped again.
 *
 * Methods returning lists do not return lists of objects, but only list of ids. It is important to
 * fetch the actual data using the specific get* method.
 *
 * Please note that the methods are mostly not documented. Only methods having a clear difference to
 * the description above may be documented. For documentation please refer to IModelService.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @see net.biomodels.jummp.core.IModelService
 */
@DBusInterfaceName("net.biomodels.jummp.model")
public interface ModelDBusAdapter extends DBusInterface {
    public List<String> getAllModelsByOffsetCountSortOrderAndSortColumn(String authenticationHash, int offset, int count, boolean sortOrder, String sortColumn);
    public List<String> getAllModelsByOffsetCountAndSortOrder(String authenticationHash, int offset, int count, boolean sortOrder);
    public List<String> getAllModelsByOffsetCountAndSortColumn(String authenticationHash, int offset, int count, String sortColumn);
    public List<String> getAllModelsByOffsetAndCount(String authenticationHash, int offset, int count);
    public List<String> getAllModelsBySortColumn(String authenticationHash, String sortColumn);
    public List<String> getAllModels(String authenticationHash);
    public int getModelCount(String authenticationHash);
    public DBusModel getModel(String authenticationHash, long id);
    public DBusModelVersion getLatestVersion(String authenticationHash, long id);
    public List<String> getAllVersions(String authenticationHash, long id);
    public DBusModelVersion getVersion(String authenticationHash, long modelId, int versionNumber);
    public DBusPublication getPublication(String authenticationHash, long id);
    public DBusModel uploadModel(String authenticationHash, String fileName, DBusModel meta);
    public DBusModel uploadModelWithPublication(String authenticationHash, String fileName, DBusModel meta, DBusPublication publication);
    public DBusModelVersion addVersion(String authenticationHash, long modelId, String fileName, String format, String comment);
    public boolean canAddVersion(String authenticationHash, long id);
    public String retrieveModelFileByVersion(String authenticationHash, long id);
    public String retrieveModelFileByModel(String authenticationHash, long id);
    public void grantReadAccess(String authenticationHash, long id, DBusUser user);
    public void grantWriteAccess(String authenticationHash, long id, DBusUser user);
    public void revokeReadAccess(String authenticationHash, long id, DBusUser user);
    public void revokeWriteAccess(String authenticationHash, long id, DBusUser user);
    public void transferOwnerShip(String authenticationHash, long id, DBusUser collaborator);
    public boolean deleteModel(String authenticationHash, long id);
    public boolean restoreModel(String authenticationHash, long id);
    public boolean deleteVersion(String authenticationHash, long modelId, int versionNumber);
    public void publishModelVersion(String authenticationHash, long modelId, int versionNumber);
}
