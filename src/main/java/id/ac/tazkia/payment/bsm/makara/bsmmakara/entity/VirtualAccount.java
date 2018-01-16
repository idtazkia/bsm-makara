package id.ac.tazkia.payment.bsm.makara.bsmmakara.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Data
public class VirtualAccount {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.UNPAID;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountType accountType = AccountType.CLOSED;

    @NotNull
    @NotEmpty
    private String accountNumber;

    @NotNull
    @NotEmpty
    private String invoiceNumber;

    @NotNull @NotEmpty
    private String name;

    private String description;

    @Email
    private String email;

    private String phone;

    @NotNull @Min(0)
    private BigDecimal amount;

    @NotNull @Min(0)
    private BigDecimal cumulativePayment = BigDecimal.ZERO;

    @NotNull
    private LocalDateTime createTime;

    @NotNull @Column(columnDefinition = "DATE")
    private LocalDate expireDate;
}
