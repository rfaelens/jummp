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
import net.biomodels.jummp.core.JummpIntegrationTest
import static org.junit.Assert.*
import grails.test.*
import net.biomodels.jummp.core.JummpIntegrationTest
import net.biomodels.jummp.core.FileSystemService
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.webapp.*
import net.biomodels.jummp.model.Model
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import grails.util.Holders
import org.junit.Test
import net.biomodels.jummp.webapp.ModelController
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockMultipartHttpServletRequest
import net.biomodels.jummp.webapp.ModelController

import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.Person
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.UserRole
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.web.servlet.ViewResolver

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.mock.web.MockMultipartHttpServletRequest



public class SubmissionFlowTestBase extends WebFlowTestCase {
	
	def authenticationManager=Holders.applicationContext.getBean("authenticationManager")
    def springSecurityService=Holders.applicationContext.getBean("springSecurityService")
    def controller = new ModelController();
	
	protected void clickCancelEndFlow() {
        signalEvent("Cancel")
        assert "abort" == flowExecutionOutcome.id
    }
   	
    /* Checks the current state against the supplied state id */
    protected void assertFlowState(String state) {
        assert state == flowExecution.activeSession.state.id
    }
    
    boolean createFlow = true;
	 
	def getFlow() {
		 if (createFlow) {
			 return controller.createFlow
		 }
		 return controller.updateFlow
	 }
    
	protected void getToUploadPage() {
    	assertFlowState("displayDisclaimer")
        signalEvent("Continue")
        assertFlowState("uploadFiles")
    }
    
     /**
     * Sets the current authentication to testuser and does not model as admin user.
     * @return The testusers authentication
     */
    protected def authenticateAsTestUser() {
        modelAdminUser(false)
        return authenticate("testuser", "secret")
    }
    
     /**
     * Sets and authentication based on username and password.
     * @param username The name of the user
     * @param password The password of the user.
     * @return The Authentication object
     */
    protected def authenticate(String username, String password) {
        def authToken = new UsernamePasswordAuthenticationToken(username, password)
        System.out.println("Authenticating with "+username+" and "+password);
        def auth = authenticationManager.authenticate(authToken)
        SecurityContextHolder.getContext().setAuthentication(auth)
        return auth
    }
    
      
    /**
     * Modifies the ifAnyGranted method of SpringSecurityUtils to return @p admin value.
     * @param admin if @c true, all access to ifAnyGranted returns @c true, @c false otherwise
     */
    protected void modelAdminUser(boolean admin) {
        SpringSecurityUtils.metaClass.'static'.ifAnyGranted = { String parameter ->
            return admin
        }
    }
    
    
    protected void ensureRoleExists(String _authority) {
        if (!Role.findByAuthority(_authority)) {
            new Role(authority: _authority).save()
        }
    }
   
