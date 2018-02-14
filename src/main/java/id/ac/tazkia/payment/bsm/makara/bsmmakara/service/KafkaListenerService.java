package id.ac.tazkia.payment.bsm.makara.bsmmakara.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dao.VirtualAccountDao;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.RequestType;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.VaPayment;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.VirtualAccountRequest;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.AccountStatus;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.VirtualAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerService.class);

    @Value("${bsm.bank-id}") private String bankId;
    @Autowired private VirtualAccountService virtualAccountService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private VirtualAccountDao virtualAccountDao;

    @KafkaListener(topics = "${kafka.topic.va.request}", groupId = "${spring.kafka.consumer.group-id}")
    public void receiveVirtualAccountRequest(String message){
        try {
            LOGGER.debug("Receive message : {}", message);
            VirtualAccountRequest vaRequest = objectMapper.readValue(message, VirtualAccountRequest.class);
            if (!bankId.equalsIgnoreCase(vaRequest.getBankId())) {
                LOGGER.debug("Request untuk bank {}, tidak diproses", vaRequest.getBankId());
                return;
            }
            vaRequest.setRequestTime(LocalDateTime.now());
            if(RequestType.CREATE.equals(vaRequest.getRequestType())) {
                virtualAccountService.createVirtualAccount(vaRequest);
            } else if(RequestType.DELETE.equals(vaRequest.getRequestType())){
                virtualAccountService.deleteVirtualAccount(vaRequest);
            } else if(RequestType.UPDATE.equals(vaRequest.getRequestType())){
                virtualAccountService.updateVirtualAccount(vaRequest);
            } else {
                LOGGER.warn("Virtual Account Request Type {} belum dibuat", vaRequest.getRequestType());
            }
        } catch (Exception err){
            LOGGER.error(err.getMessage(), err);
        }
    }

    @KafkaListener(topics = "${kafka.topic.va.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleVaPayment(String message) {
        try {
            LOGGER.debug("Receive message : {}", message);
            VaPayment payment = objectMapper.readValue(message, VaPayment.class);

            // notif dari diri sendiri, tidak perlu diproses
            if (bankId.equalsIgnoreCase(payment.getBankId())) {
                return;
            }

            VirtualAccount va = virtualAccountDao.findByInvoiceNumberAndAccountStatusIn(payment.getInvoiceNumber(), AccountStatus.UNPAID, AccountStatus.PAID_PARTIALLY);
            // bila va tidak terdaftar di sini, tidak perlu dilanjutkan
            if (va == null) {
                return;
            }

            LOGGER.info("VA {} dibayar di bank {} senilai {}",
                    va.getAccountNumber(),
                    payment.getBankId(),
                    payment.getCumulativeAmount());

            // create lagi VA dengan nilai baru
            VirtualAccountRequest request = VirtualAccountRequest.builder()
                    .requestTime(LocalDateTime.now())
                    .requestType(RequestType.CREATE)
                    .accountNumber(va.getAccountNumber())
                    .accountType(va.getAccountType())
                    .bankId(bankId)
                    .description(va.getDescription())
                    .email(va.getEmail())
                    .invoiceNumber(va.getInvoiceNumber())
                    .expireDate(va.getExpireDate())
                    .name(va.getName())
                    .phone(va.getPhone())
                    .build();

            request.setAmount(va.getAmount().subtract(payment.getCumulativeAmount()));

            // kalau pembayaran full, hapus va
            if (va.getAmount().compareTo(payment.getCumulativeAmount()) == 0) {
                request.setRequestType(RequestType.DELETE);
                virtualAccountService.deleteVirtualAccount(request);
            } else {
                request.setRequestType(RequestType.UPDATE);
                virtualAccountService.updateVirtualAccount(request);
            }

        } catch (Exception err) {
            LOGGER.error(err.getMessage(), err);
        }
    }
}
