package id.ac.tazkia.payment.bsm.makara.bsmmakara.dao;

import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.Payment;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.VirtualAccount;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PaymentDao extends PagingAndSortingRepository<Payment, String> {
    List<Payment> findByClientReferenceAndVirtualAccount(String clientReference, VirtualAccount virtualAccount);
    List<Payment> findByVirtualAccount(VirtualAccount virtualAccount);
}
