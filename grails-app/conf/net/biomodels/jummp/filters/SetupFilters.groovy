package net.biomodels.jummp.filters

import org.springframework.beans.factory.InitializingBean

/**
 * @short Filter to redirect to SetupController
 *
 * The filter checks whether the application instance has been configured yet.
 * That is, it tests for the availability of the file @c ~/.jummp.properties.
 * When the file does not exist, all requests (except Ajax requests) are redirected
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
class SetupFilters implements InitializingBean {
    private boolean configFileExists = false
    private boolean firstRun = false

    public void afterPropertiesSet() throws Exception {
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
                setupFilter1(controllerExclude: 'setup') {
                    before = {
                        redirect(controller: 'setup', action: "firstRun")
                        return true
                    }
                }
                flowFilter(controller: 'setup', action: 'setup') {
                    before = {
                        redirect(controller: 'setup', action: "firstRun")
                        return true
                    }
                }

            } else {
                setupFilter2(controller: 'setup', action: '*') {
                    before = {
                        redirect(uri: '/')
                        return false
                    }
                }
            }
        } else {
            setupFilter3(controllerExclude: 'setup') {
                before = {
                    redirect(controller: 'setup')
                    return true
                }
            }
            firstRun(controller: 'setup', action: 'firstRun') {
                before = {
                    redirect(controller: 'setup', action: 'setup')
                    return true
                }
            }
        }
    }
}
