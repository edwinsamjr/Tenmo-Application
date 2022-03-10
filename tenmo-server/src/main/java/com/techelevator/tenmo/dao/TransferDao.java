package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    BigDecimal getBalance(int accountId);

    BigDecimal updateSenderBalance(BigDecimal finalBalance, int accountId);

    BigDecimal updateReceiverBalance(BigDecimal finalBalance, int accountId);

    List<Transfer> findUserTransfers(int id);

    Transfer findTransfer(int id);

    int getUserAccountId(String username);

    boolean create(Transfer transfer);

}
