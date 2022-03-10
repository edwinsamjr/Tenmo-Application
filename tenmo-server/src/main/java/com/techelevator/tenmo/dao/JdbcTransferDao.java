package com.techelevator.tenmo.dao;

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
        jdbcTemplate.update(sql, finalBalance, accountId);
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
        jdbcTemplate.update(sql, finalBalance, accountId);
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
        Transfer transfer = new Transfer();
        if (rowSet.next()) {
            transfer = mapRowToTransfer(rowSet);
        }
        return transfer;
    }

    @Override
    public boolean create(Transfer transfer) {
        transfer.setTransfer_id(getMaxIdPlusOne());

        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
                     "VALUES (?, ?, ?, ?, ?)";
        if (verifyAccounts(transfer)) {
            completeTransfer(transfer);
        }
        Integer newTransferId;

        try {
            newTransferId = this.jdbcTemplate.queryForObject(sql, Integer.class, transfer.getTransfer_type(),
                        transfer.getTransfer_status_id(), transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());

        } catch (DataAccessException e) {
            return false;
        }

        return true;
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

    /**
     * Adds 1 to the max id and returns it
     *
     * @return
     */
    private int getMaxIdPlusOne() {
        return getMaxID() + 1;
    }

    private void completeTransfer(Transfer transfer) {
        int senderAccount = transfer.getAccount_from();
        int receiverAccount = transfer.getAccount_to();

        BigDecimal senderStartingBalance = getBalance(senderAccount);
        BigDecimal receiverStartingBalance = getBalance(receiverAccount);

        BigDecimal senderFinalBalance = getFinalBalanceWithdraw(senderStartingBalance, transfer.getAmount());
        BigDecimal receiverFinalBalance = getFinalBalanceDeposit(receiverStartingBalance, transfer.getAmount());

        updateSenderBalance(senderFinalBalance, senderAccount);
        updateReceiverBalance(receiverFinalBalance, receiverAccount);

    }

    public boolean verifyAccounts(Transfer transfer) {
        boolean senderAccountExists = false;
        boolean receiverAccountExists = false;

        List<User> users = userDao.findAll();

        List<Integer> accountNums = new ArrayList<>();

        for (User user : users) {
            int accountId = getUserAccountId(user.getUsername());
            accountNums.add(accountId);
        }

        for (int accountNum : accountNums) {
            if (transfer.getAccount_from() == accountNum){
                senderAccountExists = true;
            } else if (transfer.getAccount_to() == accountNum) {
                receiverAccountExists = true;
            }
        }

        if (senderAccountExists && receiverAccountExists) {
            return true;
        } else {
            return false;
        }


    }

//    public List<User> findAll() {
//        List<User> users = new ArrayList<>();
//        String sql = "SELECT user_id, username, password_hash FROM tenmo_user;";
//        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
//        while(results.next()) {
//            User user = mapRowToUser(results);
//            users.add(user);
//        }
//        return users;
//    }
//
//    private User mapRowToUser(SqlRowSet rs) {
//        User user = new User();
//        user.setId(rs.getLong("user_id"));
//        user.setUsername(rs.getString("username"));
//        user.setPassword(rs.getString("password_hash"));
//        user.setActivated(true);
//        user.setAuthorities("USER");
//        return user;
//    }
}
