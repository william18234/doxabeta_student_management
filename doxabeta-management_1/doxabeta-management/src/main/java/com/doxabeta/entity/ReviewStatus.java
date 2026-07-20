package com.doxabeta.entity;

/**
 * Publication state of a mentor {@link Review}.
 *
 * Lets a mentor save a review they're still writing (DRAFT) before making it
 * visible/final (PUBLISHED). The API defaults new reviews to DRAFT unless the
 * caller explicitly sets status to PUBLISHED (see ReviewService.create).
 */
public enum ReviewStatus {
    /** Review has been saved but is not yet finalized. */
    DRAFT,
    /** Review is finalized and visible as the official record. */
    PUBLISHED
}
