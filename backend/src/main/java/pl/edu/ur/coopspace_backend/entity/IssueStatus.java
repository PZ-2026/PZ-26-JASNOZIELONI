package pl.edu.ur.coopspace_backend.entity;

/**
 * Lifecycle states for an issue.
 */
public enum IssueStatus {
    /** Newly created issue awaiting work. */
    OPEN,
    /** Issue is currently being handled. */
    IN_PROGRESS,
    /** Issue has been fully resolved and closed. */
    CLOSED
}
