package id.ac.tazkia.payment.bsm.makara.bsmmakara.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VaResponse {
    private RequestType requestType;
    private RequestStatus requestStatus;
    private String accountNumber;
    private String invoiceNumber;
    private String name;
    private String bankId;
    private BigDecimal amount;
}
