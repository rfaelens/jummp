package net.biomodels.jummp.webapp.administration

import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * @short Controller for changing themes.
 *
 * This Controller offers admin users the possibility to change the
 * theme of the web application. Any change is written to the Jummp
 * configuration file and applied instantly. Which means each user
 * will have the new theme after the next complete load of a web page.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Secured('ROLE_ADMIN')
class ThemeingController {
    /**
     * Dependency injection of springSecurityService
     */
    def springSecurityService

    /**
     * Action to list all available themes.
     */
    def themes = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "THEMES"])
            return
        }
        List<String> themeNames = []
        File themeDir = new File(ServletContextHolder.servletContext.getRealPath("jquery-ui"))
        themeDir.listFiles().each { file ->
            if (file.isDirectory()) {
                themeNames << file.name
            }
        }
        String selectedTheme = null
        if (ConfigurationHolder.config.net.biomodels.jummp.webapp.theme) {
            selectedTheme = ConfigurationHolder.config.net.biomodels.jummp.webapp.theme
        }
        [themes: themeNames, selected: selectedTheme]
    }

    /**
     * Action to change the selected theme.
     * If the change is possible the new selected theme is written into the configuration file.
     */
    def save = { ThemeSaveCommand cmd ->
        if (cmd.hasErrors()) {
            Map data = [error: true]
            switch (cmd.errors.getFieldError("theme").code) {
            case "nullable":
            case "blank":
                data.put("theme", g.message(code: 'theme.save.error.blank'))
                break
            case "validator.invalid":
                data.put("theme", g.message(code: 'theme.save.error.invalid'))
                break
            }
            render data as JSON
        } else {
            ConfigurationHolder.config.net.biomodels.jummp.webapp.theme = cmd.theme
            Properties properties = new Properties()
            File configurationFile = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties")
            if (configurationFile.exists()) {
                properties.load(new FileInputStream(configurationFile))
            }
            properties.setProperty("jummp.theme", cmd.theme)
            properties.store(new FileOutputStream(configurationFile), "Jummp Configuration")
            Map data = [success: true, theme: cmd.theme]
            render data as JSON
        }
    }
}

/**
 * Command object for the controller's save action.
 */
class ThemeSaveCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String theme

    static constraints = {
        theme(nullable: false, blank: false, validator: { name ->
            File cssFile = new File(ServletContextHolder.servletContext.getRealPath("jquery-ui/${name}/${name}.css"))
            return (cssFile.exists() && cssFile.isFile())
        })
    }
}
