package id.ac.tazkia.payment.bsm.makara.bsmmakara.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MakaraResponse {
    private String responseCode;
    private String responseMessage;
    private String action;
    private String nomorPembayaran;
    private String nomorInvoice;
    private String nomorReferensi;
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
