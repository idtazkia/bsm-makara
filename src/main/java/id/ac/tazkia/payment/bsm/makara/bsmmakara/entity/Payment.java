package id.ac.tazkia.payment.bsm.makara.bsmmakara.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Data
public class Payment {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_virtual_account")
    private VirtualAccount virtualAccount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.ACCEPTED;

    @NotNull @Min(0)
    private BigDecimal amount;

    @NotNull @NotEmpty
    private String transactionReference;

    @NotNull
    private LocalDateTime transactionTime;
}
