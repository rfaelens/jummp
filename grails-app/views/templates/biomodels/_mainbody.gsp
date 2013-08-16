<div id="content" role="main" class="grid_24 clearfix">
	
	<g:if test="${g.pageProperty(name:'page.sidebar')?.length()}">
		<section class="grid_18 alpha"> 
		        <g:pageProperty name="page.main-content" />
		        <g:layoutBody/>
	        </section>  
    
	        <section class="grid_6 omega">
			<g:pageProperty name="page.sidebar"/>
		</section>
	</g:if>
	<g:else>
		<section> 
		        <g:pageProperty name="page.main-content" />
		        <g:layoutBody/>
	        </section>  
	</g:else>
    
</div>

