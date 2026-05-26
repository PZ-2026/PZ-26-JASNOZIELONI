package pl.edu.ur.coopspace_backend.entity;

/**
 * Payment states for a charge.
 */
public enum ChargeStatus {
    /** Charge has not been paid at all. */
    UNPAID,
    /** Charge has been paid only partially. */
    PARTIALLY_PAID,
    /** Charge has been fully paid. */
    PAID
}
