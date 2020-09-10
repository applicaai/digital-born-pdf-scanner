package ai.applica.scanner.util;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.*;

import static java.util.Objects.isNull;

public abstract class Try<W> {

    public static <T> Try<T> of(Supplier<T> closure) {
        try {
            return new Success<>(closure.get());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static <T> Try<T> ofValue(T value) {
        if (isNull(value))
            return new Failure<>(new NullPointerException("Value passed to Try.ofValue is null."));
        return new Success<>(value);
    }

    public static <T> Try<T> ofError(Throwable exception) {
        return new Failure<>(exception);
    }

    public <X> Try<X> map(Function<W, X> mapper) {
        if (isSuccess()) {
            try {
                W maybeValue = get();
                if (isNull(maybeValue))
                    return new Failure<>(new NullPointerException("Expected value not be null."));
                X maybeResult = mapper.apply(maybeValue);
                if (isNull(maybeResult))
                    return new Failure<>(new NullPointerException("Expected non-null result from mapping function."));
                return new Success<>(maybeResult);
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
        else {
            return new Failure<>(getException());
        }
    }

    public void accept(Consumer<W> onSuccess, Consumer<Throwable> onFailure) {
        if (isSuccess())
            onSuccess.accept(get());
        else
            onFailure.accept(getException());
    }

    public abstract boolean isSuccess();
    public abstract boolean isFailure();

    public abstract W get();
    public abstract Throwable getException();

    public abstract Optional<W> toOptional();

    public abstract OptionalInt mapToInt(ToIntFunction<W> mapper);

    public abstract TryBoolean mapToBool(Predicate<W> mapper);

    public <E> Try<E> mapError() {
        if (isFailure())
            //noinspection unchecked
            return (Try<E>) this;
        else
            throw new IllegalStateException("This instance has no error.");
    }

}

class Success<V> extends Try<V> {

    private final V value;

    Success(V value) {
        this.value = value;
    }

    public V get() {
        return value;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public Optional<V> toOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public OptionalInt mapToInt(ToIntFunction<V> mapper) {
        try {
            return OptionalInt.of(mapper.applyAsInt(value));
        } catch (Exception e) {
            return OptionalInt.empty();
        }
    }

    @Override
    public TryBoolean mapToBool(Predicate<V> predicate) {
        return TryBoolean.of(() -> predicate.test(value));
    }

}

class Failure<V> extends Try<V> {

    private final Throwable throwable;

    Failure(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public V get() {
        throw new IllegalStateException("Failure does not have value");
    }

    @Override
    public Throwable getException() {
        return throwable;
    }

    @Override
    public Optional<V> toOptional() {
        return Optional.empty();
    }

    @Override
    public OptionalInt mapToInt(ToIntFunction<V> mapper) {
        return OptionalInt.empty();
    }

    @Override
    public TryBoolean mapToBool(Predicate<V> mapper) {
        return new TryBoolean(getException());
    }

}
