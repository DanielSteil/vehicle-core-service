package br.com.fiap.soat.vehiclecore.infrastructure.persistence;

import br.com.fiap.soat.vehiclecore.application.port.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class TransactionalUnitOfWork implements UnitOfWork {

    private final TransactionTemplate transactionTemplate;

    public TransactionalUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
