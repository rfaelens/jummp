<g:each in="${covariates}">
    <g:if test="${it.continuous}">
        <p class='bold default'>Continuous covariate <span class='italic'>${it.symbId}</span></p>
        <div class="spaced-top-bottom">${it.continuous.dist}</div>
        <g:if test="${it.continuous.transf}">
            <div>${it.continuous.transf}</div>
        </g:if>
    </g:if>
    <g:if test="${it.categorical}">
        <p class='bold default'>Categorical covariate <span class='italic'>${it.symbId}</span></p>
        <div class="spaced-top-bottom">Categories: ${it.categorical}</div>
    </g:if>
</g:each>
