package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.TransferNotFoundException;
import com.techelevator.tenmo.model.TransferDetails;

import java.util.List;

public interface TransferDetailsDao {

    TransferDetails findTransferById(String username, int transferId) throws TransferNotFoundException;

    List<TransferDetails> findUserTransferDetails(String username);

    List<TransferDetails> listRequests(String username);
}
