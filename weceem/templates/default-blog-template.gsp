<html>
<head>
    <wcm:widget path="widgets/head"/>
    <wcm:ifContentIs type="org.weceem.blog.WcmBlog">  
        <wcm:feedLink type="rss" path="${node.absoluteURI}"/>  
        <wcm:feedLink type="atom" path="${node.absoluteURI}"/>  
    </wcm:ifContentIs>
</head>
<body>
    <wcm:widget path="widgets/content-head"/>

    <div id="container">
        <wcm:widget path="widgets/navigation"/>
        <wcm:widget path="widgets/branding"/>
        <div id="contentContainer">
            <div id="socialMedia">
            <!-- TODO: integrate social media button or use area for other part -->
            </div>
            <div id="content">
                <div id="main">
                  <!-- If the template is on a Blog node, render list of articles -->
                  <wcm:ifContentIs type="org.weceem.blog.WcmBlog">
                    <wcm:eachChild type="org.weceem.blog.WcmBlogEntry" var="child" max="${node.maxEntriesToDisplay}" sort="publishFrom" order="desc">
                      <!-- Render the BlogEntry using the widget -->
                      <div class="blog-entry">
                        <h1><wcm:link node="${child}">${child.title.encodeAsHTML()}</wcm:link></h1>
                        <wcm:widget model="[node:child]" path="widgets/blog-entry-widget"/>
                      </div>
                    </wcm:eachChild>
                  </wcm:ifContentIs>
                  <!-- If the template is on a BlogEntry node, render just this article -->
                  <wcm:ifContentIs type="org.weceem.blog.WcmBlogEntry">
                        <!-- Render the BlogEntry using the widget -->
                    <h1>${node.title}</h1>
                    <div class="blog-entry">
                      <wcm:widget model="[node:node]" path="widgets/blog-entry-widget"/>
                    </div>
                    <wcm:ifUserCanEdit>
                      <g:link controller="wcmEditor" action="edit" id="${node.id}">Edit</g:link>    
                    </wcm:ifUserCanEdit>
                  </wcm:ifContentIs>
                </div>
                <div id="sideBar">
                  <wcm:ifContentIs type="org.weceem.blog.WcmBlog">
                    <div class="element">
                      <h1>Archive</h1>
                      <h2></h2>
                      <p>
                      <ul>
                        <wcm:archiveList path="${node}">
                          <li><a href="${link}">${wcm.monthName(value:month).encodeAsHTML()} ${year}</a></li>
                        </wcm:archiveList>
                      </ul>
                      </p>
                    </div>
                  </wcm:ifContentIs>
                </div>
            </div>
            <wcm:widget path="widgets/links"/>
        </div>
    </div>
    <wcm:widget path="widgets/footer"/>
    <r:layoutResources/>
</body>
</html>
