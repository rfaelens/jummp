package net.biomodels.jummp.webapp

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpRequestResponseHolder
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletException
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.springframework.web.filter.GenericFilterBean
import net.biomodels.jummp.core.user.JummpAuthentication
import org.springframework.security.core.Authentication
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

/**
 * @short Custom filter clearing context, if a user's authentication is removed.
 *
 * This custom filter is registered in the filter chain after SECURITY_CONTEXT_FILTER.
 * It ensures, that users' authentications which are due to inactive period removed,
 * do not throw exceptions in the web application and display the logged off users
 * the login dialog.
 *
 * @author  Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class CheckAuthenticationFilter extends GenericFilterBean {
    /**
     * Dependency injection for the remoteJummpApplicationAdapter
     */
    def remoteJummpApplicationAdapter
    /**
     * Ensures, that the filter is only applied once per request
     */
    static final String FILTER_APPLIED = "__jummp_security_caf_applied"

    /**
     * Filter clearing the context, if the core removed a user's authentication.
     * @param req ServletRequest
     * @param res ServletResponse
     * @param chain FilterChain
     * @throws IOException
     * @throws ServletException
     */
    void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        if (request.getAttribute(FILTER_APPLIED)) {
            // ensure that filter is only applied once per request
            chain.doFilter(request, response)
            return
        }
        HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response)

        try {
            Authentication authentication = SecurityContextHolder.context.authentication
            if (authentication && authentication instanceof JummpAuthentication) {
                boolean authenticated = remoteJummpApplicationAdapter.isAuthenticated(authentication)
                if(!authenticated) {
                    SecurityContextHolder.clearContext()
                }
            }
            chain.doFilter(holder.getRequest(), holder.getResponse())

        } finally {
            request.removeAttribute(FILTER_APPLIED)
        }
    }
}
