package ar.edu.iua.iw3.model.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.Driver;

@Repository
public interface OrderRepository extends JpaRepository  <Order, Integer> {

    Optional<Order> findById(int id);

    List<Order> findByDriver(Driver driver);

    //Optional<Orden> findByOrden(Orden orden);

    //Optional<Orden> findByOrdenAndIdNot(Orden orden, int id);
}
