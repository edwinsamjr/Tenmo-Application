package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

boolean checkSufficientFunds(BigDecimal balance, BigDecimal transferAmount);

BigDecimal withdrawMoneyFromSender(BigDecimal finalBalance, int accountId);

BigDecimal depositMoneyToReceiver(BigDecimal finalBalance, int accountId);

List<Transfer> findUserTransfers();

Transfer findTransfer(int id);

}
