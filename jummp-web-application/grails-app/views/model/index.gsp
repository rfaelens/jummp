<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main"/>
        <jq:plugin name="dataTables"/>
        <title><g:message code="model.list.title"/></title>
        %{--TODO: move JavaScript into own file and delay the loading--}%
        <g:javascript>
            $(document).ready(function() {
                $('#modelTable').dataTable({
                    // TODO: in future it might be interesting to allow filtering
                    bFilter: false,
                    bProcessing: true,
                    bServerSide: true,
                    bJQueryUI: true,
                    sPaginationType: "full_numbers",
                    // TODO: generate links without grails interaction
                    sAjaxSource: "${g.createLink(action: 'dataTableSource')}",
                    // TODO: move function into an own method
                    "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                        // first column is link to model
                        $('td:eq(0)', nRow).html( '<a href="${g.createLink(action: 'show')}/' + aData[0] + '">' + aData[0] + '</a>' );
                        return nRow;
                    },
                    // i18n passed to grails
                    oLanguage: {
                        oPaginate: {
                            sFirst:    "${g.message(code: 'jquery.dataTables.paginate.first')}",
                            sLast:     "${g.message(code: 'jquery.dataTables.paginate.last')}",
                            sNext:     "${g.message(code: 'jquery.dataTables.paginate.next')}",
                            sPrevious: "${g.message(code: 'jquery.dataTables.paginate.previous')}"
                        },
                        sEmptyTable:   "${g.message(code: 'jquery.dataTables.empty')}",
                        sInfo:         "${g.message(code: 'jquery.dataTables.info', args: ["_START_", "_END_", "_TOTAL_"])}",
                        sInfoEmpty:    "${g.message(code: 'jquery.dataTables.infoEmpty')}",
                        sInfoFiltered: "${g.message(code: 'jquery.dataTables.infoFiltered', args: ["_MAX_"])}",
                        sLengthMenu:   "${g.message(code: 'jquery.dataTables.lengthMenu', args: ["_MENU_"])}",
                        sProcessing:   "${g.message(code: 'jquery.dataTables.processing')}",
                        sSearch:       "${g.message(code: 'jquery.dataTables.search')}",
                        sZeroRecords:  "${g.message(code: 'jquery.dataTables.noFilterResults')}"
                    }
                });
                $(document).bind("login", function(event) {
                    $('#modelTable').dataTable().fnDraw();
                });
            });
        </g:javascript>
    </head>
    <body>
        <div id="body">
            <table id="modelTable">
                <thead>
                   <tr>
                      <th><g:message code="model.list.modelId"/></th>
                      <th><g:message code="model.list.name"/></th>
                      <th><g:message code="model.list.publicationId"/></th>
                      <th><g:message code="model.list.lastModificationDate"/></th>
                   </tr>
                </thead>
                <tbody></tbody>
                <tfoot>
                   <tr>
                      <th><g:message code="model.list.modelId"/></th>
                      <th><g:message code="model.list.name"/></th>
                      <th><g:message code="model.list.publicationId"/></th>
                      <th><g:message code="model.list.lastModificationDate"/></th>
                   </tr>
                </tfoot>
            </table>
        </div>
    </body>
</html>
