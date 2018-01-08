package id.ac.tazkia.payment.bsm.makara.bsmmakara.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
public class PaymentReversal {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_payment")
    private Payment payment;

    @NotNull @NotEmpty
    private String transactionReference;

    @NotNull @NotEmpty
    private String clientReference;

    @NotNull
    private LocalDateTime transactionTime;
}
