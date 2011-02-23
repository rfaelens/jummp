<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>${revision.model.name}</title>
    </head>
    <body>
        <div id="body">
            <div id="modelTabs">
                <ul>
                    <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Model</a></li>
                    <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Overview</a></li>
                    <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Math</a></li>
                    <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Physical Entities</a></li>
                    <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Parameters</a></li>
                    <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Curation</a></li>
                </ul>
            </div>
            <g:javascript>
            $(document).ready(function() {
                $("#modelTabs").tabs({disabled: [1, 2, 3, 4, 5]});
            });
            </g:javascript>
        </div>
    </body>
</html>
