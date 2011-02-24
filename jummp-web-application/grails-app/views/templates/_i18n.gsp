%{-- Makes grails message codes available to JavaScript --}%
<g:javascript>
var i18n = {
    login: {
        authenticate: "${g.message(code: 'login.authenticate')}",
        cancel:       "${g.message(code: 'login.cancel')}",
        successful:   "${g.message(code: 'login.successful')}"
    },
    logout: {
        successful: "${g.message(code: 'logout.successful')}"
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
        },
        upload: {
            success: "${g.message(code: 'model.upload.success', args: ['_ID_'])}"
        },
        summary: {
            referencePublication: "${g.message(code: 'model.summary.reference-publication')}"
        }
    },
    publication: {
        pubmedid:    "${g.message(code: 'publication.pubmedid')}",
        doi:         "${g.message(code: 'publication.doi')}",
        journal:     "${g.message(code: 'publication.journal')}",
        issue:       "${g.message(code: 'publication.issue')}",
        volume:      "${g.message(code: 'publication.volume')}",
        pages:       "${g.message(code: 'publication.pages')}",
        date:        "${g.message(code: 'publication.date')}",
        affiliation: "${g.message(code: 'publication.affiliation')}",
        synopsis:    "${g.message(code: 'publication.abstract')}",
        authors:     "${g.message(code: 'publication.authors')}"
    },
    error: {
        unexpected: "${g.message(code: 'error.500.explanation', args: ['_CODE_'])}",
        denied:     "${g.message(code: 'error.403.explanation')}"
    }
};
</g:javascript>
