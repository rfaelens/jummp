<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



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

