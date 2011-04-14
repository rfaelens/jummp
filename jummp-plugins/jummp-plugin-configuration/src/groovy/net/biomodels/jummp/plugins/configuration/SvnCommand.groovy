package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating subversion settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SvnCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String localRepository

    static constraints = {
        localRepository(blank: false,
                        nullable: false,
                        validator: { repository, cmd ->
                            File directory = new File((String)repository)
                            // TODO: test whether the directory is an svn repository
                            return (directory.exists() && directory.isDirectory())
                        })
    }
}
