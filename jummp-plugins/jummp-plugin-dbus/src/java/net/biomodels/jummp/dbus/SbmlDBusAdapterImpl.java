package net.biomodels.jummp.dbus;

import grails.converters.JSON;
import net.biomodels.jummp.core.IModelService;
import net.biomodels.jummp.core.ISbmlService;
import net.biomodels.jummp.core.model.RevisionTransportCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @short Concrete Implementation of SbmlDBusAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class SbmlDBusAdapterImpl extends AbstractDBusAdapter implements SbmlDBusAdapter {
    /**
     * Dependency Injection for ModelService
     */
    private IModelService modelService;
    /**
     * Dependency Injection of SbmlService
     */
    private ISbmlService sbmlService;

    public long getVersion(String authenticationHash, long modelId, int revisionNumber) {
        try {
            setAuthentication(authenticationHash);
            return sbmlService.getVersion(modelService.getRevision(modelId, revisionNumber));
        } finally {
            restoreAuthentication();
        }
    }

    public long getLevel(String authenticationHash, long modelId, int revisionNumber) {
        try {
            setAuthentication(authenticationHash);
            return sbmlService.getLevel(modelService.getRevision(modelId, revisionNumber));
        } finally {
            restoreAuthentication();
        }
    }

    public String getModelNotes(String authenticationHash, long modelId, int revisionNumber) {
        try {
            setAuthentication(authenticationHash);
            return sbmlService.getNotes(modelService.getRevision(modelId, revisionNumber));
        } finally {
            restoreAuthentication();
        }
    }

    public String getModelMetaId(String authenticationHash, long modelId, int revisionNumber) {
        try {
            setAuthentication(authenticationHash);
            return sbmlService.getMetaId(modelService.getRevision(modelId, revisionNumber));
        } finally {
            restoreAuthentication();
        }
    }

    public String getModelAnnotations(String authenticationHash, long modelId, int revisionNumber) {
        try {
            setAuthentication(authenticationHash);
            JSON json = new JSON(sbmlService.getAnnotations(modelService.getRevision(modelId, revisionNumber)));
            return json.toString();
        } finally {
            restoreAuthentication();
        }
    }

    public String getParameters(String authenticationHash, long modelId, int revisionNumber) {
        try {
            setAuthentication(authenticationHash);
            JSON json = new JSON(sbmlService.getParameters(modelService.getRevision(modelId, revisionNumber)));
            return json.toString();
        } finally {
            restoreAuthentication();
        }
    }

    public boolean isRemote() {
        return false;
    }

    public void setModelService(IModelService modelService) {
        this.modelService = modelService;
    }

    public void setSbmlService(ISbmlService sbmlService) {
        this.sbmlService = sbmlService;
    }
}
