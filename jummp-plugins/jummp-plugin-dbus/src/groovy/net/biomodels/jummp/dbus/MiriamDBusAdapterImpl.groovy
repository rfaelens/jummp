package net.biomodels.jummp.dbus

import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod

/**
 * Created by IntelliJ IDEA.
 * User: graessli
 * Date: Jun 27, 2011
 * Time: 1:18:35 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter']
@DBusAdapter(interfaceName="MiriamDBusAdapter", serviceName="miriamService")
public class MiriamDBusAdapterImpl extends AbstractDBusAdapter implements MiriamDBusAdapter {

    private IMiriamService miriamService

    /**
     * Empty Default ctor
     */
    public MiriamDBusAdapterImpl() {}

    @DBusMethod(isAuthenticate = true)
    public void updateMiriamResources(String authenticationHash, String url, boolean force) {
    }
    @DBusMethod(isAuthenticate = true, json=true)
    public String miriamData(String authenticationHash, String urn) {
    }
    @DBusMethod(isAuthenticate = true)
    public void updateAllMiriamIdentifiers(String authenticationHash) {
    }
    @DBusMethod(isAuthenticate = true)
    public void updateModels(String authenticationHash) {
    }

    public void setMiriamService(IMiriamService miriamService) {
        this.miriamService = miriamService
    }
}
