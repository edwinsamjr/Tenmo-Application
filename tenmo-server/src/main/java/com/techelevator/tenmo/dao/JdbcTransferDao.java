package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.AccountNotFoundException;
import com.techelevator.tenmo.exceptions.InvalidTransferException;
import com.techelevator.tenmo.exceptions.UserNotFoundException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;
    JdbcUserDao userDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        userDao = new JdbcUserDao(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }


    public boolean checkSufficientFunds(BigDecimal balance, BigDecimal transferAmount) {
        if (balance.compareTo(transferAmount) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public BigDecimal getBalance(int accountId) {
        //Gets current user's account balance

        String sql = "SELECT account_id, user_id, balance FROM account " +
                "WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        BigDecimal balance = new BigDecimal("0.00");
        if (rowSet.next()) {
            balance = rowSet.getBigDecimal("balance");
        }
        return balance;
    }

    public BigDecimal getFinalBalanceWithdraw(BigDecimal startingBalance, BigDecimal transferAmount) {
        //Gets sender's final balance for SQL Update query

        BigDecimal finalBalance = startingBalance.subtract(transferAmount);
        return finalBalance;
    }

    @Override
    public BigDecimal updateSenderBalance(BigDecimal finalBalance, int accountId) {
        //Updates sender's balance in database

        String sql = "UPDATE account SET balance = ? " +
                "WHERE account_id = ?;";
        jdbcTemplate.update(sql, finalBalance, accountId);
        return finalBalance;
    }

    public BigDecimal getFinalBalanceDeposit(BigDecimal startingBalance, BigDecimal transferAmount) {
        //Gets receiver's final balance for SQL Update query

        BigDecimal finalBalance = startingBalance.add(transferAmount);
        return finalBalance;
    }

    @Override
    public BigDecimal updateReceiverBalance(BigDecimal finalBalance, int accountId) {
        ////Updates receiver's balance in database

        String sql = "UPDATE account SET balance = ? " +
                "WHERE account_id = ?;";
        jdbcTemplate.update(sql, finalBalance, accountId);
        return finalBalance;
    }

    @Override
    public int getUserAccountId(String username) throws AccountNotFoundException {
        //Gets current user's account Id
        int accountId = -1;

        String sql = "SELECT account_id " +
                "FROM tenmo_user " +
                "JOIN account ON account.user_id = tenmo_user.user_id " +
                "WHERE username = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);

        if (rowSet.next()) {
            accountId = rowSet.getInt("account_id");
        }

        boolean accountExists = accountId != -1;
        if (!accountExists) {
            throw new AccountNotFoundException();
        }

        return accountId;
    }

    @Override
    public List<Transfer> findUserTransfers(int accountId) {
        //Lists current user's transfers

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
    public Transfer findTransferById(int id) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer " +
                "WHERE transfer_id = ?;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        Transfer transfer = new Transfer();
        if (rowSet.next()) {
            transfer = mapRowToTransfer(rowSet);
        }
        return transfer;
    }

    @Override
    public boolean send(Transfer transfer) throws InvalidTransferException, AccountNotFoundException {
        //Completes transfer

        transfer.setTransfer_id(getMaxIdPlusOne());
        BigDecimal userBalance = getBalance(transfer.getAccount_from());
        boolean hasSufficientFunds = userBalance.compareTo(transfer.getAmount()) >= 0;

        if (verifyAccounts(transfer) && hasSufficientFunds) {
            completeTransfer(transfer);
        } else {
            throw new InvalidTransferException();
        }

        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            this.jdbcTemplate.queryForObject(sql, Integer.class, 2,
                    2, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    @Override
    public String getUsernameByAccountId(int accountId) throws UserNotFoundException {
        String username = null;
        String sql = "SELECT username " +
                     "FROM account " +
                        "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                     "WHERE account.account_id = ?;";
        SqlRowSet rowSet = this.jdbcTemplate.queryForRowSet(sql, accountId);

        if (rowSet.next()) {
            username = rowSet.getString("username");
        }

        if (username == null) {
            throw new UserNotFoundException();
        }

        return username;

    }


    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setTransfer_id(rowSet.getInt("transfer_id"));
        transfer.setTransfer_type(rowSet.getInt("transfer_type_id"));
        transfer.setTransfer_status_id(rowSet.getInt("transfer_status_id"));
        transfer.setAccount_from(rowSet.getInt("account_from"));
        transfer.setAccount_to(rowSet.getInt("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));

        return transfer;
    }

    public List<Transfer> getAllTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer;";

        SqlRowSet rowSet = this.jdbcTemplate.queryForRowSet(sql);

        while (rowSet.next()) {
            Transfer transfer = mapRowToTransfer(rowSet);
            transfers.add(transfer);
        }

        return transfers;
    }

    private int getMaxID() {
        int maxID = 0;
        List<Transfer> transfers = getAllTransfers();
        for (Transfer transfer : transfers) {
            if (transfer.getTransfer_id() > maxID) {
                maxID = transfer.getTransfer_id();
            }
        }
        return maxID;
    }

    private int getMaxIdPlusOne() {
        return getMaxID() + 1;
    }

    private void completeTransfer(Transfer transfer) {
        //Gets sender and receiver's account IDs
        int senderAccount = transfer.getAccount_from();
        int receiverAccount = transfer.getAccount_to();

        //Gets sender and receiver's starting balances
        BigDecimal senderStartingBalance = getBalance(senderAccount);
        BigDecimal receiverStartingBalance = getBalance(receiverAccount);

        //Withdraws/deposits money and gets sender and receiver's final balances
        BigDecimal senderFinalBalance = getFinalBalanceWithdraw(senderStartingBalance, transfer.getAmount());
        BigDecimal receiverFinalBalance = getFinalBalanceDeposit(receiverStartingBalance, transfer.getAmount());

        //Updates sender and receiver's final balance in database
        updateSenderBalance(senderFinalBalance, senderAccount);
        updateReceiverBalance(receiverFinalBalance, receiverAccount);
    }

    public boolean verifyAccounts(Transfer transfer) throws AccountNotFoundException {
        /*Checks that both sender and receiver's account IDs are in database
        and don't match (cannot send money to yourself) */


        boolean senderAccountExists = false;
        boolean receiverAccountExists = false;

        boolean senderAndReceiverMatch = transfer.getAccount_from() == transfer.getAccount_to();

        List<User> users = userDao.findAll();

        List<Integer> accountNums = new ArrayList<>();

        for (User user : users) {
            int accountId = getUserAccountId(user.getUsername());
            accountNums.add(accountId);
        }

        for (int accountNum : accountNums) {
            if (transfer.getAccount_from() == accountNum) {
                senderAccountExists = true;
            } else if (transfer.getAccount_to() == accountNum) {
                receiverAccountExists = true;
            }
        }

        if (senderAccountExists && receiverAccountExists && !senderAndReceiverMatch) {
            return true;
        } else {
            return false;
        }


    }

}
