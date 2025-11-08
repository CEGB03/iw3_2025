package ar.edu.iua.iw3.model.persistence;

import ar.edu.iua.iw3.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    public Optional<Customer> findBySocialNumber(long socialNumber);

    public Optional<Customer> findBySocialNumberAndIdNot(long socialNumber, String id);
}