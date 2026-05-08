package fr.traqueur.crates.api.models.crates;

import org.jetbrains.annotations.Nullable;

/**
 * Result of attempting to open a crate.
 * @param status The status of the open attempt.
 * @param failedCondition The condition that failed, if any.
 */
public record OpenResult(Status status, @Nullable Condition failedCondition) {

    /**
     * Possible statuses for crate opening attempts.
     */
    public enum Status {
        /**
         * The crate was opened successfully.
         */
        SUCCESS,
        /**
         * The player does not have the required key to open the crate.
         */
        NO_KEY,
        /**
         * A condition required to open the crate was not met.
         */
        CONDITION_FAILED,
        /**
         * The crate opening was cancelled by an event.
         */
        EVENT_CANCELLED,

        /**
         * The crate is already being opened by the player.
         */
        ALREADY_OPENING,
    }

    /**
     * Creates a successful OpenResult.
     * @return An OpenResult indicating success.
     */
    public static OpenResult success() {
        return new OpenResult(Status.SUCCESS, null);
    }

    /**
     * Creates an OpenResult indicating the player lacks the required key.
     * @return An OpenResult indicating no key.
     */
    public static OpenResult noKey() {
        return new OpenResult(Status.NO_KEY, null);
    }

    /**
     * Creates an OpenResult indicating a condition failed.
     * @param condition The condition that failed.
     * @return An OpenResult indicating a condition failure.
     */
    public static OpenResult conditionFailed(Condition condition) {
        return new OpenResult(Status.CONDITION_FAILED, condition);
    }

    public static OpenResult alreadyOpening() {
        return new OpenResult(Status.ALREADY_OPENING, null);
    }

    /**
     * Creates an OpenResult indicating the event was cancelled.
     * @return An OpenResult indicating event cancellation.
     */
    public static OpenResult eventCancelled() {
        return new OpenResult(Status.EVENT_CANCELLED, null);
    }

    /**
     * Checks if the open attempt resulted in an error.
     * @return True if there was an error, false if successful.
     */
    public boolean isError() {
        return status != Status.SUCCESS;
    }
}