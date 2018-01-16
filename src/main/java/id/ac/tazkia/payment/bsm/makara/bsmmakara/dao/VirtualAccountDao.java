package id.ac.tazkia.payment.bsm.makara.bsmmakara.dao;

import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.AccountStatus;
import id.ac.tazkia.payment.bsm.makara.bsmmakara.entity.VirtualAccount;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface VirtualAccountDao extends PagingAndSortingRepository<VirtualAccount, String>{
    List<VirtualAccount> findByAccountNumberAndAccountStatusIn(String accountNumber, AccountStatus... accountStatus);
    VirtualAccount findByInvoiceNumberAndAccountStatusIn(String invoiceNumber, AccountStatus... accountStatus);
}
