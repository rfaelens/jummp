<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the
 terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>











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
    <li><a href="#modelDefinition">Model Definition</a></li>
    <li><a href="#trialDesign">Trial Design</a></li>
    <li><a href="#modellingSteps">Modelling Steps</a></li>
</content>
<content tag="modelspecifictabscontent">
    <div id="modelDefinition">

        <p><strong>Independent variable</strong>&nbsp;${independentVar}</p>

        <h3>Function Definitions</h3>
        <pharmml:functionDefinitions functionDefs="${functionDefs}"/>

        <pharmml:structuralModel sm="${structuralModel}" iv="${independentVar}"/>

        <pharmml:variabilityModel variabilityModel="${variabilityModel}"/>

        <pharmml:covariates covariate="${covariateModel}" />

        <pharmml:parameterModel parameterModel="${parameterModel}" covariates="${covariateModel}" />

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
       <pharmml:modellingSteps steps="${modellingSteps}" independentVariable="${independentVar}" />
       <pharmml:stepDeps deps="${stepDeps}" />
    </div>
</content>
