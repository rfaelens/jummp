<head>
    <meta name="layout" content="modelDisplay"/>
    <style>
        td, th {
            padding: 5px;
        }
        th {
            text-align: center;
            vertical-align: middle;
        }
    </style>
</head>
<content tag="modelspecifictabs">
    <li><a href="#customDefinitions">Custom Definitions</a></li>
    <li><a href="#modelDefinition">Model Definition</a></li>
    <li><a href="#trialDesign">Trial Design</a></li>
    <li><a href="#modellingSteps">Modelling Steps</a></li>
</content>
<content tag="modelspecifictabscontent">
    <div id="customDefinitions">
        <p><strong>Independent variable</strong>&nbsp;${independentVar}</p>
        <h3>Symbol Definitions</h3>
            <pharmml:symbolDefinitions symbolDefs="${symbolDefs}"/>
   </div>

    <div id="modelDefinition">
        <h3>Variability</h3>
        <pharmml:variabilityLevel level="${variabilityLevel}"/>

        <h3>Covariates</h3>
        <table>
            <thead>
                <tr>
                    <th rowspan="2">Identifier</th>
                    <th rowspan="2">Parameters</th>
                    <th colspan="4">Covariate</th>
                </tr>
                <tr>
                    <th>Type</th>
                    <th>Identifier</th>
                    <th>Name</th>
                    <th>Transformation</th>
                </tr>
            </thead>
            <tbody>
            <tr>
                <td class="value">${covariateModel[0].id}</td>
                <td class="value">
                    <pharmml:covariateParameters parameter="${covariateModel[0].parameter}"/>
                </td>
                <pharmml:covariates covariate="${covariateModel[0].covariate}"/>
            </tr>
            </tbody>
        </table>

        <h3>Model Definition Parameters</h3>
        <table>
            <tr>
                <td class="key"></td>
                <td class="value">
                </td>
            </tr>
        </table>
        <h3>Structural Model</h3>
        <table>
            <tr>
                <td class="key"></td>
                <td class="value">
                </td>
            </tr>
        </table>
        <h3>Observations</h3>
        <pharmml:observations observations="${observationModel}"/>
 </div>

    <div id="trialDesign">
       <h3>Treatments</h3>
       <pharmml:treatment treatment="${treatment}"/>
       <h3>Epoch</h3>
       <pharmml:treatmentEpoch epoch="${treatmentEpoch}"/>
       <h3>Group</h3>
       <pharmml:group group="${group}"/>
    </div>

    <div id="modellingSteps">
       <pharmml:checkModellingSteps steps="${modellingSteps}"/>
       <pharmml:variableDefs variables="${variableDefinitions}"/>
       <pharmml:estSimSteps steps="${estSimSteps}"/>
    </div>
</content>
