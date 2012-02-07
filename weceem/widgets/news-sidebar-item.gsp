<div class="element">
  <g:set var="newsBlog" value="${wcm.findNode(path:'news')}"/>
  <wcm:eachChild path="${newsBlog}" type="org.weceem.blog.WcmBlogEntry" max="1" sort="publishFrom" order="desc">
    <h1>News</h1>
    <div class="rss">
        <a href="wcm-tools/feed/rss/jummp/news">
            <img src="${resource(dir: 'images', file: 'feed.png')}"/>
        </a>
    </div>
    <h2>${it.title.encodeAsHTML()}</h2>
${it.summary}
    <p>
      <a href="${wcm.createLink(path: it)}">more</a>
    </p>
  </wcm:eachChild>
  <p>
    <a href="${wcm.createLink(path: 'news')}">all news</a>
  </p>
</div>
