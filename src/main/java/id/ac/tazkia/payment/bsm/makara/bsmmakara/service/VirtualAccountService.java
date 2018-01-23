package id.ac.tazkia.payment.bsm.makara.bsmmakara.service;

import id.ac.tazkia.payment.bsm.makara.bsmmakara.dao.VirtualAccountDao;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.RequestStatus;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.dto.VirtualAccountRequest;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.AccountStatus;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.VirtualAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service @Transactional
public class VirtualAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualAccountService.class);

    @Autowired private VirtualAccountDao virtualAccountDao;
    @Autowired private KafkaSenderService kafkaSenderService;

    public void createVirtualAccount(VirtualAccountRequest vaRequest) {
        try {
            VirtualAccount va = new VirtualAccount();
            va.setCreateTime(LocalDateTime.now());
            BeanUtils.copyProperties(vaRequest, va);
            virtualAccountDao.save(va);
            vaRequest.setRequestStatus(RequestStatus.SUCCESS);
            kafkaSenderService.sendVaResponse(vaRequest);
        } catch (Exception err) {
            LOGGER.warn(err.getMessage(), err);
            vaRequest.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(vaRequest);
        }
    }

    public void deleteVirtualAccount(VirtualAccountRequest request) {
        List<VirtualAccount> existing = virtualAccountDao
                .findByAccountNumberAndAccountStatusIn(request.getAccountNumber(), AccountStatus.UNPAID, AccountStatus.PAID_PARTIALLY);

        if(existing.isEmpty()) {
            LOGGER.warn("VA dengan nomor {} belum ada", request.getAccountNumber());
            request.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(request);
            return;
        }

        if(existing.size() > 1){
            LOGGER.warn("VA dengan nomor {} ada {} buah. Delete tidak dapat diproses",
                    request.getAccountNumber(), existing.size());
            request.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(request);
            return;
        }

        try {
            VirtualAccount va = existing.iterator().next();
            va.setAccountStatus(AccountStatus.INACTIVE);
            virtualAccountDao.save(va);
            request.setRequestStatus(RequestStatus.SUCCESS);

            kafkaSenderService.sendVaResponse(request);
        } catch (Exception err) {
            LOGGER.warn(err.getMessage(), err);
            request.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(request);
        }

    }

    public void updateVirtualAccount(VirtualAccountRequest request) {
        List<VirtualAccount> existing = virtualAccountDao
                .findByAccountNumberAndAccountStatusIn(request.getAccountNumber(), AccountStatus.UNPAID, AccountStatus.PAID_PARTIALLY);

        if(existing.isEmpty()) {
            LOGGER.warn("VA dengan nomor {} belum ada", request.getAccountNumber());
            request.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(request);
            return;
        }

        if(existing.size() > 1){
            LOGGER.warn("VA dengan nomor {} ada {} buah. Delete tidak dapat diproses",
                    request.getAccountNumber(), existing.size());
            request.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(request);
            return;
        }

        try {
            VirtualAccount va = existing.iterator().next();
            BeanUtils.copyProperties(request, va);

            // kalau tanggal expire yang baru sudah lewat, langsung nonaktifkan
            if (va.getExpireDate().isBefore(LocalDate.now())) {
                va.setAccountStatus(AccountStatus.INACTIVE);
            }

            virtualAccountDao.save(va);
            request.setRequestStatus(RequestStatus.SUCCESS);
            kafkaSenderService.sendVaResponse(request);
        } catch (Exception err) {
            LOGGER.warn(err.getMessage(), err);
            request.setRequestStatus(RequestStatus.ERROR);
            kafkaSenderService.sendVaResponse(request);
        }
    }
}
