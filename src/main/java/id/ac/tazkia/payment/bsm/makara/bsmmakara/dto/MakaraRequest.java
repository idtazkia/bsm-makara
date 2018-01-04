package id.ac.tazkia.payment.bsm.makara.bsmmakara.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MakaraRequest {
    public static final String ACTION_INQUIRY = "inquiry";
    public static final String ACTION_PAYMENT = "payment";
    public static final String ACTION_REVERSAL = "reversal";

    private String action;
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
