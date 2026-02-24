package com.chalmers.atas.common;

import org.springframework.transaction.interceptor.TransactionAspectSupport;

public class TransactionalResult<T> extends Result<T> {
    private TransactionalResult(boolean success, T data, Error error) {
        super(success, data, error);
    }

    public static <T> TransactionalResult<T> ok(T data) {
        return new TransactionalResult<>(true, data, null);
    }

    public static <T> TransactionalResult<T> rollbackFor(Error error) {
        TransactionAspectSupport
                .currentTransactionStatus()
                .setRollbackOnly();
        return new TransactionalResult<>(false, null, error);
    }

}
