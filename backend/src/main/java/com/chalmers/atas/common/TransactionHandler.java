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

    public <T> TransactionalResult<T> rollbackFor(ErrorCode errorCode) {
        return TransactionalResult.rollbackFor(errorCode);
    }

    public <T> TransactionalResult<T> rollbackFor(ErrorCode errorCode, String details) {
        return TransactionalResult.rollbackFor(errorCode, details);
    }

    public <T> TransactionalResult<T> ok(T data) {
        return TransactionalResult.ok(data);
    }

    public TransactionalResult<Void> ok() {
        return TransactionalResult.ok();
    }
}