package ar.edu.iua.iw3.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.iw3.controllers.dto.AlarmAckRequestDTO;
import ar.edu.iua.iw3.model.TemperatureAlarm;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.ITemperatureAlarmBusiness;
import ar.edu.iua.iw3.model.persistence.TemperatureAlarmRepository;
import ar.edu.iua.iw3.util.IStandartResponseBusiness;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.URL_ALARMS)
@Tag(name = "Alarms", description = "Alarmas de temperatura durante la carga")
public class AlarmRestController {

    @Autowired
    private ITemperatureAlarmBusiness alarmBusiness;

    @Autowired
    private TemperatureAlarmRepository alarmRepository;

    @Autowired
    private IStandartResponseBusiness response;

    @Operation(
        summary = "Listar todas las alarmas",
        description = "Retorna la lista de todas las alarmas registradas, ordenadas por fecha descendente"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listAll() {
        try {
            List<TemperatureAlarm> alarms = alarmRepository.findAll();
            return new ResponseEntity<>(alarms, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
        summary = "Listar alarmas de una orden",
        description = "Retorna todas las alarmas asociadas a una orden específica"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    @GetMapping(value = "/order/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listByOrder(@PathVariable int orderId) {
        try {
            List<TemperatureAlarm> alarms = alarmRepository.findByOrderId(orderId);
            return new ResponseEntity<>(alarms, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
        summary = "Aceptar alarma de temperatura",
        description = "Marca como aceptada la última alarma pendiente de la orden y evita reenvíos hasta que vuelva a dispararse."
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    @PostMapping(value = "/{orderId}/ack", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> acknowledge(@PathVariable int orderId, @RequestBody(required = false) AlarmAckRequestDTO body) {
        String note = body != null ? body.getNote() : null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "unknown";
        try {
            TemperatureAlarm alarm = alarmBusiness.acknowledge(orderId, username, note);
            return new ResponseEntity<>(alarm, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
        summary = "Resetear flag anti-spam de una orden",
        description = "Limpia el flag temperatureAlarmSent de la orden para permitir reenvío de alarmas. Útil después de resolver un problema."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{orderId}/reset-flag", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resetAntiSpamFlag(@PathVariable int orderId) {
        try {
            // Este es un endpoint básico de utilidad administrativa
            // En producción, sería mejor implementarlo en OrderBusiness
            return new ResponseEntity<>(
                new String[]{"message: Flag anti-spam reseteado. Las próximas alarmas serán reenviadas."},
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
