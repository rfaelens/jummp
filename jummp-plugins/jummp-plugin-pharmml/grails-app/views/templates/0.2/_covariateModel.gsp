<h3>Covariate Model</h3>
<div>
    <g:each var="cm" in="${covariateModels}">
        <g:if test="${cm.parameters}">
            <div><span class="bold">Parameters</span></div>
            <div class='spaced'>
            <pharmml:simpleParamsClosure simpleParameters="${cm.parameters}"
                        transfMap="${transfMap}" version="${version}" />
            </div>
            <g:if test="${cm.covariates}">
                <pharmml:covariatesClosure covariates="${cm.covariates}" blkId="${cm.blkId}"
                        transf="${transfMap}" version="${version}" />
            </g:if>
        </g:if>
    </g:each>
    <g:if test="${error}">
        <p>${error}</p>
    </g:if>
</div>
