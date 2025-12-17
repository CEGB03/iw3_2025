package ar.edu.iua.iw3.model.business.implementations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.TemperatureAlarm;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlarmNotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void notifyAlarm(TemperatureAlarm alarm, List<String> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("Alarma {} para orden {} sin destinatarios configurados", alarm.getId(), alarm.getOrder().getId());
            return;
        }

        if (mailSender == null) {
            log.warn("JavaMailSender no configurado. Simulando envío de alarma para orden {}", alarm.getOrder().getId());
            logSimulatedEmail(alarm, recipients);
            return;
        }

        for (String email : recipients) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("⚠️ ALERTA: Temperatura Crítica - Orden #" + alarm.getOrder().getId());
                message.setText(buildEmailBody(alarm));
                message.setFrom("noreply@iw3.local");

                mailSender.send(message);
                log.info("Email enviado a {} para alarma {}", email, alarm.getId());
            } catch (Exception e) {
                log.error("Error enviando email a {}: {}", email, e.getMessage(), e);
            }
        }
    }

    private String buildEmailBody(TemperatureAlarm alarm) {
        return String.format(
            "ALERTA DE TEMPERATURA CRÍTICA\n\n" +
            "Orden ID: %d\n" +
            "Temperatura Actual: %.2f°C\n" +
            "Umbral Configurado: %.2f°C\n" +
            "Fecha/Hora: %s\n\n" +
            "Por favor, revise inmediatamente la carga.\n\n" +
            "Sistema IW3 - Monitoreo de Temperatura",
            alarm.getOrder().getId(),
            alarm.getTemperature(),
            alarm.getThreshold(),
            alarm.getCreatedAt()
        );
    }

    private void logSimulatedEmail(TemperatureAlarm alarm, List<String> recipients) {
        log.info("[SIMULATED EMAIL] Orden={} Temp={} Threshold={} Destinatarios={}",
                alarm.getOrder().getId(), alarm.getTemperature(), alarm.getThreshold(), recipients);
    }
}
