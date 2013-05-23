package net.biomodels.jummp.dbus.model

import net.biomodels.jummp.core.model.ModelVersionTransportCommand
import org.freedesktop.dbus.DBusSerializable
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand

/**
 * @short DBus Wrapper for a ModelVersionTransportCommand.
 *
 * This class extends the ModelVersionTransportCommand which means that it can be
 * used by the remote side without unwrapping.
 *
 * The ModelTransportCommand wrapped in this object only includes the model id!
 * If further access is required the data has to be fetched separately.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DBusModelVersion extends ModelVersionTransportCommand implements DBusSerializable {

    public DBusModelVersion() {}

    public Object[] serialize() {
        Object[] ret = new Object[8]
        ret[0] = this.id ? this.id : 0
        ret[1] = this.versionNumber ? this.versionNumber : 0
        ret[2] = this.owner ? this.owner : ""
        ret[3] = this.minorVersion ? this.minorVersion : false
        ret[4] = this.comment ? this.comment : ""
        ret[5] = this.uploadDate ? this.uploadDate.getTime() : 0
        ret[6] = this.format ? this.format.name : ""
        ret[7] = this.model ? this.model.id : 0
        return ret
    }

    public void deserialize(long id, int versionNumber, String owner, boolean minorVersion, String comment, long uploadDate, String format, long model) {
        this.id = id
        this.versionNumber = versionNumber
        this.owner = owner
        this.minorVersion = minorVersion
        this.comment = comment
        this.uploadDate = new Date(uploadDate)
        this.format = new ModelFormatTransportCommand(name: format)
        this.model = new ModelTransportCommand(id: model)
    }

    /**
     * Creates a new DBusModelVersion from a ModelVersionTransportCommand.
     * @param rev The ModelVersionTransportCommand
     * @return a DBusSerializable ModelVersionTransportCommand
     */
    public static DBusModelVersion fromModelVersionTransportCommand(ModelVersionTransportCommand ver) {
        return new DBusModelVersion(id: ver.id,
                versionNumber: ver.versionNumber,
                owner: ver.owner,
                minorVersion: ver.minorVersion,
                comment: ver.comment,
                uploadDate: ver.uploadDate,
                format: ver.format,
                model: ver.model
        )
    }
}
