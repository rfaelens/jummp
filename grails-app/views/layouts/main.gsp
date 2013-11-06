<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the
 terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>











<!doctype html>
<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/precursor" />
<html>
<head>
    <g:javascript library="jquery" plugin="jquery"/>
    <g:javascript src="jquery/jquery.i18n.properties-min-1.0.9.js"/>
    <g:javascript>
    	$.appName = "${grailsApplication.metadata["app.name"]}";
    	$.serverUrl = "${grailsApplication.config.grails.serverURL}";
    	$.i18n.properties({
    		name: 'messages',
    		path: "${grailsApplication.config.grails.serverURL}/js/i18n/",
    		mode: "map"
    	});
    </g:javascript>
    <g:javascript src="jummp.js"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/head" />
    <g:layoutHead/>
</head>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/header"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/mainbody"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/footer"/>
</html>
