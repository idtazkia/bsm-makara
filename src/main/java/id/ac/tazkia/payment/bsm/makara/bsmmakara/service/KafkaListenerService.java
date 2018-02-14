package id.ac.tazkia.payment.bsm.makara.bsmmakara.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dao.VirtualAccountDao;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.RequestType;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.VirtualAccountRequest;
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
}
