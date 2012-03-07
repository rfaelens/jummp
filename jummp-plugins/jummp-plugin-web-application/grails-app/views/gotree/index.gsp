<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Gene Ontology Tree</title>
        <meta name="layout" content="main" />
        <link rel="stylesheet" href="${resource(dir:'css/dynatree', file:'ui.dynatree.css')}" type="text/css"/>
        <r:require module="jquery-ui"/>
        <r:require module="gotree"/>
    </head>
    <body activetab="search">
        <div id="gotree"></div>
        <r:script>
$(function() {
    $.jummp.gotree.load();
});
        </r:script>
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
