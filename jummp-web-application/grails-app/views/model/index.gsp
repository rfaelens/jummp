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
                    // i18n
                    oLanguage: {
                        oPaginate: {
                            sFirst:    i18n.dataTables.paginate.first,
                            sLast:     i18n.dataTables.paginate.last,
                            sNext:     i18n.dataTables.paginate.next,
                            sPrevious: i18n.dataTables.paginate.previous
                        },
                        sEmptyTable:   i18n.dataTables.empty,
                        sInfo:         i18n.dataTables.info,
                        sInfoEmpty:    i18n.dataTables.infoEmpty,
                        sInfoFiltered: i18n.dataTables.infoFiltered,
                        sLengthMenu:   i18n.dataTables.lengthMenu,
                        sProcessing:   i18n.dataTables.processing,
                        sSearch:       i18n.dataTables.search,
                        sZeroRecords:  i18n.dataTables.noFilterResults
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
