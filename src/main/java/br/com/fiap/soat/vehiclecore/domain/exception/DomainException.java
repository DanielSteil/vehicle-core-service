package br.com.fiap.soat.vehiclecore.domain.exception;

/** Base de todas as exceções de regra de negócio do serviço. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
