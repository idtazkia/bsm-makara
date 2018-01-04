package id.ac.tazkia.payment.bsm.makara.bsmmakara.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity @Data
public class ReversalRequest {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private LocalDateTime transactionTime;

    @NotNull @NotEmpty
    private String accountNumber;

    @NotNull @NotEmpty
    private String message;
}
