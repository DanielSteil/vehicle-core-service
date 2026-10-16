package br.com.fiap.soat.vehiclecore.domain.exception;

/** A operação é válida, mas conflita com o estado atual do recurso. */
public abstract class ConflictException extends DomainException {

    protected ConflictException(String message) {
        super(message);
    }
}
