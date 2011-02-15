package net.biomodels.jummp.core.model

/**
 * @short Enum for identifying the where publication information is hosted.
 *
 * This enum is meant for keeping information on where information about a publication
 * is stored. That is to which website a link has to be point if a specific ID is
 * presented. It is meant to be stored in the Publication domain object.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public enum PublicationLinkProvider {
    /**
     * Link points to PubMed
     */
    PUBMED,
    /**
     * Link points to a DOI URL
     */
    DOI,
    /**
     * Custom URL without a Provider
     */
    URL
}