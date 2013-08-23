<meta name="layout" content="modelDisplay"/>
<content tag="modelspecifictabs">
    <li><a href="#general">General Information</a></li>
    <li><a href="#modelDefinition">Model Definition</a></li>
    <li><a href="#trialDesign">Trial Design</a></li>
    <li><a href="#modellingSteps">Modelling Steps</a></li>
</content>
<content tag="modelspecifictabscontent">
    <div id="general">
        <table>
            <tr>
                <td class="key">Independent variable</td>
                <td class="value">${independentVar}</td>
            </tr>
        </table>

        <h3>Symbol Definitions</h3>
            <pharmml:symbolDefinitions symbolDefs="${symbolDefs}"/>
    </div>

    <div id="modelDefinition">
        <h3>Variability Model</h3>
        <pharmml:variabilityLevel level="${variabilityLevel}"/>
        <h3>Covariate Model</h3>
        <table>
            <tr>
                <td class="key">Id</td>
                <td class="value">${covariateModel[0].id}</td>
            </tr>
            <tr>
                <td class="key">Parameters</td>
                <td class="value">
                    <pharmml:covariateModelParameters parameter="${covariateModel[0].parameter}"/>
                </td>
            </tr>
            <tr>
                <td class="key">Covariate</td>
                <td class="value">
                    <pharmml:covariateModelCovariate covariate="${covariateModel[0].covariate}"/>
                </td>
            </tr>
        </table>
        <h3>Parameter Model</h3>
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
      <h3>Observation Model</h3>
           <table>
            <tr>
                <td class="key"></td>
                <td class="value">
                </td>
            </tr>
        </table>
 </div>

    <div id="trialDesign">
       <p>${trialDesign}</p>
    </div>

    <div id="modellingSteps">
       <p>${modellingSteps}</p>
    </div>
</content>
