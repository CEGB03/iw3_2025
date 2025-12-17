package ar.edu.iua.iw3.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Temperature_Alarms")
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TemperatureAlarm {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties("details")
    private Order order;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "threshold")
    private Double threshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private TemperatureAlarmState state = TemperatureAlarmState.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ack_user")
    private String ackUser;

    @Column(name = "ack_note", length = 500)
    private String ackNote;

    @Column(name = "ack_at")
    private LocalDateTime ackAt;
}
