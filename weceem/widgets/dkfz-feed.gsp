<div class="element external">
  <h1>Latest DKFZ News</h1>
  <div class="rss">
    <a href="http://www.dkfz.de/xml/rssfeed/2.0/en/index.php">
        <img src="${resource(dir: 'images', file: 'feed.png')}"/>
    </a>
  </div>
  <wcm:dataFeed url="http://www.dkfz.de/xml/rssfeed/2.0/en/index.php" max="1" custom="true">
    <h2><a href="${item.link}" target="_blank">${item.title.encodeAsHTML()}</a></h2>
    <p>${item.description}</p>
  </wcm:dataFeed>
  <p><a href="http://www.dkfz.de/en/presse/pressemitteilungen/" target="_blank">more</a></p>
</div>
