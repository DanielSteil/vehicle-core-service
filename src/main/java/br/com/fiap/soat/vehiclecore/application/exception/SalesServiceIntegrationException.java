package br.com.fiap.soat.vehiclecore.application.exception;

/**
 * O serviço de venda recusou a chamada por um motivo que repetir não resolve
 * (token inválido, contrato divergente). Indica erro de configuração ou de integração.
 */
public class SalesServiceIntegrationException extends RuntimeException {

    public SalesServiceIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
