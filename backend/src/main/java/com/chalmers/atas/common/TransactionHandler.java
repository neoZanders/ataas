package com.chalmers.atas.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class TransactionHandler {

    @Transactional
    public <T> Result<T> executeInTransaction(Supplier<TransactionalResult<T>> action) {
        return action.get();
    }
}
