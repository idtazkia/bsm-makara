package id.ac.tazkia.payment.bsm.makara.bsmmakara.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MakaraRequest {
    public static final String ACTION_INQUIRY = "inquiry";
    public static final String ACTION_PAYMENT = "payment";
    public static final String ACTION_REVERSAL = "reversal";

    @NotNull @NotEmpty
    private String action;
    @NotNull @NotEmpty
    private String nomorPembayaran;
    private String nomorInvoice;
    private String kodeChannel;
    private String kodeBank;
    private String kodeTerminal;
    private String idTransaksi;
    private BigDecimal nilai;
    private String tanggalTransaksi;
    private String tanggalTransaksiAsal;
    private String nomorJurnalPembukuan;
}
