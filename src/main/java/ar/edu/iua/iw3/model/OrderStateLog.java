package ar.edu.iua.iw3.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Order_State_Logs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderStateLog {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "from_state")
    private Integer fromState;

    @Column(name = "to_state")
    private Integer toState;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "actor")
    private String actor;

    @Column(name = "notes")
    private String notes;

    public OrderStateLog(Order order, Integer fromState, Integer toState, LocalDateTime changedAt, String actor, String notes) {
        this.order = order;
        this.fromState = fromState;
        this.toState = toState;
        this.changedAt = changedAt;
        this.actor = actor;
        this.notes = notes;
    }

}
