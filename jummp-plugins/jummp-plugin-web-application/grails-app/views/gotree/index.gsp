<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Gene Ontology Tree</title>
        <meta name="layout" content="main" />
        <link rel="stylesheet" href="${resource(dir:'css/dynatree', file:'ui.dynatree.css')}" type="text/css"/>
        <g:javascript src="js/showModels.js"/>
        <g:javascript src="js/gotree.js"/>
    </head>
    <body activetab="search">
        <div class="ui-widget">
            <table>
            <tr>
                <td><label for="gotree-filter">Filter Go Tree:</label></td>
                <td><input id="gotree-filter"/></td>
            </tr>
            </table>
        </div>
        <div id="gotree"></div>
        <g:javascript>
$(function() {
    $.jummp.gotree.load();
});
        </g:javascript>
    </body>
    <content tag="sidebar">
        <div class="element">
            <h1>Gene Ontology Relationships</h1>
            <h2></h2>
            <table>
                <tbody>
                    <tr>
                        <td><r:img dir="css/dynatree" file="go_isa.gif"/></td>
                        <td>is a</td>
                    </tr>
                    <tr>
                        <td><r:img dir="css/dynatree" file="go_partof.gif"/></td>
                        <td>part of</td>
                    </tr>
                    <tr>
                        <td><r:img dir="css/dynatree" file="go_devfrom.gif"/></td>
                        <td>develops from</td>
                    </tr>
                    <tr>
                        <td><r:img dir="css/dynatree" file="go_other.gif"/></td>
                        <td>other</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </content>
</html>
