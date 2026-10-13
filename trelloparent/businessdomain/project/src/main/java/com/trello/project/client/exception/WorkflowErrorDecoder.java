package com.trello.project.client.exception;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.trello.project.client.exception.dto.RemoteApiError;
import com.trello.project.exception.BusinessRuleException;

import feign.Response;
import feign.codec.ErrorDecoder;
import tools.jackson.databind.ObjectMapper;

@Component
// Decodificador de errores
public class WorkflowErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    public WorkflowErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        // Leer la respuesta estandarizada de Workflow.
        if (response.body() != null) {
            try (var inputStream = response.body().asInputStream()) {

                RemoteApiError error = objectMapper.readValue(
                        inputStream,
                        RemoteApiError.class);

                // Identificar el error mediante su tipo estable (campo type)
                if ("/errors/board-accesses/already-exists"
                        .equals(error.type())) {
                    return new RemoteBoardAccessAlreadyExistsException();
                }

                if ("/errors/board-accesses-not-found"
                        .equals(error.type())) {
                    return new RemoteBoardAccessNotFoundException();
                }

            } catch (IOException e) {
                // Si no se puede interpretar el cuerpo,
                // utilizar el comportamiento predeterminado de Feign.
                return new BusinessRuleException();
            }
        }

        // Mantener el comportamiento normal para otros errores HTTP.
        return defaultDecoder.decode(methodKey, response);
    }

}
