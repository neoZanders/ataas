package com.chalmers.atas.common;

import lombok.Data;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public static <T> Result<T> ofOptional(Optional<T> maybeData) {
        if (maybeData.isPresent()) {
            return new Result<>(true, maybeData.get(), null);
        } else {
            return new Result<>(false, null, ErrorCode.NOT_FOUND.toError());
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

    public T orError() {
        if (success) {
            return data;
        } else {
            throw new RuntimeException(error.getMessage());
        }
    }

    public T orElse(T fallback) {
        return success ? data : fallback;
    }

    public Result<T> orErrorIf(boolean condition, Error error) {
        return condition ? Result.error(error) : this;
    }
}
