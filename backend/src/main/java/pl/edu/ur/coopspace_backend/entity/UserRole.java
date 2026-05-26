package pl.edu.ur.coopspace_backend.entity;

/**
 * Available roles for application users.
 */
public enum UserRole {
    /**
     * Resident with standard user permissions.
     */
    RESIDENT,

    /**
     * Administrator with full system permissions.
     */
    ADMIN,

    /**
     * Maintenance staff responsible for handling issues.
     */
    MAINTAINER
}
