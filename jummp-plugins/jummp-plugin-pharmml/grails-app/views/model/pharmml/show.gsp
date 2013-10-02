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
        .default {
            margin:0;
        }
        .bold {
            font-weight: bold;
        }
        .spaced {
            padding: 10px 0px;
        }
        .italic {
            font-style: italic;
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
        <h3>Function Definitions</h3>
            <pharmml:functionDefinitions functionDefs="${functionDefs}"/>
   </div>

    <div id="modelDefinition">
        <pharmml:structuralModel sm="${structuralModel}" iv="${independentVar}"/>

        <pharmml:variabilityModel variabilityModel="${variabilityModel}"/>

        <pharmml:covariates covariate="${covariateModel}" />

        <pharmml:parameterModel parameterModel="${parameterModel}" />

        <pharmml:observations observations="${observationModel}"/>
 </div>

    <div id="trialDesign">
       <h3>Treatments</h3>
       <%--pharmml:treatment treatment="${treatment}"/--%>
       <h3>Epoch</h3>
       <%--pharmml:treatmentEpoch epoch="${treatmentEpoch}"/--%>
       <h3>Group</h3>
       <%--pharmml:group group="${group}"/--%>
    </div>

    <div id="modellingSteps">
       <pharmml:modellingSteps steps="${modellingSteps}"/>
       <pharmml:stepDeps deps="${stepDeps}" />
    </div>
</content>
