package net.biomodels.jummp.dbus.model

import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.freedesktop.dbus.DBusSerializable
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand

/**
 * @short DBus Wrapper for a RevisionTransportCommand.
 *
 * This class extends the RevisionTransportCommand which means that it can be
 * used by the remote side without unwrapping.
 *
 * The ModelTransportCommand wrapped in this object only includes the model id!
 * If further access is required the data has to be fetched separately.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DBusRevision extends RevisionTransportCommand implements DBusSerializable {

    public DBusRevision() {}

    public Object[] serialize() {
        Object[] ret = new Object[8]
        ret[0] = this.id ? this.id : 0
        ret[1] = this.revisionNumber ? this.revisionNumber : 0
        ret[2] = this.owner ? this.owner : ""
        ret[3] = this.minorRevision ? this.minorRevision : false
        ret[4] = this.comment ? this.comment : ""
        ret[5] = this.uploadDate ? this.uploadDate.getTime() : 0
        ret[6] = this.format ? this.format.name : ""
        ret[7] = this.model ? this.model.id : 0
        return ret
    }

    public void deserialize(long id, int revisionNumber, String owner, boolean minorRevision, String comment, long uploadDate, String format, long model) {
        this.id = id
        this.revisionNumber = revisionNumber
        this.owner = owner
        this.minorRevision = minorRevision
        this.comment = comment
        this.uploadDate = new Date(uploadDate)
        this.format = new ModelFormatTransportCommand(name: format)
        this.model = new ModelTransportCommand(id: model)
    }

    /**
     * Creates a new DBusRevision from a RevisionTransportCommand.
     * @param rev The RevisionTransportCommand
     * @return a DBusSerializable RevisionTransportCommand
     */
    public static DBusRevision fromRevisionTransportCommand(RevisionTransportCommand rev) {
        return new DBusRevision(id: rev.id,
                revisionNumber: rev.revisionNumber,
                owner: rev.owner,
                minorRevision: rev.minorRevision,
                comment: rev.comment,
                uploadDate: rev.uploadDate,
                format: rev.format,
                model: rev.model
        )
    }
}
