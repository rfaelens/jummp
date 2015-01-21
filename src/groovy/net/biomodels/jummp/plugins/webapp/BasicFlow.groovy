/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/



package net.biomodels.jummp.plugins.webapp
import grails.util.Holders
import net.biomodels.jummp.webapp.ModelController
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import grails.test.WebFlowTestCase


class BasicFlow extends FlowBase {
	 boolean createFlow = true;
	 def closure;
	 
	 public BasicFlow(boolean create) {
		 createFlow = create;
	 }
	 
	 public void setTest(def cls) {
	 	 closure = cls;
	 	 closure.delegate = this;
	 }
	 
	 public void performTest() {
	 	 closure()
	 }
	 
	 def getFlow() {
		 if (createFlow) {
			 return new ModelController().createFlow
		 }
		 return new ModelController().updateFlow
	 }
}