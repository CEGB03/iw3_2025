package ar.edu.iua.iw3.model.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.iua.iw3.model.Orden;

public interface OrdenRepository extends JpaRepository  <Orden, Integer> {

    Optional<Orden> findById(int id);

    Optional<Orden> findByOrden(Orden orden);

    Optional<Orden> findByOrdenAndIdNot(Orden orden, int id);
}
