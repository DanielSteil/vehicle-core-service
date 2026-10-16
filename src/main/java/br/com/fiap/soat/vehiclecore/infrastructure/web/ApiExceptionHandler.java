package br.com.fiap.soat.vehiclecore.infrastructure.web;

import br.com.fiap.soat.vehiclecore.application.exception.SalesServiceIntegrationException;
import br.com.fiap.soat.vehiclecore.application.exception.SalesServiceUnavailableException;
import br.com.fiap.soat.vehiclecore.domain.exception.BusinessRuleException;
import br.com.fiap.soat.vehiclecore.domain.exception.ConflictException;
import br.com.fiap.soat.vehiclecore.domain.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/** Traduz as exceções para respostas no formato RFC 9457 (application/problem+json). */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Conflito com o estado atual", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ProblemDetail handleConcurrentChange(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Alteração concorrente",
                "O recurso foi alterado por outra requisição. Consulte o estado atual e tente novamente.");
    }

    @ExceptionHandler(SalesServiceUnavailableException.class)
    ProblemDetail handleSalesServiceUnavailable(SalesServiceUnavailableException ex) {
        log.error("Serviço de venda indisponível", ex);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Serviço de venda indisponível",
                "Não foi possível publicar o veículo no serviço de venda. Nada foi gravado; tente novamente.");
    }

    @ExceptionHandler(SalesServiceIntegrationException.class)
    ProblemDetail handleSalesServiceIntegration(SalesServiceIntegrationException ex) {
        log.error("Falha de integração com o serviço de venda", ex);
        return problem(HttpStatus.BAD_GATEWAY, "Falha de integração com o serviço de venda", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "Um ou mais campos são inválidos");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
