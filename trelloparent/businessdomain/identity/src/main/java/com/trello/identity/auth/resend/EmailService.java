package com.trello.identity.auth.resend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.trello.identity.exception.BusinessRuleException;

@Service
public class EmailService {
    private final Resend resend;
    private final String fromEmail;

    public EmailService(
            Resend resend,
            @Value("${resend.test.domain}") String fromEmail) {

        this.resend = resend;
        this.fromEmail = fromEmail;
    }

    // Envía un correo electrónico mediante la API de Resend, se tienen los
    // parametros:
    // recipient: Dirección de correo electrónico del destinatario.
    // subject: Asunto del correo electrónico.
    // htmlContent: Contenido del correo en formato HTML.

    public String sendEmail(
            String recipient,
            String subject,
            String htmlContent) {

        try {

            // Construye los parámetros necesarios para realizar el envío.
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(recipient)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            // Ejecuta la solicitud de envío a través de la API de Resend.
            CreateEmailResponse response = resend.emails().send(params);

            // Devuelve el identificador asignado al correo por Resend.
            return response.getId();

            // Nota: recibir un identificador significa que Resend aceptó la solicitud, pero
            // no garantiza que el correo haya llegado a la bandeja de entrada.
        } catch (ResendException exception) {
            // Convierte la excepción del SDK en una excepción propia
            // de la aplicación para centralizar el manejo de errores.
            throw new BusinessRuleException(
                    "No se pudo enviar el correo electrónico",
                    exception);
        }
    }

}