package br.com.fiap.soat.vehiclecore.application.port;

import java.util.function.Supplier;

/** Executa o trabalho numa única transação: ou tudo é gravado, ou nada é. */
public interface UnitOfWork {

    <T> T execute(Supplier<T> work);
}
