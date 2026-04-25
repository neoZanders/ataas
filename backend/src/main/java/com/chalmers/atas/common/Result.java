package com.chalmers.atas.common;

import lombok.Data;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
public class Result<T> {
    private final boolean success;
    private final T data;
    private final Error error;

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> ok() {
        return new Result<>(true, null, null);
    }

    public static <T> Result<T> error(Error error) {
        return new Result<>(false, null, error);
    }

    public static <T> Result<T> ofOptional(Optional<T> maybeData, Error notFoundError) {
        if (maybeData.isPresent()) {
            return new Result<>(true, maybeData.get(), null);
        } else {
            return new Result<>(false, null, notFoundError);
        }
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (success) return Result.ok(mapper.apply(data));
        else return Result.error(error);
    }

    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (isSuccess()) {
            return mapper.apply(data);
        } else {
            return Result.error(error);
        }
    }

    public void ifSuccess(Consumer<? super T> action) {
        if (success) {
            action.accept(data);
        }
    }

    public Result<Void> then(Consumer<? super T> action) {
        if (success) {
            action.accept(data);
            return Result.ok();
        } else {
            return Result.error(error);
        }
    }

    public <U> Result<U> then(Supplier<Result<U>> next) {
        if (success) {
            return next.get();
        } else {
            return Result.error(error);
        }
    }

    public Result<T> peek(Consumer<? super T> action) {
        if (success) {
            action.accept(data);
        }
        return this;
    }

    public T orError() {
        if (success) {
            return data;
        } else {
            throw new RuntimeException(error.getMessage());
        }
    }

    public T orGet(T fallback) {
        return success ? data : fallback;
    }

    public Result<T> orGetIfError(Error error, Supplier<? extends Result<T>> supplier) {
        if (success) {
            return this;
        } else {
            return this.error.getCode().equals(error.getCode()) ? supplier.get() : this;
        }
    }

    public Result<T> orGet(Supplier<? extends Result<T>> supplier) {
        if (success) {
            return this;
        } else {
            return supplier.get();
        }
    }
}
