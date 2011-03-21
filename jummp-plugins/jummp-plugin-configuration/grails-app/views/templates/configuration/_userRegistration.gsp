<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="registration">Registration of new Users:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'registration', 'errors')}">
                    <input type="checkbox" name="registration" id="registration" ${!userRegistration || userRegistration.registration ? 'checked="checked"' : ''} title="Registration of new Users"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="sendEmail">Send Email on User Registration:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'sendEmail', 'errors')}">
                    <input type="checkbox" name="sendEmail" id="sendEmail" ${!userRegistration || userRegistration.sendEmail ? 'checked="checked"' : ''} title="Send Email on User Registration"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="sendToAdmin">Send Email to Admin User instead of new user:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'sendToAdmin', 'errors')}">
                    <input type="checkbox" name="sendEmail" id="sendToAdmin" ${userRegistration && userRegistration.sendToAdmin ? 'checked="checked"' : ''} title="Send Email to Admin User instead of new user"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="senderAddress">Sender Address:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'senderAddress', 'errors')}">
                    <input type="text" name="senderAddress" id="senderAddress" value="${userRegistration?.senderAddress}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="adminAddress">Admin Address:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'adminAddress', 'errors')}">
                    <input type="text" name="adminAddress" id="adminAddress" value="${userRegistration?.adminAddress}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="subject">Subject:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'subject', 'errors')}">
                    <input type="text" name="subject" id="subject" value="${userRegistration?.subject}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="body">Body:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'body', 'errors')}">
                    <textarea id="body" rows="20" cols="100" name="body">${userRegistration?.body}</textarea>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="url">URL for Account Verification:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'url', 'errors')}">
                    <input type="text" name="url" id="url" value="${userRegistration?.url}"/><span>register/validate/{{CODE}}</span>
                </td>
            </tr>
            <tr class="prop">
                <td colspan="2">The following Config Options are for account verification if an Administrator registers a new user account.</td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="activationSubject">Subject for Activation:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'activationSubject', 'errors')}">
                    <input type="text" name="activationSubject" id="activationSubject" value="${userRegistration?.activationSubject}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="activationBody">Body for Account Verification:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'activationBody', 'errors')}">
                    <textarea id="activationBody" rows="20" cols="100" name="activationBody">${userRegistration?.activationBody}</textarea>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="activationUrl">URL for Account Activation:</label></td>
                <td class="value ${hasErrors(bean: userRegistration, field: 'activationUrl', 'errors')}">
                    <input type="text" name="activationUrl" id="activationUrl" value="${userRegistration?.activationUrl}"/><span>register/confirmRegistration/{{CODE}}</span>
                </td>
            </tr>
        </tbody>
    </table>
</div>