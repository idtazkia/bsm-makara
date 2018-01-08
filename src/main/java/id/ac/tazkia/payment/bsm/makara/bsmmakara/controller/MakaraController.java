package id.ac.tazkia.payment.bsm.makara.bsmmakara.controller;

import id.ac.tazkia.payment.bsm.makara.bsmmakara.constants.ResponseCodeConstants;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dao.VirtualAccountDao;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.MakaraRequest;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.MakaraResponse;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.AccountStatus;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.VirtualAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bsm")
public class MakaraController {

    @Autowired private VirtualAccountDao virtualAccountDao;

    @PostMapping("/api")
    public MakaraResponse handleRequest(@RequestBody @Valid MakaraRequest request) {
        if(MakaraRequest.ACTION_INQUIRY.equals(request.getAction())){
            return handleInquiry(request);
        }

        if (MakaraRequest.ACTION_PAYMENT.equals(request.getAction())) {
            return handlePayment(request);
        }

        if (MakaraRequest.ACTION_REVERSAL.equals(request.getAction())) {
            return handleReversal(request);
        }

        return errorResponse(ResponseCodeConstants.INVALID_ACTION, "Action " + request.getAction() + " tidak dikenal");
    }

    private MakaraResponse handleReversal(@Valid MakaraRequest request) {
        MakaraResponse response = MakaraResponse.builder()
                .responseCode(ResponseCodeConstants.INVALID_ACTION)
                .responseMessage("Not implemented yet")
                .build();

        return response;
    }

    private MakaraResponse handlePayment(@Valid MakaraRequest request) {
        MakaraResponse response = MakaraResponse.builder()
                .responseCode(ResponseCodeConstants.INVALID_ACTION)
                .responseMessage("Not implemented yet")
                .build();

        return response;
    }

    private MakaraResponse handleInquiry(@Valid MakaraRequest request) {
        String nomorPembayaran = request.getNomorPembayaran();

        if(!StringUtils.hasText(nomorPembayaran)){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "nomorPembayaran harus diisi");
        }

        List<VirtualAccount> virtualAccounts = virtualAccountDao.findByAccountNumberAndAccountStatus(nomorPembayaran, AccountStatus.ACTIVE);

        if(virtualAccounts.isEmpty()){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " tidak ditemukan");
        }

        if(virtualAccounts.size() > 1){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " duplikat. Hubungi admin");
        }

        VirtualAccount va = virtualAccounts.get(0);

        if (!AccountStatus.ACTIVE.equals(va.getAccountStatus())) {
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " tidak aktif");
        }

        if(LocalDateTime.now().isAfter(va.getExpireDate().atStartOfDay())){
            return errorResponse(ResponseCodeConstants.INVALID_ACCOUNT, "Nomor Pembayaran " + nomorPembayaran + " sudah kadaluarsa");
        }

        MakaraResponse response = MakaraResponse.builder()
                .responseCode(ResponseCodeConstants.SUCCESS)
                .nama(va.getName())
                .nomorInvoice(va.getInvoiceNumber())
                .nomorPembayaran(va.getAccountNumber())
                .nilai(va.getAmount())
                .build();

        return response;
    }

    private MakaraResponse errorResponse(String code, String message){
        return MakaraResponse.builder()
                .responseCode(code)
                .responseMessage(message)
                .build();
    }
}
