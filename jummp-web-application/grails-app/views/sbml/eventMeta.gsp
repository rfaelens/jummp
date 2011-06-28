<table>
    <jummp:contentMathMLTableRow mathML="${event.trigger}" title="${g.message(code: 'sbml.event.trigger.title')}"/>
    <jummp:contentMathMLTableRow mathML="${event.delay}" title="${g.message(code: 'sbml.event.delay.title')}"/>
    <jummp:sboTableRow sbo="${event.sbo}"/>
    <jummp:annotationsTableRow annotations="${event.annotation}"/>
    <sbml:notesTableRow notes="${event.notes}"/>
</table>
