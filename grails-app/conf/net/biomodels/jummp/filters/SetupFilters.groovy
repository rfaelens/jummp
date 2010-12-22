package net.biomodels.jummp.filters

/**
 * @short Filter to redirect to SetupController
 *
 * The filter checks whether the application instance has been configured yet.
 * That is it tests for the availability of the file @c ~/.jummp.properties.
 * When the file does not exists all requests (except Ajax requests) are redirected
 * to the setup webflow of SetupController.
 *
 * When the file exists but the property @c jummp.firstRun is set to true all requests
 * are redirected to the firstRun webflow of SetupController.
 *
 * When the file exists and the firstRun property is false all access to SetupController
 * are blocked as the application is already configured.
 *
 * @see net.biomodels.jummp.controllers.SetupController
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SetupFilters {
    private boolean configFileExists
    private boolean firstRun

    public SetupFilters() {
        File configurationProperties = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties")
        configFileExists = configurationProperties.exists()
        if (configFileExists) {
            Properties props = new Properties()
            props.load(new FileInputStream(configurationProperties))
            firstRun = Boolean.parseBoolean(props.getProperty("jummp.firstRun"))
        } else {
            firstRun = false
        }
    }

    def filters = {
        if (configFileExists) {
            if (firstRun) {
                setupFilter(controller: '*', action: '*') {
                    before = {
                        if ((controllerName != 'setup' || (controllerName == "setup" && actionName != "firstRun"))
                                && !request.getHeader("X-Requested-With") && request.getParameter('ajax') != "true") {
                            redirect(controller: 'setup', action: "firstRun")
                            return false
                        } else {
                            return true
                        }
                    }
                }

            } else {
                setupFilter(controller: 'setup', action: '*') {
                    before = {
                        redirect(uri: '/')
                        return false
                    }
                }
            }
        } else {
            setupFilter(controller: '*', action: '*') {
                before = {
                    if (controllerName != 'setup' && !request.getHeader("X-Requested-With") && request.getParameter('ajax') != "true") {
                        redirect(controller: 'setup')
                        return false
                    } else {
                        return true
                    }
                }
            }
        }
    }
}