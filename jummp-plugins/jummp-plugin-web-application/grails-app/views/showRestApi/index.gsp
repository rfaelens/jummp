<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), 
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the 
 terms of the GNU Affero General Public License as published by the Free 
 Software Foundation; either version 3 of the License, or (at your option) any 
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY 
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more 
 details.

 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.

 Additional permission under GNU Affero GPL version 3 section 7

 If you modify Jummp, or any covered work, by linking or combining it with 
 Apache Commons (or a modified version of that library), containing parts 
 covered by the terms of Apache License v2.0, the licensors of this 
 Program grant you additional permission to convey the resulting work. 
 {Corresponding Source for a non-source form of such a combination shall include 
 the source code for the parts of Apache Commons used as well as that of 
 the covered work.}
--%>

<!DOCTYPE html>
<html>
<head>
  <title>Swagger UI</title>
  <link href='//fonts.googleapis.com/css?family=Droid+Sans:400,700' rel='stylesheet' type='text/css'/>
  <link href='${resource(dir: 'css/swagger', file: 'highlight.default.css')}' media='screen' rel='stylesheet' type='text/css'/>
  <link href='${resource(dir: 'css/swagger', file: 'screen.css')}' media='screen' rel='stylesheet' type='text/css'/>

  <script src="${resource(dir: 'js/swagger/lib', file: 'shred.bundle.js')}"></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'jquery-1.8.0.min.js')}"></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'jquery.slideto.min.js')}" ></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'jquery.wiggle.min.js')}" ></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'jquery.ba-bbq.min.js')}" ></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'handlebars-1.0.0.js')}" ></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'underscore-min.js')}"></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'backbone-min.js')}"></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'swagger.js')}"></script>
  <script src="${resource(dir: 'js/swagger', file: 'swagger-ui.js')}"></script>
  <script src="${resource(dir: 'js/swagger/lib', file: 'highlight.7.3.pack.js')}" ></script>
  <script >
    var baseUrl = '';
    $(function () {
      window.swaggerUi = new SwaggerUi({
        url: "${grailsApplication.config.grails.serverURL}/api-docs",
        dom_id: "swagger-ui-container",
        supportedSubmitMethods: ['get', 'post', 'put', 'delete'],
        onComplete: function (swaggerApi, swaggerUi) {
          if (console) {
            console.log("Loaded SwaggerUI")
          }
          $('pre code').each(function (i, e) {
            hljs.highlightBlock(e)
          });
        },
        onFailure: function (data) {
          if (console) {
            console.log("Unable to Load SwaggerUI");
            console.log(data);
          }
        },
        docExpansion: "none"
      });

      $('#input_apiKey').change(function () {
        var key = $('#input_apiKey')[0].value;
        console.log("key: " + key);
        if (key && key.trim() != "") {
          console.log("added key " + key);
          window.authorizations.add("key", new ApiKeyAuthorization("api_key", key, "query"));
        }
      })
      window.swaggerUi.load();
    });

  </script>
</head>

<body>
<div id='header'>
  <div class="swagger-ui-wrap">
    <a id="logo" href="https://developers.helloreverb.com/swagger/">swagger</a>

    <form id='api_selector'>

      <div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl" type="text"/>
      </div>

      <div class='input'><input placeholder="api_key" id="input_apiKey" name="apiKey" type="text"/></div>

      <div class='input'><a id="explore" href="#">Explore</a></div>
    </form>
  </div>
</div>

<div id="message-bar" class="swagger-ui-wrap">
  &nbsp;
</div>

<div id="swagger-ui-container" class="swagger-ui-wrap">

</div>

</body>

</html>
