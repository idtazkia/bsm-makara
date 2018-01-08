package id.ac.tazkia.payment.bsm.makara.bsmmakara.dao;

import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.Payment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PaymentDao extends PagingAndSortingRepository<Payment, String> {
}
