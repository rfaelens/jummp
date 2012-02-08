<div class="element">
  <g:set var="newsBlog" value="${wcm.findNode(path:'news')}"/>
  <h1>News</h1>
  <wcm:eachChild path="${newsBlog}" type="org.weceem.blog.WcmBlogEntry" max="1" sort="publishFrom" order="desc">
    <div class="rss">
        <a href="wcm-tools/feed/rss/jummp/news">
            <img src="${resource(dir: 'images', file: 'feed.png')}"/>
        </a>
    </div>
    <h2><a href="${wcm.createLink(path: it)}">${it.title.encodeAsHTML()}</a></h2>
${it.summary}
  </wcm:eachChild>
  <g:if test="${wcm.countChildren() == '0'}">
    <h2>No News published yet</h2>
  </g:if>
  <p>
    <a href="${wcm.createLink(path: 'news')}">more</a>
  </p>
</div>
