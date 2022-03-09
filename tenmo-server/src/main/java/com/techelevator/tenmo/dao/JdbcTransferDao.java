package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class JdbcTransferDao implements TransferDao {



    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean checkSufficientFunds(BigDecimal balance, BigDecimal transferAmount) {
        if (balance.compareTo(transferAmount) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public BigDecimal getBalance(int accountId) {
        String sql = "SELECT account_id, user_id, balance FROM account\n" +
                "WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
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

    @Override
    public BigDecimal updateSenderBalance(BigDecimal finalBalance, int accountId) {
        String sql = "UPDATE account SET balance = ? " +
                "WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, finalBalance, accountId);
        return finalBalance;
    }

    public BigDecimal getFinalBalanceDeposit(BigDecimal startingBalance, BigDecimal transferAmount) {
        BigDecimal finalBalance = startingBalance.add(transferAmount);
        return finalBalance;
    }

    @Override
    public BigDecimal updateReceiverBalance(BigDecimal finalBalance, int accountId) {
        String sql = "UPDATE account SET balance = ? " +
                "WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, finalBalance, accountId);
        return finalBalance;
    }

    @Override
    public int getUserAccountId(String username) {
        String sql = "SELECT account_id " +
                     "FROM tenmo_user " +
                        "JOIN account ON account.user_id = tenmo_user.user_id " +
                     "WHERE username = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        int accountId = -1;
        if (rowSet.next()) {
            accountId = rowSet.getInt("account_id");
        }

        return accountId;
    }

    @Override
    public List<Transfer> findUserTransfers(int accountId) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer " +
                     "WHERE transfer.account_from = ? OR transfer.account_to = ? " +
                     "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
        List<Transfer> transfers = new ArrayList<>();
        while (rowSet.next()) {
            Transfer transfer = mapRowToTransfer(rowSet);
            transfers.add(transfer);
        }

        return transfers;

    }

    @Override
    public Transfer findTransfer(int id) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer " +
                "WHERE transfer_id = ?;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        return mapRowToTransfer(rowSet);
    }


    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setTransfer_id(rowSet.getInt("transfer_id"));
        transfer.setTransfer_type(rowSet.getInt("transfer_type"));
        transfer.setTransfer_status_id(rowSet.getInt("transfer_status_id"));
        transfer.setAccount_from(rowSet.getInt("account_from"));
        transfer.setAccount_to(rowSet.getInt("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));

        return transfer;
    }
}
