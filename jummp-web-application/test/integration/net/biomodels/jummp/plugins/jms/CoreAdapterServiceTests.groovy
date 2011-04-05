package net.biomodels.jummp.plugins.jms

import grails.test.*
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.JummpException
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.user.JummpAuthentication
import net.biomodels.jummp.jms.remote.RemoteJummpApplicationAdapterJmsImpl
import net.biomodels.jummp.jms.remote.RemoteModelAdapterJmsImpl

/**
 * @short Test suite for the CoreAdapterService.
 *
 * These tests are no normal Integration Tests. They communicate with the
 * core application through JMS. Because of that the core application needs to
 * be running with the test-core script which prepares the database in a way
 * that the tests can pass.
 *
 * It is important to remember that each Model added to the database during the
 * test cannot be removed again as the core does not provide the required API calls.
 * The tests have to flag the created Models as deleted to not break following tests.
 * Nevertheless some constraints:
 * @li name has to be unique
 * @li the same test cannot be executed twice against the same core application, it needs
 * to be restarted
 * @li a failing test will cause more tests to fail.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class CoreAdapterServiceTests extends GrailsUnitTestCase {
    def remoteJummpApplicationAdapter
    def remoteModelService
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testAuthenticate() {
        // test an invalid authentication
        shouldFail(AuthenticationException) {
            remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "wrong"))
        }
        shouldFail(AuthenticationException) {
            remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("nosuchuser", "wrong"))
        }
        Authentication result = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(result)
        assertTrue(result instanceof JummpAuthentication)
        assertTrue(result.isAuthenticated())
    }

    void testGetAllModels() {
        // we do not have any models in the database yet
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        List result = remoteModelService.getAllModels()
        assertTrue(result.isEmpty())
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestGetAllModels", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        // now we should have a model
        result = remoteModelService.getAllModels()
        assertFalse(result.isEmpty())
        assertEquals(1, result.size())
        assertEquals(model.id, result[0].id)
        // different user should not see it
        Authentication auth2 = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("user", "verysecret"))
        SecurityContextHolder.context.authentication = auth2
        assertTrue(remoteModelService.getAllModels().isEmpty())
        // for cleaning up: delete
        SecurityContextHolder.context.authentication = auth
        assertTrue(remoteModelService.deleteModel(model.id))
    }

    void testModelCount() {
        // we do not have any models in the database yet
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        Integer result = remoteModelService.getModelCount()
        assertEquals(0, result)
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestGetModelCount", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        // now we should have a model
        result = remoteModelService.getModelCount()
        assertEquals(1, remoteModelService.getModelCount())
        // different user should not see it
        Authentication auth2 = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("user", "verysecret"))
        SecurityContextHolder.context.authentication = auth2
        assertEquals(0, remoteModelService.getModelCount())
        // for cleaning up: delete
        SecurityContextHolder.context.authentication = auth
        assertTrue(remoteModelService.deleteModel(model.id))
    }

    void testGetLatestRevision() {
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestGetLatestRevision", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        RevisionTransportCommand revision = remoteModelService.getLatestRevision(model.id)
        assertNotNull(revision)
        assertEquals(model.id, revision.model.id)
        // different user should not see it
        Authentication auth2 = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("user", "verysecret"))
        SecurityContextHolder.context.authentication = auth2
        shouldFail(AccessDeniedException) {
            remoteModelService.getLatestRevision(model.id)
        }
        // for cleaning up: delete
        SecurityContextHolder.context.authentication = auth
        assertTrue(remoteModelService.deleteModel(model.id))
    }

    void testGetAllRevisions() {
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestGetAllRevisions", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        List<RevisionTransportCommand> revisions = remoteModelService.getAllRevisions(model.id)
        assertFalse(revisions.isEmpty())
        assertEquals(1, revisions.size)
        assertEquals(model.id, revisions.first().model.id)
        // different user should not see it
        Authentication auth2 = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("user", "verysecret"))
        SecurityContextHolder.context.authentication = auth2
        assertTrue(remoteModelService.getAllRevisions(model.id).isEmpty())
        // for cleaning up: delete
        SecurityContextHolder.context.authentication = auth
        assertTrue(remoteModelService.deleteModel(model.id))
    }

    void testUploadModel() {
        // we know that uploading works, due to the method being used in the other tests
        // so we just need to test for the model exception being thrown
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestGetAllRevisions", format: new ModelFormatTransportCommand(identifier: "UNKNOWN"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        shouldFail(ModelException) {
            remoteModelService.uploadModel(modelSource.bytes, meta)
        }
    }

    void testAddRevision() {
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestAddRevision", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        // user should have one revision
        assertEquals(1, remoteModelService.getAllRevisions(model.id).size())
        // adding one revision
        modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="2"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        RevisionTransportCommand revision = remoteModelService.addRevision(model.id, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Add Revision")
        assertNotNull(revision)
        assertEquals(model.id, revision.model.id)
        assertEquals(2, remoteModelService.getAllRevisions(model.id).size())
        assertEquals(revision.id, remoteModelService.getLatestRevision(model.id).id)
        // create a model exception
        shouldFail(ModelException) {
            remoteModelService.addRevision(model.id, modelSource.bytes, new ModelFormatTransportCommand(identifier: "UNKNOWN"), "Add Revision")
        }
        // different user should not see it
        Authentication auth2 = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("user", "verysecret"))
        SecurityContextHolder.context.authentication = auth2
        assertTrue(remoteModelService.getAllRevisions(model.id).isEmpty())
        shouldFail(AccessDeniedException) {
            remoteModelService.addRevision(model.id, "Test".bytes, new ModelFormatTransportCommand(identifier: "UNKNOWN"), "Test")
        }
        // for cleaning up: delete
        SecurityContextHolder.context.authentication = auth
        assertTrue(remoteModelService.deleteModel(model.id))
    }

    void testRetrieveModelFile() {
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestRetrieveModelFile", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        RevisionTransportCommand revision = remoteModelService.getLatestRevision(model.id)
        byte[] modelData = remoteModelService.retrieveModelFile(revision)
        assertEquals(modelSource, new String(modelData))
        Authentication auth2 = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("user", "verysecret"))
        SecurityContextHolder.context.authentication = auth2
        shouldFail(AccessDeniedException) {
            remoteModelService.retrieveModelFile(revision)
        }
        // for cleaning up: delete
        SecurityContextHolder.context.authentication = auth
        assertTrue(remoteModelService.deleteModel(model.id))
    }

    void testDeleteModel() {
        // delete is called from each and every method, so we know it works
        // we just need to try different cases
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestDeleteModel", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        // different user should not be allowed to delete the model
        Authentication auth2 = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("user", "verysecret"))
        SecurityContextHolder.context.authentication = auth2
        shouldFail(AccessDeniedException) {
            remoteModelService.deleteModel(model.id)
        }
        // user himself should be able to delete
        SecurityContextHolder.context.authentication = auth
        assertTrue(remoteModelService.deleteModel(model.id))
        // deleting again should not work
        assertFalse(remoteModelService.deleteModel(model.id))
        // deleting a non-existant model should not work
        shouldFail(AccessDeniedException) {
            // passing 0 through JMS seems not to work
            remoteModelService.deleteModel(10000000l)
        }
    }

    void testRestoreModel() {
        // first authenticate
        Authentication auth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("testuser", "secret"))
        SecurityContextHolder.context.authentication = auth
        // upload one model
        ModelTransportCommand meta = new ModelTransportCommand(name: "coreTestRestoreModel", format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test")
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        ModelTransportCommand model = remoteModelService.uploadModel(modelSource.bytes, meta)
        // delete the model
        assertTrue(remoteModelService.deleteModel(model.id))
        // restore not allowed to user
        shouldFail(AccessDeniedException) {
            remoteModelService.restoreModel(model.id)
        }
        // admin user is allowed
        Authentication adminAuth = remoteJummpApplicationAdapter.authenticate(new UsernamePasswordAuthenticationToken("admin", "1234"))
        SecurityContextHolder.context.authentication = adminAuth
        assertTrue(remoteModelService.restoreModel(model.id))
        // restoring again should not be possible
        assertFalse(remoteModelService.restoreModel(model.id))
        shouldFail(IllegalArgumentException) {
            // model does not exist - it should fail
            remoteModelService.restoreModel(0)
        }
    }

    /*void testValidateReturnValue() {
        shouldFail(JummpException) {
            coreAdapterService.validateReturnValue(null, String.class)
        }
        shouldFail(JummpException) {
            coreAdapterService.validateReturnValue("test", Integer.class)
        }
        shouldFail(Exception) {
            coreAdapterService.validateReturnValue(new Exception(), String.class)
        }
        // shouldn't fail
        coreAdapterService.validateReturnValue("test", String.class)
        coreAdapterService.validateReturnValue(1, Integer.class)
        coreAdapterService.validateReturnValue(true, Boolean.class)
    }*/
}
