package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating change password settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ChangePasswordCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Boolean changePassword
    Boolean resetPassword
    String senderAddress
    String subject
    String body
    String url

    static constraints = {
        changePassword(nullable: true)
        resetPassword(nullable: true)
        senderAddress(nullable: false, email: true, validator: { senderAddress, cmd ->
            if (cmd.resetPassword) {
                return (senderAddress && !senderAddress.isEmpty())
            } else {
                return true
            }
        })
        subject(nullable: false, validator: { subject, cmd ->
            if (cmd.resetPassword) {
                return (subject && !subject.isEmpty())
            } else {
                return true
            }
        })
        body(nullable: false, validator: { body, cmd ->
            if (cmd.resetPassword) {
                return (body && !body.isEmpty())
            } else {
                return true
            }
        })
        url(nullable: false, url: true, validator: { url, cmd ->
            if (cmd.resetPassword) {
                return (url && !url.isEmpty())
            } else {
                return true
            }
        })
    }
}
