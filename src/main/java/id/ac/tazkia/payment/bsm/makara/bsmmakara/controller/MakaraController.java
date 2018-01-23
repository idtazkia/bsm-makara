package id.ac.tazkia.payment.bsm.makara.bsmmakara.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.constants.ResponseCodeConstants;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dao.PaymentDao;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dao.PaymentReversalDao;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dao.VirtualAccountDao;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.MakaraRequest;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.MakaraResponse;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.*;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.service.KafkaSenderService;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.utils.ChecksumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController @Transactional
@RequestMapping("/bsm")
public class MakaraController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MakaraController.class);

    @Value("${shared.key}") private String sharedKey;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private VirtualAccountDao virtualAccountDao;
    @Autowired private PaymentDao paymentDao;
    @Autowired private PaymentReversalDao paymentReversalDao;
    @Autowired private KafkaSenderService kafkaSenderService;

    @PostMapping("/api")
    public MakaraResponse handleRequest(@RequestBody @Valid MakaraRequest request) {
        try {
            LOGGER.debug("Request : {}", objectMapper.writeValueAsString(request));

            String checksum = request.getChecksum();
            if (!StringUtils.hasText(checksum)) {
                return errorResponse(ResponseCodeConstants.INVALID_REQUEST_FORMAT, "Checksum harus diisi");
            }

            String verifikasiChecksum = ChecksumUtils.calculateChecksum(request.getNomorPembayaran(), sharedKey, request.getTanggalTransaksi());
            if (!verifikasiChecksum.equalsIgnoreCase(checksum)) {
                return errorResponse(ResponseCodeConstants.INVALID_CHECKSUM, "Invalid Checksum");
            }

            MakaraResponse response = errorResponse(ResponseCodeConstants.INVALID_ACTION, "Action " + request.getAction() + " tidak dikenal");

            if (MakaraRequest.ACTION_INQUIRY.equals(request.getAction())) {
                response = handleInquiry(request);
            }

            if (MakaraRequest.ACTION_PAYMENT.equals(request.getAction())) {
                response = handlePayment(request);
            }

            if (MakaraRequest.ACTION_REVERSAL.equals(request.getAction())) {
                response = handleReversal(request);
            }

            LOGGER.debug("Response : {}", objectMapper.writeValueAsString(response));
            return response;
        } catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
            return errorResponse(ResponseCodeConstants.ERROR_OTHERS, "Error memproses request");
        }
    }

    private MakaraResponse handleReversal(@Valid MakaraRequest request) {
        String nomorInvoice = request.getNomorInvoice();
        if (!StringUtils.hasText(nomorInvoice)) {
            return errorResponse(ResponseCodeConstants.INVALID_REQUEST_FORMAT, "Nomor invoice " + nomorInvoice + " harus diisi");
        }

        String nomorPembayaran = request.getNomorPembayaran();
        if(!StringUtils.hasText(nomorPembayaran)){
            return errorResponse(ResponseCodeConstants.INVALID_REQUEST_FORMAT, "nomorPembayaran harus diisi");
        }

        String idTransaksi = request.getIdTransaksi();
        if(!StringUtils.hasText(idTransaksi)){
            return errorResponse(ResponseCodeConstants.INVALID_REQUEST_FORMAT, "idTransaksi harus diisi");
        }

        VirtualAccount va = virtualAccountDao.findByInvoiceNumberAndAccountStatusIn(nomorInvoice,
                AccountStatus.PAID, AccountStatus.PAID_PARTIALLY);
        if (va == null) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT,
                    "Nomor invoice " + nomorInvoice + " belum dibayar");
        }

        List<Payment> paymentList = paymentDao.findByClientReferenceAndVirtualAccount(idTransaksi, va);
        if (paymentList.isEmpty()) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT,
                    "Pembayaran dengan idTransaksi " + idTransaksi + " tidak ditemukan");
        }

        if (paymentList.size() > 1) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT,
                    "Pembayaran dengan idTransaksi " + idTransaksi + " duplikat. Hubungi admin");
        }

        Payment payment = paymentList.get(0);

        if (PaymentStatus.REVERSED.equals(payment.getPaymentStatus())) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT,
                    "Pembayaran dengan idTransaksi " + idTransaksi + " sudah dibatalkan");
        }

        if (payment.getAmount().compareTo(request.getNilai()) != 0) {
            return errorResponse(ResponseCodeConstants.INVALID_AMOUNT,
                    "Nilai pembayaran [" + payment.getAmount() + "] berbeda dengan nilai reversal [" + request.getNilai() +"]");
        }

        payment.setPaymentStatus(PaymentStatus.REVERSED);
        paymentDao.save(payment);

        PaymentReversal paymentReversal = new PaymentReversal();
        paymentReversal.setPayment(payment);
        paymentReversal.setTransactionTime(LocalDateTime.now());
        paymentReversal.setTransactionReference(UUID.randomUUID().toString());
        paymentReversal.setClientReference(idTransaksi);
        paymentReversalDao.save(paymentReversal);

        va.setCumulativePayment(va.getCumulativePayment().subtract(payment.getAmount()));
        if (va.getCumulativePayment().compareTo(BigDecimal.ZERO) > 0) {
            va.setAccountStatus(AccountStatus.PAID_PARTIALLY);
        } else {
            va.setAccountStatus(AccountStatus.UNPAID);
        }
        virtualAccountDao.save(va);

        MakaraResponse response = MakaraResponse.builder()
                .kodeBank(request.getKodeBank())
                .kodeChannel(request.getKodeChannel())
                .kodeTerminal(request.getKodeTerminal())
                .idTransaksi(request.getIdTransaksi())
                .tanggalTransaksi(request.getTanggalTransaksi())
                .responseCode(ResponseCodeConstants.SUCCESS)
                .responseMessage("OK")
                .nomorInvoice(va.getInvoiceNumber())
                .nomorPembayaran(va.getAccountNumber())
                .nama(va.getName())
                .keterangan(va.getDescription())
                .referensiPembayaran(payment.getTransactionReference())
                .referensiReversal(paymentReversal.getTransactionReference())
                .tanggalTransaksi(paymentReversal.getTransactionTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        return response;
    }

    private MakaraResponse handlePayment(@Valid MakaraRequest request) {
        String nomorPembayaran = request.getNomorPembayaran();

        if(!StringUtils.hasText(nomorPembayaran)){
            return errorResponse(ResponseCodeConstants.INVALID_REQUEST_FORMAT, "nomorPembayaran harus diisi");
        }

        List<VirtualAccount> virtualAccounts = virtualAccountDao
                .findByAccountNumberAndAccountStatusIn(nomorPembayaran,
                        AccountStatus.UNPAID, AccountStatus.PAID_PARTIALLY);

        if(virtualAccounts.isEmpty()){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " tidak ditemukan");
        }

        if(virtualAccounts.size() > 1){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " duplikat. Hubungi admin");
        }

        VirtualAccount va = virtualAccounts.get(0);

        if (AccountStatus.PAID.equals(va.getAccountStatus())) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " sudah lunas");
        }

        if(LocalDateTime.now().isAfter(va.getExpireDate().atStartOfDay())){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " sudah kadaluarsa");
        }

        BigDecimal amount = request.getNilai();

        if (AccountType.CLOSED.equals(va.getAccountType())) {
            if(va.getAmount().compareTo(amount) != 0){
                return errorResponse(ResponseCodeConstants.INVALID_AMOUNT, "Nilai pembayaran ["+amount+"] tidak sama dengan nilai tagihan ["+va.getAmount()+"]");
            }
        }

        if (AccountType.INSTALLMENT.equals(va.getAccountType())) {
            if (va.getAmount().compareTo(va.getCumulativePayment().add(amount)) < 0) {
                return errorResponse(ResponseCodeConstants.INVALID_AMOUNT,
                        "Nilai pembayaran ["
                                +va.getCumulativePayment().add(amount)+
                                "] melebihi nilai tagihan ["
                                +va.getAmount()+"]");
            }
        }

        if (AccountType.OPEN.equals(va.getAccountType())) {
            if (amount.compareTo(va.getAmount()) < 0) {
                return errorResponse(ResponseCodeConstants.INVALID_AMOUNT,
                        "Nilai pembayaran ["
                                + amount +
                                "] kurang dari nilai minimum ["
                                +va.getAmount()+"]");
            }
        }

        String idTransaksi = request.getIdTransaksi();
        if(!StringUtils.hasText(idTransaksi)){
            return errorResponse(ResponseCodeConstants.INVALID_REQUEST_FORMAT, "idTransaksi harus diisi");
        }

        List<Payment> paymentList = paymentDao.findByClientReferenceAndVirtualAccount(idTransaksi, va);
        if (!paymentList.isEmpty()) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "idTransaksi " + idTransaksi + " sudah pernah digunakan untuk nomorPembayaran "+nomorPembayaran);
        }

        Payment payment = new Payment();
        payment.setVirtualAccount(va);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.ACCEPTED);
        payment.setTransactionTime(LocalDateTime.now());
        payment.setTransactionReference(UUID.randomUUID().toString());
        payment.setClientReference(request.getIdTransaksi());
        paymentDao.save(payment);

        va.setCumulativePayment(va.getCumulativePayment().add(amount));
        va.setAccountStatus(AccountStatus.PAID_PARTIALLY);

        if (!AccountType.OPEN.equals(va.getAccountType())) {
            if (va.getCumulativePayment().compareTo(va.getAmount()) >= 0) {
                va.setAccountStatus(AccountStatus.PAID);
            }
        }

        virtualAccountDao.save(va);
        kafkaSenderService.sendVaPayment(payment);

        MakaraResponse response = MakaraResponse.builder()
                .kodeBank(request.getKodeBank())
                .kodeChannel(request.getKodeChannel())
                .kodeTerminal(request.getKodeTerminal())
                .idTransaksi(request.getIdTransaksi())
                .tanggalTransaksi(request.getTanggalTransaksi())
                .responseCode(ResponseCodeConstants.SUCCESS)
                .responseMessage("OK")
                .nomorInvoice(va.getInvoiceNumber())
                .nomorPembayaran(va.getAccountNumber())
                .jenisAkun(va.getAccountType().name())
                .nama(va.getName())
                .tagihanTotal(va.getAmount())
                .tagihanEfektif(va.getAmount().subtract(va.getCumulativePayment()))
                .akumulasiPembayaran(va.getCumulativePayment())
                .keterangan(va.getDescription())
                .referensiPembayaran(payment.getTransactionReference())
                .tanggalTransaksi(payment.getTransactionTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        // OPEN payment tidak perlu menampilkan akumulasi pembayaran dan sisa tagihan
        if (AccountType.OPEN.equals(va.getAccountType())) {
            response.setAkumulasiPembayaran(null);
            response.setTagihanEfektif(response.getTagihanTotal());
        }

        return response;
    }

    private MakaraResponse handleInquiry(@Valid MakaraRequest request) {

        String nomorPembayaran = request.getNomorPembayaran();

        if(!StringUtils.hasText(nomorPembayaran)){
            return errorResponse(ResponseCodeConstants.INVALID_REQUEST_FORMAT, "nomorPembayaran harus diisi");
        }

        List<VirtualAccount> virtualAccounts = virtualAccountDao.findByAccountNumberAndAccountStatusIn(nomorPembayaran,
                AccountStatus.UNPAID, AccountStatus.PAID_PARTIALLY);

        if(virtualAccounts.isEmpty()){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " tidak ditemukan");
        }

        if(virtualAccounts.size() > 1){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " duplikat. Hubungi admin");
        }

        VirtualAccount va = virtualAccounts.get(0);

        if (AccountStatus.PAID.equals(va.getAccountStatus())) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " sudah lunas");
        }

        if(LocalDateTime.now().isAfter(va.getExpireDate().atStartOfDay())){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " sudah kadaluarsa");
        }

        MakaraResponse response = MakaraResponse.builder()
                .kodeBank(request.getKodeBank())
                .kodeChannel(request.getKodeChannel())
                .kodeTerminal(request.getKodeTerminal())
                .idTransaksi(request.getIdTransaksi())
                .tanggalTransaksi(request.getTanggalTransaksi())
                .responseCode(ResponseCodeConstants.SUCCESS)
                .responseMessage("OK")
                .nama(va.getName())
                .nomorInvoice(va.getInvoiceNumber())
                .nomorPembayaran(va.getAccountNumber())
                .jenisAkun(va.getAccountType().name())
                .tagihanTotal(va.getAmount())
                .tagihanEfektif(va.getAmount().subtract(va.getCumulativePayment()))
                .akumulasiPembayaran(va.getCumulativePayment())
                .keterangan(va.getDescription())
                .build();

        // OPEN payment tidak perlu menampilkan akumulasi pembayaran dan sisa tagihan
        if (AccountType.OPEN.equals(va.getAccountType())) {
            response.setAkumulasiPembayaran(null);
            response.setTagihanEfektif(response.getTagihanTotal());
        }

        return response;
    }

    private MakaraResponse errorResponse(String code, String message){
        return MakaraResponse.builder()
                .responseCode(code)
                .responseMessage(message)
                .build();
    }
}
