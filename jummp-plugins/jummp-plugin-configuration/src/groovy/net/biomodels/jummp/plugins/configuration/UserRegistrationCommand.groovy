package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating user registration settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserRegistrationCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Boolean registration
    Boolean sendEmail
    Boolean sendToAdmin
    String senderAddress
    String adminAddress
    String subject
    String body
    String url

    static constraints = {
        registration(nullable: true)
        sendEmail(nullable: true)
        sendToAdmin(nullable: true)
        senderAddress(nullable: false, email: true, validator: { senderAddress, cmd ->
            if (cmd.sendEmail) {
                return (senderAddress && !senderAddress.isEmpty())
            } else {
                return true
            }
        })
        adminAddress(nullable: false, email: true, validator: { adminAddress, cmd ->
            if (cmd.sendEmail && cmd.sendToAdmin) {
                return (adminAddress && !adminAddress.isEmpty())
            } else {
                return true
            }
        })
        subject(nullable: false, validator: { subject, cmd ->
            if (cmd.sendEmail) {
                return (subject && !subject.isEmpty())
            } else {
                return true
            }
        })
        body(nullable: false, validator: { body, cmd ->
            if (cmd.sendEmail) {
                return (body && !body.isEmpty())
            } else {
                return true
            }
        })
        url(nullable: false, url: true, validator: { url, cmd ->
            if (cmd.sendEmail) {
                return (url && !url.isEmpty())
            } else {
                return true
            }
        })
    }
}
