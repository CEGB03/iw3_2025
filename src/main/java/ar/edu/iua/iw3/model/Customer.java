package ar.edu.iua.iw3.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Customers")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Customer {

    @Id
    //@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private String id;

    @Column
    private long socialNumber;

    @Column
    private long phoneNumber;

    @Column
    private String mail;
    
}
