package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter
import net.biomodels.jummp.dbus.MiriamDBusAdapter
import org.springframework.beans.factory.InitializingBean

/**
 * Created by IntelliJ IDEA.
 * User: graessli
 * Date: Jun 27, 2011
 * Time: 1:24:00 PM
 * To change this template use File | Settings | File Templates.
 */
@RemoteDBusAdapter(interfaceName="IMiriamService", dbusAdapterName="miriamDBusAdapter")
class RemoteMiriamAdapterDBusImpl extends AbstractRemoteDBusAdapter implements IMiriamService, InitializingBean {
    private MiriamDBusAdapter miriamDBusAdapter

    public void afterPropertiesSet() throws Exception {
        miriamDBusAdapter = getRemoteObject("/Miriam", MiriamDBusAdapter.class)
    }
}
