package net.biomodels.jummp.dbus;

import groovy.util.ConfigObject;
import net.biomodels.jummp.dbus.authentication.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

/**
 * @short Concrete implementation of ApplicationDBusAdapter.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class ApplicationDBusAdapterImpl extends AbstractDBusAdapter implements ApplicationDBusAdapter {
    /**
     * Dependency Injection of AuthenticationManager
     */
    private AuthenticationManager authenticationManager;
    /**
     * Dependency Injection of grailsApplication
     */
    def grailsApplication

    /**
     * Default empty constructor
     */
    public ApplicationDBusAdapterImpl() {}

    public DBusAuthentication authenticate(String userName, String password) throws AuthenticationDBusException {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
            DBusAuthentication dbusAuth = DBusAuthentication.fromAuthentication(auth);
            String hash = authenticationHashService.hashAuthentication(auth);
            dbusAuth.setHash(hash);
            return dbusAuth;
        } catch (BadCredentialsException e) {
            throw new BadCredentialsDBusException(e.getMessage());
        } catch (AccountExpiredException e) {
            throw new AccountExpiredDBusException(e.getMessage());
        } catch (CredentialsExpiredException e) {
            throw new CredentialsExpiredDBusException(e.getMessage());
        } catch (DisabledException e) {
            throw new DisabledDBusException(e.getMessage());
        } catch (LockedException e) {
            throw new LockedDBusException(e.getMessage());
        } catch (AuthenticationException e) {
            throw new AuthenticationDBusException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, String> getJummpConfig() {
        return (Map<String, String>)((ConfigObject)grailsApplication.config.get("jummp")).flatten();
    }

    public boolean isAuthenticated(String hash) {
        return authenticationHashService.isAuthenticated(hash);
    }

    /**
     * Setter for Dependency Injection of AuthenticationManager
     * @param authenticationManager The AuthenticationManager
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
