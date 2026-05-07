package com.chalmers.atas.arch;

import com.chalmers.atas.common.RequiresTransaction;
import com.chalmers.atas.common.TransactionalResult;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.fail;

public class TransactionalResultArchTest {

    @Test
    void transactionalResultMethodsShouldOnlyBeCalledFromTransactionalMethodsOrTransactionHandler() {
        JavaClasses classes = new ClassFileImporter()
                .importPackages("com.chalmers.atas");

        StringBuilder violations = new StringBuilder();

        for (var javaClass : classes) {
            for (JavaMethodCall call : javaClass.getMethodCallsFromSelf()) {
                if (!isCallToRequiresTransactionMethod(call)) {
                    continue;
                }

                if (isAllowed(call)) {
                    continue;
                }

                violations.append("\n")
                        .append(call.getOrigin().getFullName())
                        .append(" calls ")
                        .append(call.getTarget().getFullName())
                        .append(" outside a transaction boundary.");
            }
        }

        if (!violations.isEmpty()) {
            fail("TransactionalResult transaction boundary violations:" + violations);
        }
    }

    private boolean isCallToRequiresTransactionMethod(JavaMethodCall call) {
        if (!call.getTargetOwner().isAssignableTo(TransactionalResult.class)) {
            return false;
        }

        return call.getTarget()
                .resolveMember()
                .map(method -> method.isAnnotatedWith(RequiresTransaction.class))
                .orElse(false);
    }

    private boolean isAllowed(JavaMethodCall call) {
        return isInsideTransactionalMethod(call)
                || isInsideTransactionalClass(call)
                || isInsideTransactionHandler(call)
                || isInsideTransactionalResultItself(call);
    }

    private boolean isInsideTransactionalMethod(JavaMethodCall call) {
        JavaCodeUnit origin = call.getOrigin();
        return origin.isAnnotatedWith(Transactional.class);
    }

    private boolean isInsideTransactionalClass(JavaMethodCall call) {
        return call.getOriginOwner().isAnnotatedWith(Transactional.class);
    }

    private boolean isInsideTransactionHandler(JavaMethodCall call) {
        return call.getOriginOwner().getSimpleName().equals("TransactionHandler");
    }

    private boolean isInsideTransactionalResultItself(JavaMethodCall call) {
        return call.getOriginOwner().isAssignableTo(TransactionalResult.class);
    }
}