   /**
     * Creates three users and their roles:
     * @li testuser with password secret and role ROLE_USER
     * @li user with password verysecret and role ROLE_USER
     * @li admin with password 1234 and ROLE_ADMIN and ROLE_USER
     */
    protected void createUserAndRoles() {
        User user, user2, admin, curator
        Person person
        if (!User.findByUsername("testuser")) {
        	person=new Person(userRealName: "Test")
        	user = new User(username: "testuser",
                    password: springSecurityService.encodePassword("secret"),
                    person: person,
                    email: "test@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(person.save(flush:true, failOnError:true)) 
            assertNotNull(user.save())
            assertNotNull(new AclSid(sid: user.username, principal: true).save(flush: true))
            System.out.println("User created: "+user);
        } else {
            user = User.findByUsername("testuser")
            System.out.println("User exists: "+user);
        }
        if (!User.findByUsername("username")) {
            person=new Person(userRealName: "Test2")
        	user2 = new User(username: "username",
                    password: springSecurityService.encodePassword("verysecret"),
                    person: person,
                    email: "test2@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(person.save(flush:true, failOnError:true)) 
            assertNotNull(user2.save())
            assertNotNull(new AclSid(sid: user2.username, principal: true).save(flush: true))
        } else {
            user2 = User.findByUsername("username")
        }
        if (!User.findByUsername("admin")) {
            person=new Person(userRealName: "administrator")
        	admin = new User(username: "admin",
                    password: springSecurityService.encodePassword("1234"),
                    person: person,
                    email: "admin@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(person.save(flush:true, failOnError:true)) 
            assertNotNull(admin.save())
            assertNotNull(new AclSid(sid: admin.username, principal: true).save(flush: true))
        } else {
            admin = User.findByUsername("admin")
        }
        if (!User.findByUsername("curator")) {
            person = new Person(userRealName: "Curator")
        	assertNotNull(person.save(flush:true, failOnError:true)) 
            curator = new User(username: "curator",
                    password: springSecurityService.encodePassword("extremelysecret"),
                    person: person,
                    email: "curator@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(curator.save())
            assertNotNull(new AclSid(sid: curator.username, principal: true).save(flush: true))
        } else {
            curator = User.findByUsername("curator")
        }
        ensureRoleExists("ROLE_USER")
        Role userRole = Role.findByAuthority("ROLE_USER")
        createUserRoleIfNeeded(user, userRole)
        createUserRoleIfNeeded(user2, userRole)
        createUserRoleIfNeeded(admin, userRole)
        createUserRoleIfNeeded(curator, userRole)
        ensureRoleExists("ROLE_ADMIN")
        Role adminRole = Role.findByAuthority("ROLE_ADMIN")
        createUserRoleIfNeeded(admin, adminRole)
        ensureRoleExists("ROLE_CURATOR")
        Role curatorRole = Role.findByAuthority("ROLE_CURATOR")
        createUserRoleIfNeeded(curator, curatorRole)
    }
    
    protected void createUserRoleIfNeeded(User user, Role role) {
    	if (!UserRole.findByUserAndRole(user, role)) {
    		UserRole.create(user, role, false)
    	}
    }

   
    
    // Click through the upload pipeline with the supplied file and test name/description strings
    void fileUploadPipeline(File file, String format, String mname, String[] descriptionStrings) {
        Map<File, String> additionalFiles = getRandomAdditionalFiles(10)
        addSubmissionFiles([file], additionalFiles)
        signalEvent("Upload")
        assertFlowState("displayModelInfo")
        signalEvent("Continue")

        //Dont add publication info
        assertFlowState("enterPublicationLink")
        //(mockRequest as MockHttpServletRequest).setParameter("PubLinkProvider","PUBMED") Doesnt seem to work :/
        //(mockRequest as MockHttpServletRequest).setParameter("PublicationLink","9486845")

        signalEvent("Continue")

        assertFalse((Boolean) flowScope.workingMemory.get("isUpdateOnExistingModel"))
        assertEquals format, flowScope.workingMemory.get("model_type").identifier
        RTC revision = flowScope.workingMemory.get("RevisionTC") as RTC
        //test name
        assertEquals mname, revision.name
        //test that the description contains known strings
        checkDescription(revision.description, descriptionStrings)
        //add tests for when displayModelInfo does something interesting
        assertFlowState("displaySummaryOfChanges")

        signalEvent("Continue")

        assertFlowExecutionOutcomeEquals("displayConfirmationPage")

        //test that the model is infact saved in the database
        String model = mockRequest.session.result_submission as String
        Model thisModel = Model.findByPerennialIdentifier(model)
        assertNotNull thisModel
        Revision rev = modelService.getLatestRevision(thisModel)
        assertNotNull rev
        assertEquals mname, rev.name
        checkDescription(rev.description, descriptionStrings)

        //test that the model is saved in the repository
        List<RepositoryFileTransportCommand> files =
                    modelService.retrieveModelFiles(thisModel)
        validateFiles(files, [file] + additionalFiles.keySet())
    }

    private void checkDescription(String description, String[] descriptionStrings) {
        if (descriptionStrings) {
            descriptionStrings.each {
                assertTrue description.contains(it)
            }
        }
    }
    
    public File getFileForTest(String filename, String text) {
        def tmp = System.getProperty("java.io.tmpdir")
        def testFile=new File(tmp + File.separator + filename)
        testFile.setText(text?: "")
        return testFile
    }
    
    /*
     * Convenience function to create arbitrary additional files with corresponding
     * descriptions.
     */
    protected Map<File, String> getRandomAdditionalFiles(int num) {
        Map<File,String> returnMe = new HashMap<File,String>()
        for (int i = 0; i < num; i++) {
            returnMe.put(getFileForTest("add_file_${i}.xml", "my text is $num"),
                        "this is a description for file $i")
        }
        return returnMe
    }
    
    /* 
     * Convenience function to add the supplied main and additional files
     * to submission
     */
    protected void addSubmissionFiles(List<File> mainFiles, Map<File, String> additionalFiles) {
        mainFiles.each {
            addFileToRequest(it, "mainFile", "application/xml")
        }
        additionalFiles.keySet().each {
            addFileToRequest(it, "extraFiles", "application/xml")
            mockRequest.addParameter("description", additionalFiles.get(it))
        }
    }
    
    /*
     * Convenience function to compare a map of String->byte[] retrieved from
     * the repository with the supplied list of files
     */
    protected void validateFiles(List<RepositoryFileTransportCommand> retrieved, List<File> testFiles) {
        assert retrieved
        Map<String, byte[]> files = new HashMap<String, byte[]>()
        retrieved.each {
            File file = new File(it.path)
            files.put(file.getName(), file.getBytes())
        }
        testFiles.each {
            assert files.containsKey(it.getName())
            byte[] savedFile = files.get(it.getName())
            assert savedFile == it.getBytes()
        }
    }
    
     /* 
     * Adds the supplied file with parameters as a mock multipart file 
     * 
     */
    private void addFileToRequest(File modelFile, String formID, String contentType) {
        final file = new MockMultipartFile(formID, modelFile.getName(), contentType, modelFile.getBytes())
        mockRequest.addFile(file)
    }
    
    private File bigModel() {
        return new File("test/files/BIOMD0000000272.xml")
    }
}