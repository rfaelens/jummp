<div id="model-parameters">
    <table>
    <sbml:renderParameters title="${g.message(code: 'sbml.parameters.globalParameters')}" parameters="${parameters}"/>
<g:each var="reaction" in="${reactionParameters}">
    <sbml:renderParameters title="${reaction.name}" parameters="${reaction.parameters}"/>
</g:each>
    </table>
</div>
