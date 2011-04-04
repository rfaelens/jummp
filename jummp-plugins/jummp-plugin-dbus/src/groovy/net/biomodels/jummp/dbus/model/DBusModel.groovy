package net.biomodels.jummp.dbus.model

import net.biomodels.jummp.core.model.ModelTransportCommand
import org.freedesktop.dbus.DBusSerializable
import net.biomodels.jummp.core.model.ModelFormatTransportCommand


/**
 * @short DBusWrapper for a ModelTransportCommand.
 *
 * This class actually extends the ModelTransportCommand, so it can be used to be
 * passed to the core.
 *
 * The serialization does not includes the Publication.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DBusModel extends ModelTransportCommand implements DBusSerializable {

    public DBusModel() {}

    Object[] serialize() {
        Object[] retVals = new Object[10]
        retVals[0] = this.id ? this.id : 0
        retVals[1] = this.name ? this.name : ""
        retVals[2] = this.state ? this.state.toString() : ""
        retVals[3] = this.format ? this.format.identifier : ""
        retVals[4] = this.format ? this.format.name : ""
        retVals[5] = this.comment ? this.comment : ""
        retVals[6] = this.lastModifiedDate ? this.lastModifiedDate.getTime() : 0
        retVals[7] = this.submitter ? this.submitter : ""
        retVals[8] = this.submissionDate ? this.submissionDate.getTime() : 0
        retVals[9] = this.creators ? this.creators.toArray() : [''].toArray()
        return retVals
    }

    public void deserialize(long id, String name, String state, String format, String formatName, String comment, long lastModifiedDate, String submitter, long submissionDate, String[] creators) {
        this.id = id
        this.name = name
        //this.state =
        this.comment = comment
        if (format != "") {
            this.format = new ModelFormatTransportCommand(identifier: format)
            if (formatName != "") {
                this.format.name = formatName
            }
        }
        if (comment != "") {
            this.comment = comment
        }
        if (lastModifiedDate != 0) {
            this.lastModifiedDate = new Date(lastModifiedDate)
        }
        if (submitter != "") {
            this.submitter = submitter
        }
        if (submissionDate != 0) {
            this.submissionDate = new Date(submissionDate)
        }
        this.creators = new HashSet(creators.toList())
    }

    static DBusModel fromModelTransportCommand(ModelTransportCommand model) {
        return new DBusModel(id: model.id,
                name: model.name,
                state: model.state,
                format: model.format,
                comment: model.comment,
                lastModifiedDate: model.lastModifiedDate,
                publication: model.publication,
                submitter: model.submitter,
                submissionDate: model.submissionDate,
                creators: model.creators
        )
    }
}
