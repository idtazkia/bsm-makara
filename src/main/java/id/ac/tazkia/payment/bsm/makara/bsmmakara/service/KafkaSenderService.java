package id.ac.tazkia.payment.bsm.makara.bsmmakara.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.VaPayment;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.VaResponse;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.VirtualAccountRequest;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class KafkaSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSenderService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @Value("${bsm.bank-id}") private String bankId;
    @Value("${bsm.client-id}") private String bankClientId;
    @Value("${bsm.prefix}") private String bankAccountPrefix;
    @Value("${kafka.topic.va.response}") private String kafkaTopicResponse;
    @Value("${kafka.topic.va.payment}") private String kafkaTopicPayment;

    @Async
    public void sendVaResponse(VirtualAccountRequest va) {
        try {
            VaResponse vaResponse = new VaResponse();
            BeanUtils.copyProperties(va, vaResponse);
            vaResponse.setAccountNumber(accountToVaNumber(va.getAccountNumber()));
            String jsonResponse = objectMapper.writeValueAsString(vaResponse);
            LOGGER.debug("VA Response : {}", jsonResponse);
            kafkaTemplate.send(kafkaTopicResponse, jsonResponse);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Async
    public void sendVaPayment(Payment payment) {
        try {
            VaPayment vaPayment = new VaPayment();
            vaPayment.setBankId(bankId);
            vaPayment.setInvoiceNumber(payment.getVirtualAccount().getInvoiceNumber());
            vaPayment.setAccountNumber(accountToVaNumber(payment.getVirtualAccount().getAccountNumber()));
            vaPayment.setAmount(payment.getAmount());
            vaPayment.setCumulativeAmount(payment.getVirtualAccount().getCumulativePayment());
            vaPayment.setPaymentTime(payment.getTransactionTime());
            vaPayment.setReference(payment.getClientReference());
            String jsonData = objectMapper.writeValueAsString(vaPayment);
            LOGGER.debug("VA Payment : {}", jsonData);
            kafkaTemplate.send(kafkaTopicPayment, jsonData);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String accountToVaNumber(String accountNumber) {
        return bankAccountPrefix + bankClientId + accountNumber;
    }
}
