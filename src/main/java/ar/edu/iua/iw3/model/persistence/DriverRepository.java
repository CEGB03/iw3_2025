package ar.edu.iua.iw3.model.persistence;

import ar.edu.iua.iw3.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {

    Optional<Driver> findByDni(long document);

    Optional<Driver> findByDniAndIdNot(long document, String id);
}