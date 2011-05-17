<div id="model-parameters">
    <table>
    <sbml:renderParameters title="Global Parameters"  parameters="${parameters}"/>
<g:each var="reaction" in="${reactionParameters}">
    <sbml:renderParameters title="${reaction.name}" parameters="${reaction.parameters}"/>
</g:each>
    </table>
</div>
