package com.chalmers.atas.common;

import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransactionalResult<T> extends Result<T> {

    private TransactionalResult(boolean success, T data, Error error) {
        super(success, data, error);
    }

    public static <T> TransactionalResult<T> ok(T data) {
        return new TransactionalResult<>(true, data, null);
    }

    public static TransactionalResult<Void> ok() {
        return new TransactionalResult<>(true, null, null);
    }

    @RequiresTransaction
    public static <T> TransactionalResult<T> rollbackFor(Error error) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport
                    .currentTransactionStatus()
                    .setRollbackOnly();
        }

        return new TransactionalResult<>(false, null, error);
    }

    @RequiresTransaction
    public static <T> TransactionalResult<T> rollbackFor(ErrorCode errorCode) {
        return rollbackFor(errorCode.toError());
    }

    @RequiresTransaction
    public static <T> TransactionalResult<T> rollbackFor(ErrorCode errorCode, String details) {
        return rollbackFor(errorCode.toError(details));
    }

    @RequiresTransaction
    public static <T> TransactionalResult<T> from(Result<T> result) {
        if (result.isSuccess()) {
            return TransactionalResult.ok(result.getData());
        }

        return TransactionalResult.rollbackFor(result.getError());
    }

    @Override
    @RequiresTransaction
    public <R> TransactionalResult<R> map(Function<T, R> mapper) {
        if (isSuccess()) {
            return TransactionalResult.ok(mapper.apply(getData()));
        }

        return TransactionalResult.rollbackFor(getError());
    }

    @Override
    @RequiresTransaction
    public <R> TransactionalResult<R> flatMap(Function<T, Result<R>> mapper) {
        if (isSuccess()) {
            return TransactionalResult.from(mapper.apply(getData()));
        }

        return TransactionalResult.rollbackFor(getError());
    }

    @Override
    @RequiresTransaction
    public TransactionalResult<Void> then(Consumer<? super T> action) {
        if (isSuccess()) {
            action.accept(getData());
            return TransactionalResult.ok();
        }

        return TransactionalResult.rollbackFor(getError());
    }

    @Override
    public TransactionalResult<T> peek(Consumer<? super T> action) {
        if (isSuccess()) {
            action.accept(getData());
        }

        return this;
    }

    @RequiresTransaction
    public static <T> TransactionalResult<T> ofOptional(Optional<T> maybeData, ErrorCode notFoundErrorCode) {
        if (maybeData.isPresent()) {
            return TransactionalResult.ok(maybeData.get());
        }

        return TransactionalResult.rollbackFor(notFoundErrorCode);
    }
}