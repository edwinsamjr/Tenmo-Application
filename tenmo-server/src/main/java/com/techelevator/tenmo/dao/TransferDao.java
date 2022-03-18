package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.*;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.data.relational.core.sql.In;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransferDao {


    BigDecimal getBalance(String username);

    BigDecimal updateSenderBalance(BigDecimal finalBalance, int accountId);

    BigDecimal updateReceiverBalance(BigDecimal finalBalance, int accountId) ;

    List<Transfer> findUserTransfers(int accountId);

    List<Transfer> findUserTransfers(String username);

    Transfer findTransferById(String username, int transferId) throws TransferNotFoundException;

    Transfer findTransferById(int id);

    int getUserAccountId(String username);

    boolean send(Transfer transfer, String username) throws InvalidTransferException, AccountNotFoundException;


    boolean request(Transfer transfer, String username) throws InvalidTransferException;

    List<Transfer> viewRequests(String username);


    void approveRequest(int transferId, String username) throws InvalidTransferException;

    void rejectRequest(int transferId, String username) throws IllegalTranscationException;

    String getUsernameByAccountId(int id);




}
