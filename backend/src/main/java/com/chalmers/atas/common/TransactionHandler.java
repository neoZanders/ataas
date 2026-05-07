package com.chalmers.atas.common;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TransactionHandler {

    @Transactional
    public <T> Result<T> executeInTransaction(Supplier<TransactionalResult<T>> action) {
        return Result.from(action.get());
    }

    public <T> TransactionalResult<T> rollbackFor(Error error) {
        return TransactionalResult.rollbackFor(error);
    }

    public <T> TransactionalResult<T> ok(T data) {
        return TransactionalResult.ok(data);
    }

    public TransactionalResult<Void> ok() {
        return TransactionalResult.ok();
    }
}