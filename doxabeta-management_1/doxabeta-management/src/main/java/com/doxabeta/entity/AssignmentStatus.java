package com.doxabeta.entity;

/**
 * Lifecycle of a student {@link Assignment} submission.
 */
public enum AssignmentStatus {
    /** Student has submitted the work; no grade recorded yet. This is the default on creation. */
    SUBMITTED,
    /** A mentor/admin has graded the submission (see AssignmentService.grade). */
    GRADED
}
