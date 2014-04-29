<g:each in="${covariates}">
    <g:if test="${it.continuous}">
        <p class='bold default'>Continuous covariate <span class='italic'>${it.symbId}</span></p>
        <div class="spaced">${it.continuous.dist}</div>
        <div class='spaced'>${it.continuous.transf}</div>
    </g:if>
    <g:if test="${it.categorical}">
        <p class='bold default'>Categorical covariate <span class='italic'>${it.symbId}</span></p>
        <div class="spaced">Categories: ${it.categorical}</div>
    </g:if>
</g:each>
