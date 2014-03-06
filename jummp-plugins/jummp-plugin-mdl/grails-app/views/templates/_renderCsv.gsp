<div id="${id}">
    <div id="${id}_text">
    </div>
    <g:javascript>
        var output = getCSVData("${text}");
        document.getElementById("${id}_text").innerHTML = output;
    </g:javascript>
</div>
