<%--
 Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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

<head>
    <meta name="layout" content="modelDisplay"/>
    <g:javascript>
        function hideQuestionMark() {
            $('.toolbar').remove();
        }
    </g:javascript>
</head>
<content tag="modelspecifictabs">
    <g:each var="mdlFile" in="${mdlFiles}">
        <mdl:addMenuItem file="${mdlFile}" >
            <li><a href="${fileHref}">${fileName}</a></li>
        </mdl:addMenuItem>
    </g:each>
    <g:each var="dataFile" in="${dataFiles}">
        <mdl:addMenuItem file="${dataFile}" >
            <li><a href="${fileHref}">${fileName}</a></li>
        </mdl:addMenuItem>
    </g:each>
</content>
<content tag="modelspecifictabscontent">
    <g:each var="mdlFile" in="${mdlFiles}">
        <mdl:renderMdlFile file="${mdlFile}"/>
    </g:each>
    <g:each var="dataFile" in="${dataFiles}">
        <mdl:renderDataFile file="${dataFile}"/>
    </g:each>
    <script type="text/javascript">
        SyntaxHighlighter.all();
    </script>
</content>
