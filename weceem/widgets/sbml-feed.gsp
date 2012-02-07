<div class="element external">
  <h1>Latest SBML News</h1>
  <div class="rss"><a href="http://sbml.org/index.php?title=News&action=feed&feed=rss">R</a></div>
  <wcm:dataFeed type="atom" url="http://sbml.org/index.php?title=News&action=feed" max="1" custom="true">
    <h2>${item.title.encodeAsHTML()}</h2>
<%
def regex = /<\/?(.|\n)*?>/
%>
${item.summary.text().replaceAll(regex, '')}
  </wcm:dataFeed>
  <p><a href="http://sbml.org/News/SBML" target="_blank">more</a></p>
</div>
