package net.biomodels.jummp.model

/**
 * The states a Model can have.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public enum ModelState {
    /**
     * The Model has not yet been curated.
     * It is only visible to the owner and users the owner had granted rights to.
     * This is the default state a Model has after upload.
     */
    UNPUBLISHED,
    /**
     * The Model is currently being curated.
     */
    UNDER_CURATION,
    /**
     * The Model has been curated and at least one Revision is visible to all users.
     */
    PUBLISHED,
    /**
     * The Model has been included in a release.
     */
    RELEASED,
    /**
     * The Model is deleted. The previous state was UNPUBLISHED
     */
    DELETED
}