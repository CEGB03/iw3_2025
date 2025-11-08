package ar.edu.iua.iw3.model.persistence;

import ar.edu.iua.iw3.model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

    Optional<Truck> findByLicensePlate(String licensePlate);

    Optional<Truck> findByLicensePlateAndIdNot(String licensePlate, Long id);
}