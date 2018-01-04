package id.ac.tazkia.payment.bsm.makara.bsmmakara.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MakaraResponse {
    private String responseCode;
    private String responseMessage;
    private String action;
    private String nomorPembayaran;
    private String nomorInvoice;
    private BigDecimal nilai;
    private String nama;
    private String keterangan;
    private String kodeChannel;
    private String kodeBank;
    private String kodeTerminal;
    private String idTransaksi;
    private String tanggalTransaksi;
    private String tanggalTransaksiAsal;
}
