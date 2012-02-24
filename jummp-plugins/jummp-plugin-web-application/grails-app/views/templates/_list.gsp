<g:applyLayout name="main">
<content tag="main-content">

<button onClick="load()">load models</button>

<div id="modelNavigation">
    <span id="modelNavigationOffset" style="display: none">${offset ? offset : "0"}</span>
    <span id="modelNavigationSorting" style="display: none">${sort ? sort : "0"}</span>
    <span id="modelNavigationDirection" style="display: none">${dir ? dir : "asc"}</span>
</div>
<table id="modelTable">
    <thead>
       <tr>
          <th><g:message code="model.list.modelId"/></th>
          <th><g:message code="model.list.name"/></th>
          <th><g:message code="model.list.publicationId"/></th>
          <th><g:message code="model.list.lastModificationDate"/></th>
          <th><g:message code="model.list.format"/></th>
       </tr>
    </thead>
    <tbody></tbody>
    <tfoot>
       <tr>
          <th><g:message code="model.list.modelId"/></th>
          <th><g:message code="model.list.name"/></th>
          <th><g:message code="model.list.publicationId"/></th>
          <th><g:message code="model.list.lastModificationDate"/></th>
          <th><g:message code="model.list.format"/></th>
       </tr>
    </tfoot>
</table>

<div id="body" class="ui-widget"></div>

</content>

<g:javascript>

function load() {
console.log('here!');
//loadModelListCallback();
    <g:if test="${data}">
        loadView("${link}", ${callback}, "${data}");
    </g:if>
    <g:else>
        loadView("${link}", ${callback});
    </g:else>
};

</g:javascript>

</g:applyLayout>