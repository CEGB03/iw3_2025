package ar.edu.iua.iw3.model.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.iua.iw3.model.TemperatureAlarm;
import ar.edu.iua.iw3.model.TemperatureAlarmState;

@Repository
public interface TemperatureAlarmRepository extends JpaRepository<TemperatureAlarm, Integer> {

    Optional<TemperatureAlarm> findTopByOrderIdAndStateOrderByCreatedAtDesc(int orderId, TemperatureAlarmState state);

    List<TemperatureAlarm> findByOrderId(int orderId);
}
