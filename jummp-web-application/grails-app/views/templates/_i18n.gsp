%{-- Makes grails message codes available to JavaScript --}%
<g:javascript>
var i18n = {
    login: {
        authenticate: "${g.message(code: 'login.authenticate')}",
        cancel:       "${g.message(code: 'login.cancel')}"
    },
    dataTables: {
        paginate: {
            first:    "${g.message(code: 'jquery.dataTables.paginate.first')}",
            last:     "${g.message(code: 'jquery.dataTables.paginate.last')}",
            next:     "${g.message(code: 'jquery.dataTables.paginate.next')}",
            previous: "${g.message(code: 'jquery.dataTables.paginate.previous')}"
        },
        empty:           "${g.message(code: 'jquery.dataTables.empty')}",
        info:            "${g.message(code: 'jquery.dataTables.info', args: ["_START_", "_END_", "_TOTAL_"])}",
        infoEmpty:       "${g.message(code: 'jquery.dataTables.infoEmpty')}",
        infoFiltered:    "${g.message(code: 'jquery.dataTables.infoFiltered', args: ["_MAX_"])}",
        lengthMenu:      "${g.message(code: 'jquery.dataTables.lengthMenu', args: ["_MENU_"])}",
        processing:      "${g.message(code: 'jquery.dataTables.processing')}",
        search:          "${g.message(code: 'jquery.dataTables.search')}",
        noFilterResults: "${g.message(code: 'jquery.dataTables.noFilterResults')}"
    },
    model: {
        list: {
            download: "${g.message(code: 'model.list.table.download')}"
        }
    }
};
</g:javascript>
