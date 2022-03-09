package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean checkSufficientFunds(BigDecimal balance, BigDecimal transferAmount) {
        if (balance.compareTo(transferAmount) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public BigDecimal getBalance(int accountFrom) {
        String sql = "SELECT account_id, user_id, balance FROM account\n" +
                "WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountFrom);
        BigDecimal balance = new BigDecimal("0.00");
        if (rowSet.next()) {
            balance = rowSet.getBigDecimal("balance");
        }
        return balance;
    }

    public BigDecimal getFinalBalanceWithdraw(BigDecimal startingBalance, BigDecimal transferAmount) {
        BigDecimal finalBalance = startingBalance.subtract(transferAmount);
        return finalBalance;
    }

    public BigDecimal withdrawMoneyFromSender(BigDecimal finalBalance, int accountId) {
        String sql = "UPDATE account SET balance = ? " +
                "WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, finalBalance, accountId);
        return finalBalance;
    }

    public BigDecimal getFinalBalanceDeposit(BigDecimal startingBalance, BigDecimal transferAmount) {
        BigDecimal finalBalance = startingBalance.add(transferAmount);
        return finalBalance;
    }

    public BigDecimal depositMoneyToReceiver(BigDecimal finalBalance, int accountId) {
        String sql = "UPDATE account SET balance = ? " +
                "WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, finalBalance, accountId);
        return finalBalance;
    }

    public List<Transfer> findUserTransfers() {
        return null;
    }

    public Transfer findTransfer(int id) {
        return null;
    }
}
