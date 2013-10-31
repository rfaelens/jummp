/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


// configuration for plugin testing - will not be included in the plugin zip
Properties jummpProps = new Properties()
try {
    jummpProps.load(new FileInputStream("${userHome}${System.getProperty('file.separator')}.jummp.properties"))
}
catch (Exception ignored) {
}
def cfg = new ConfigSlurper().parse(jummpProps)
jummp.vcs.workingDirectory = cfg.jummp.vcs.workingDirectory
jummp.vcs.exchangeDirectory = cfg.jummp.vcs.exchangeDirectory
 
log4j = {
    // as of Grails 2.2.2, the stacktrace goes to java.io.tmpdir or Tomcat's logs folder
    appenders {
        file name: "stacktrace", append: true, file: "logs/jummp-plugin-subversion_stacktrace.log"
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
    rollingFile name: "pluginAppender", file: "logs/jummp-plugin-subversion.log", threshold: org.apache.log4j.Level.ALL
    info  pluginAppender: [ 'grails.app', 'net.biomodels.jummp.plugins.subversion' ]
}

grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.enable.native2ascii=true
