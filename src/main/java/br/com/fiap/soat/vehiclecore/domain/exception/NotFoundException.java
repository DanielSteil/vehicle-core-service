package br.com.fiap.soat.vehiclecore.domain.exception;

/** O recurso solicitado não existe. */
public abstract class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }
}
