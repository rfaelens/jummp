<g:each in="${covariates}">
    <g:if test="${it.continuous}">
        <p class='bold'>Continuous covariate <span class='italic'>${it.symbId}</span></p>
        <p>${it.continuous.dist}</p>
        <p>${it.continuous.transf}</p>
    </g:if>
</g:each>
