package org.codehaus.groovy.grails.plugins.springsecurity.acl

/**
 * This class has been aut-generated by the Spring Security ACL plugin.
 * See http://burtbeckwith.github.com/grails-spring-security-acl/docs/manual/index.html
 */
class AclClass {

	String className

	@Override
	String toString() {
		"AclClass id $id, className $className"
	}

	static mapping = {
		className column: 'class'
		version false
	}

	static constraints = {
		className unique: true, blank: false, size: 1..255
	}
}
