package com.doxabeta.entity;

/**
 * Lifecycle states for a {@link Student} in the program.
 *
 * Stored in the database as a plain string (see @Enumerated(EnumType.STRING) on
 * Student.status) rather than an ordinal number, so the column stays human-readable
 * and safe to reorder in code later without corrupting existing rows.
 */
public enum StudentStatus {
    /** Currently enrolled and active in the program. This is the default for new students. */
    ACTIVE,
    /** Temporarily not participating (e.g. on leave), but expected to return. */
    PAUSED,
    /** Finished the program successfully. */
    COMPLETED,
    /** Left the program before completion. */
    WITHDRAWN
}
