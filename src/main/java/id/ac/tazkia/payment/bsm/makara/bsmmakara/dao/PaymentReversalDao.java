package id.ac.tazkia.payment.bsm.makara.bsmmakara.dao;

import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.PaymentReversal;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PaymentReversalDao extends PagingAndSortingRepository<PaymentReversal, String> {
}
