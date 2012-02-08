<div class="blog-entry-date"><g:formatDate date="${node.publishFrom}" format="dd MMM yyyy 'at' hh:mm"/></div>
<div class="blog-entry-content">
${node.content}
</div>
<div class="blog-entry-post-info">
    <span class="quiet"><wcm:countChildren node="${node}" type="org.weceem.content.WcmComment"/> Comments</span>
</div>
<div class="blog-entry-post-info">
    <span class="quiet">Tags:
    <wcm:join in="${node.tags}" delimiter=", " var="tag">
        <wcm:searchLink mode='tag' query="${tag}">${tag.encodeAsHTML()}</wcm:searchLink>
    </wcm:join>
    </span>
</div>
