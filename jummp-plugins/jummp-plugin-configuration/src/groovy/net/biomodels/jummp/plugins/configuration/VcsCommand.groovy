package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating version control system settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Validateable
class VcsCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String vcs
    String workingDirectory
    String exchangeDirectory

    static constraints = {
        vcs(blank: false,
            nullable: false,
            validator: { vcs, cmd ->
                return (vcs == "svn" || vcs == "git")
            })
        workingDirectory(blank: true,
                nullable: false,
                validator: { workingDirectory, cmd ->
                    if (!workingDirectory.isEmpty()) {
                        // if it is not empty it has to be a directory
                        File directory = new File((String)workingDirectory)
                        if (!directory.exists() || !directory.isDirectory()) {
                            return false
                        }
                        if (workingDirectory == cmd.exchangeDirectory) {
                            return false
                        }
                    }
                    if (cmd.vcs == "git") {
                        // TODO: verify that workingDirectory is a git directory
                        return !workingDirectory.isEmpty()
                    }
                    return true
                })
        exchangeDirectory(blank: true,
                nullable: false,
                validator: { exchangeDirectory, cmd ->
                    if (!exchangeDirectory.isEmpty()) {
                        // if it is not empty it has to be a directory
                        File directory = new File((String)exchangeDirectory)
                        if (!directory.exists() || !directory.isDirectory()) {
                            return false
                        }
                        if (exchangeDirectory == cmd.workingDirectory) {
                            return false
                        }
                    }
                    return true
                })
    }

    /**
     * @return @c true if object is for git, @c false otherwise
     */
    boolean isGit() {
        return vcs == "git"
    }

    /**
     * @return @c true if object is for subversion, @c false otherwise
     */
    boolean isSvn() {
        return vcs == "svn"
    }

    /**
     * @return @c git for git, @c subversion for svn and empty string for incorrect value
     */
    String pluginName() {
        if (isGit()) {
            return "git"
        }
        if (isSvn()) {
            return "subversion"
        }
        return ""
    }
}
