package br.com.fiap.soat.vehiclecore.application.exception;

/** O serviço de venda não respondeu (fora do ar, timeout ou erro 5xx) mesmo após as novas tentativas. */
public class SalesServiceUnavailableException extends RuntimeException {

    public SalesServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
