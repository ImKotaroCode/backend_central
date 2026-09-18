package backend_central.backend_central.service;

import backend_central.backend_central.entity.Institucion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Best-effort: un fallo de correo no debe tumbar el checkout, la institucion ya quedo creada.
    public void enviarBienvenida(Institucion inst, String contactoNombre, String loginUrl) {
        if (from == null || from.isBlank()) {
            log.warn("SMTP no configurado, se omite correo de bienvenida para {}", inst.getEmailContacto());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(inst.getEmailContacto());
            message.setSubject("Bienvenido a KUI - " + inst.getNombre());
            message.setText(
                    "Hola " + contactoNombre + ",\n\n" +
                    "Tu institucion \"" + inst.getNombre() + "\" ya fue registrada en KUI con el Plan Rocket " +
                    "(6 meses de prueba gratis).\n\n" +
                    "Ingresa aqui para continuar: " + loginUrl + "\n\n" +
                    "Equipo KUI"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Correo de bienvenida fallido para {}: {}", inst.getEmailContacto(), e.getMessage());
        }
    }
}
