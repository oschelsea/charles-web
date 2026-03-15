package com.xenon.core.exception;

/**
 * Exception thrown when trying to create a resource that already exists.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceAlreadyExistsException(String resourceType, String resourceId) {
        super(String.format("%s already exists: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
