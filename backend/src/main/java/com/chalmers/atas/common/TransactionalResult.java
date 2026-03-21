package com.chalmers.atas.common;

import org.springframework.transaction.interceptor.TransactionAspectSupport;

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

    public static <T> TransactionalResult<T> rollbackFor(Error error) {
        TransactionAspectSupport
                .currentTransactionStatus()
                .setRollbackOnly();
        return new TransactionalResult<>(false, null, error);
    }

    public static <T> TransactionalResult<T> from(Result<T> result) {
        if (result.isSuccess()) {
            return TransactionalResult.ok(result.getData());
        }
        return TransactionalResult.rollbackFor(result.getError());
    }

    @Override
    public <R> TransactionalResult<R> map(Function<T, R> mapper) {
        if (isSuccess()) {
            return TransactionalResult.ok(mapper.apply(getData()));
        }
        return TransactionalResult.rollbackFor(getError());
    }

    @Override
    public <R> TransactionalResult<R> flatMap(Function<T, Result<R>> mapper) {
        if (isSuccess()) {
            return TransactionalResult.from(mapper.apply(getData()));
        }
        return TransactionalResult.rollbackFor(getError());
    }

    @Override
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
}
