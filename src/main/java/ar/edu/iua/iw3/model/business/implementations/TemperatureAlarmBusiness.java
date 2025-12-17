package ar.edu.iua.iw3.model.business.implementations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.TemperatureAlarm;
import ar.edu.iua.iw3.model.TemperatureAlarmState;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.ITemperatureAlarmBusiness;
import ar.edu.iua.iw3.model.persistence.TemperatureAlarmRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TemperatureAlarmBusiness implements ITemperatureAlarmBusiness {

    @Value("${alarm.temperature.threshold:40.0}")
    private double threshold;

    @Value("${alarm.temperature.recipients:}")
    private String recipientsRaw;

    @Autowired
    private TemperatureAlarmRepository alarmRepository;

    @Autowired
    private AlarmNotificationService notificationService;

    @Override
    public TemperatureAlarm handle(Order order, Double temperature) throws BusinessException {
        if (order == null || temperature == null) {
            return null;
        }

        if (temperature <= threshold) {
            return null;
        }

        // Verificar si ya se envió una alarma para esta orden (anti-spam)
        if (order.getTemperatureAlarmSent() != null && order.getTemperatureAlarmSent()) {
            Optional<TemperatureAlarm> existing = alarmRepository
                    .findTopByOrderIdAndStateOrderByCreatedAtDesc(order.getId(), TemperatureAlarmState.PENDING);
            if (existing.isPresent()) {
                return existing.get(); // Retorna alarma existente
            }
        }

        try {
            Optional<TemperatureAlarm> pending = alarmRepository
                    .findTopByOrderIdAndStateOrderByCreatedAtDesc(order.getId(), TemperatureAlarmState.PENDING);

            if (pending.isPresent()) {
                return pending.get();
            }

            TemperatureAlarm alarm = new TemperatureAlarm();
            alarm.setOrder(order);
            alarm.setTemperature(temperature);
            alarm.setThreshold(threshold);
            alarm.setState(TemperatureAlarmState.PENDING);
            alarm.setCreatedAt(LocalDateTime.now());

            TemperatureAlarm saved = alarmRepository.save(alarm);

            try {
                List<String> recipients = getRecipients();
                notificationService.notifyAlarm(saved, recipients);
                // Marcar flag anti-spam en Order para evitar reenvíos
                order.setTemperatureAlarmSent(true);
            } catch (Exception notifyEx) {
                log.warn("No se pudo notificar alarma {}: {}", saved.getId(), notifyEx.getMessage());
            }

            return saved;
        } catch (Exception e) {
            log.error("Error manejando alarma de temperatura para orden {}: {}", order.getId(), e.getMessage());
            throw BusinessException.builder().ex(e).message("No se pudo procesar la alarma de temperatura").build();
        }
    }

    @Override
    public TemperatureAlarm acknowledge(int orderId, String username, String note)
            throws NotFoundException, BusinessException {
        try {
            TemperatureAlarm alarm = alarmRepository
                    .findTopByOrderIdAndStateOrderByCreatedAtDesc(orderId, TemperatureAlarmState.PENDING)
                    .orElseThrow(() -> NotFoundException.builder().message("No hay alarma pendiente para esta orden").build());

            alarm.setState(TemperatureAlarmState.ACKED);
            alarm.setAckUser(username);
            alarm.setAckNote(note);
            alarm.setAckAt(LocalDateTime.now());

            return alarmRepository.save(alarm);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error aceptando alarma para orden {}: {}", orderId, e.getMessage());
            throw BusinessException.builder().ex(e).message("No se pudo aceptar la alarma").build();
        }
    }

    private List<String> getRecipients() {
        if (recipientsRaw == null || recipientsRaw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(recipientsRaw.split("[;,]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
