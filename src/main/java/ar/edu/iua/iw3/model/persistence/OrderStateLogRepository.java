package ar.edu.iua.iw3.model.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.iua.iw3.model.OrderStateLog;

public interface OrderStateLogRepository extends JpaRepository<OrderStateLog, Integer> {

}
