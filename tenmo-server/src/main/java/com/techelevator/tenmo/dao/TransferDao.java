package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.AccountNotFoundException;
import com.techelevator.tenmo.exceptions.InvalidTransferException;
import com.techelevator.tenmo.exceptions.UserNotFoundException;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    BigDecimal getBalance(int accountId);

    BigDecimal updateSenderBalance(BigDecimal finalBalance, int accountId);

    BigDecimal updateReceiverBalance(BigDecimal finalBalance, int accountId) ;

    List<Transfer> findUserTransfers(int id);

    Transfer findTransferById(int id);

    int getUserAccountId(String username) throws AccountNotFoundException;

    boolean send(Transfer transfer) throws InvalidTransferException, AccountNotFoundException;

    String getUsernameByAccountId (int accountId) throws UserNotFoundException;

}
