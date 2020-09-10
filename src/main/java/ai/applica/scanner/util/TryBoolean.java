package ai.applica.scanner.util;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public class TryBoolean {

    private final Throwable exception;
    private final boolean value;

    TryBoolean(boolean value) {
        this.exception = null;
        this.value = value;
    }

    TryBoolean(Throwable exception) {
        this.exception = exception;
        this.value = false;
    }

    public static TryBoolean of(BooleanSupplier supplier) {
        try {
            boolean value = supplier.getAsBoolean();
            return new TryBoolean(value);
        } catch (Exception e) {
            return new TryBoolean(e);
        }
    }

    /**
     * Returns value of this <code>TryBoolean</code> or alternative if exception is present.
     * @param alternative Alternative value to be returned if exception occurred.
     */
    public boolean getOrDefault(boolean alternative) {
        if (Objects.isNull(exception))
            return alternative;
        return value;
    }

    /**
     * Returns exception associated with this <code>TryBoolean</code> or <code>null</code> if there is none.
     * @return The exception
     */
    public Throwable getException() {
        return exception;
    }

    public boolean isSuccess() {
        return Objects.isNull(exception);
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public static TryBoolean success() {
        return new TryBoolean(true);
    }

    public <V> Try<V> mapError() {
        return Try.ofError(getException());
    }

}
