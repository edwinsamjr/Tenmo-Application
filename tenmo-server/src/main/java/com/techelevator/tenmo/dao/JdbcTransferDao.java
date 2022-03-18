package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.*;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;
    JdbcUserDao userDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        userDao = new JdbcUserDao(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }


    public BigDecimal getBalance(String username) {
        //Gets current user's account balance

        String sql = "SELECT balance " +
                     "FROM account " +
                     "WHERE account.account_id = " +
                        "(SELECT account_id " +
                        "FROM account " +
                            "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                        "WHERE tenmo_user.username = ?);";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        BigDecimal balance = new BigDecimal("0.00");
        if (rowSet.next()) {
            balance = rowSet.getBigDecimal("balance");
        }
        return balance;
    }

    public boolean checkSufficientFundsForRequest(String username, int transferId) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer " +
                     "WHERE (transfer.account_from = ( " +
                            "SELECT account_id  " +
                            "FROM account " +
                                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id  " +
                            "WHERE tenmo_user.username = ?  " +
                                ")   " +
                        "OR transfer.account_to = (  " +
                            "SELECT account_id  " +
                            "FROM account  " +
                                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id  " +
                            "WHERE tenmo_user.username = ?  " +
                                ")) AND transfer_status_id = 1 AND transfer_id = ?" +
                     "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username, username, transferId);
        Transfer transfer = null;
        if (rowSet.next()) {
            transfer = mapRowToTransfer(rowSet);
        }

        BigDecimal balance = getBalance(username);

        if (transfer == null) {
            return false;
        } else if (balance.compareTo(transfer.getAmount()) > -1) {
            return true;
        } else {
            return false;
        }

    }


    public boolean checkSufficientFundsForSend(Transfer transfer, String username) {
        BigDecimal balance = getBalance(username);

        if (balance.compareTo(transfer.getAmount()) > -1) {
            return true;
        } else {
            return false;
        }

    }



    @Override
    public BigDecimal updateSenderBalance(BigDecimal amount, int accountId) {
        String sql = "UPDATE account SET balance = balance - ? " +
                     "WHERE account_id = ?" +
                     "RETURNING balance;";

        BigDecimal balance = this.jdbcTemplate.queryForObject(sql, BigDecimal.class, amount, accountId);
        return balance;
    }

    @Override
    public BigDecimal updateReceiverBalance(BigDecimal amount, int accountId) {
        String sql = "UPDATE account SET balance = balance + ? " +
                     "WHERE account_id = ?" +
                     "RETURNING balance;";

        BigDecimal balance = this.jdbcTemplate.queryForObject(sql, BigDecimal.class, amount, accountId);
        return balance;
    }




    @Override
    public int getUserAccountId(String username) {
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

        /* The method will only be run with the current user's username,
           so accountId will never return as -1 */
        return accountId;
    }

    @Override
    public List<Transfer> findUserTransfers(int accountId) {
        //Lists current user's transfers

        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer " +
                     "WHERE (transfer.account_from = ? OR transfer.account_to = ?) AND transfer_status_id = 2 " +
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
    public List<Transfer> findUserTransfers(String username) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer " +
                     "WHERE (transfer.account_from = ( " +
                        "SELECT account_id FROM account WHERE account.account_id =  " +
                        "(SELECT account_id " +
                        "FROM account " +
                            "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                        "WHERE tenmo_user.username = ?) " +
                     ") " +
                     "OR transfer.account_to = ( " +
                     "SELECT account_id FROM account WHERE account.account_id =  " +
                     "(SELECT account_id " +
                     "FROM account " +
                        "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                     "WHERE tenmo_user.username = ?) " +
                     ")) AND transfer_status_id = 2 " +
                     "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username, username);
        List<Transfer> transfers = new ArrayList<>();
        while (rowSet.next()) {
            Transfer transfer = mapRowToTransfer(rowSet);
            transfers.add(transfer);
        }

        return transfers;

    }

    @Override
    public Transfer findTransferById(String username, int transferId) throws TransferNotFoundException {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer " +
                     "WHERE (transfer.account_from = ( " +
                            "SELECT account_id FROM account WHERE account.account_id =  " +
                                "(SELECT account_id " +
                                "FROM account " +
                                    "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                                "WHERE tenmo_user.username = ?) " +
                        ") " +
                        "OR transfer.account_to = ( " +
                            "SELECT account_id FROM account WHERE account.account_id =  " +
                                "(SELECT account_id " +
                                 "FROM account " +
                                    "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                                "WHERE tenmo_user.username = ?) " +
                        ")) AND transfer_status_id = 2 AND transfer_id = ?" +
                     "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username, username, transferId);
        Transfer transfer;
        if (rowSet.next()) {
            transfer = mapRowToTransfer(rowSet);
        } else {
            throw new TransferNotFoundException();
        }

        return transfer;

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
    public boolean send(Transfer transfer, String username) throws InvalidTransferException {
        //Completes transfer

        boolean hasSufficientFunds = checkSufficientFundsForSend(transfer, username);

        if (verifyAccountsForSend(transfer, username) && hasSufficientFunds) {
            updateBalancesSend(transfer);
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
    public boolean request(Transfer transfer, String username) throws InvalidTransferException{

        if (!verifyAccountsForRequest(transfer, username)) {
            throw new InvalidTransferException();
        }

        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            this.jdbcTemplate.queryForObject(sql, Integer.class, 1,
                    1, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }


    @Override
    public List<Transfer> viewRequests(String username) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer " +
                     "WHERE transfer.account_from = ( " +
                        "SELECT account_id FROM account WHERE account.account_id =  " +
                            "(SELECT account_id " +
                            "FROM account " +
                                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                            "WHERE tenmo_user.username = ?) " +
                     ") AND transfer_status_id = 1 " +
                     "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        List<Transfer> transfers = new ArrayList<>();
        while (rowSet.next()) {
            Transfer transfer = mapRowToTransfer(rowSet);
            transfers.add(transfer);
        }

        return transfers;
    }

    @Override
    public void approveRequest(int transferId, String username) throws InvalidTransferException {
        if (!checkSufficientFundsForRequest(username, transferId)) {
            throw new InvalidTransferException();
        }
        String sql = "UPDATE transfer SET transfer_status_id = 2 \n" +
                "WHERE account_from = ( \n" +
                "\t\tSELECT account_id \n" +
                "\t\tFROM account \n" +
                "\t\t\t JOIN tenmo_user ON tenmo_user.user_id = account.user_id \n" +
                "\t\tWHERE tenmo_user.username = ?) \n" +
                "\tAND transfer_id = ? AND transfer_status_id = 1\n" +
                "RETURNING transfer_status_id;";
        int newStatusId = this.jdbcTemplate.queryForObject(sql, Integer.class, username, transferId);

        if (newStatusId != 2) {
            throw new InvalidTransferException();
        }

        updateBalancesRequest(transferId);

    }


    @Override
    public void rejectRequest(int transferId, String username) throws IllegalTranscationException {
        int newStatusId = -1;
        try {
            String sql = "UPDATE transfer SET transfer_status_id = 3 " +
                    "WHERE account_from = ( " +
                    "SELECT account_id FROM account WHERE account.account_id =  " +
                    "(SELECT account_id " +
                    " FROM account " +
                    "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                    " WHERE tenmo_user.username = ?) " +
                    "  ) AND transfer_id = ? AND transfer_status_id = 1 " +
                    "RETURNING transfer_status_id;";
            newStatusId = this.jdbcTemplate.queryForObject(sql, Integer.class, username, transferId);
        } catch (EmptyResultDataAccessException e ) {
            throw new IllegalTranscationException();
        }

    }

    @Override
    public String getUsernameByAccountId(int id) {
        String sql = "SELECT username " +
                     "FROM tenmo_user " +
                        "JOIN account ON account.user_id = tenmo_user.user_id " +
                     "WHERE account.account_id = ?;";

        String username = this.jdbcTemplate.queryForObject(sql, String.class, id);

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



    private void updateBalancesRequest(int transferId) {
        Transfer transfer = findTransferById(transferId);

        updateSenderBalance(transfer.getAmount(), transfer.getAccount_from());
        updateReceiverBalance(transfer.getAmount(), transfer.getAccount_to());

    }

    private void updateBalancesSend(Transfer transfer) {
        updateSenderBalance(transfer.getAmount(), transfer.getAccount_from());
        updateReceiverBalance(transfer.getAmount(), transfer.getAccount_to());
    }


    public boolean verifyAccountsForRequest(Transfer transfer, String username){
        /*Checks that both sender and receiver's account IDs are in database
        and don't match (cannot send money to yourself) */

        String sql = "SELECT COUNT(*) FROM account WHERE account_id = ? OR account_id = ?;";

        //Given the sender and receiver's account IDs, returns how many exist in the database (0, 1, or 2?)
        int numValidAccounts = this.jdbcTemplate.queryForObject(sql, Integer.class, transfer.getAccount_from(), transfer.getAccount_to());

        boolean bothAccountsExist = numValidAccounts == 2;
        boolean senderAndReceiverMatch = transfer.getAccount_from() == transfer.getAccount_to();

        boolean receiverIsCurrentUser = getUserAccountId(username) == transfer.getAccount_to();


        if (bothAccountsExist && !senderAndReceiverMatch && receiverIsCurrentUser) {
            return true;
        } else {
            return false;
        }


    }

    public boolean verifyAccountsForSend(Transfer transfer, String username) {
        /*Checks that both sender and receiver's account IDs are in database
        and don't match (cannot send money to yourself) */

        String sql = "SELECT COUNT(*) FROM account WHERE account_id = ? OR account_id = ?;";

        //Given the sender and receiver's account IDs, returns how many exist in the database (0, 1, or 2?)
        int numValidAccounts = this.jdbcTemplate.queryForObject(sql, Integer.class, transfer.getAccount_from(), transfer.getAccount_to());

        boolean bothAccountsExist = numValidAccounts == 2;
        boolean senderAndReceiverMatch = transfer.getAccount_from() == transfer.getAccount_to();

        boolean senderIsCurrentUser = getUserAccountId(username) == transfer.getAccount_from();


        if (bothAccountsExist && !senderAndReceiverMatch && senderIsCurrentUser) {
            return true;
        } else {
            return false;
        }

    }

}
