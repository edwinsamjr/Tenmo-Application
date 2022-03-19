package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.TransferNotFoundException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDetailsDao implements TransferDetailsDao {

    private JdbcTemplate jdbcTemplate;
    JdbcUserDao userDao;

    public JdbcTransferDetailsDao(JdbcTemplate jdbcTemplate) {
        userDao = new JdbcUserDao(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TransferDetails findTransferById(String username, int transferId) throws TransferNotFoundException {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, c.username AS sendername, d.username AS receivername " +
                "FROM transfer " +
                "JOIN account a ON a.account_id = transfer.account_from " +
                "JOIN account b ON b.account_id = transfer.account_to " +
                "JOIN tenmo_user c ON c.user_id = a.user_id " +
                "JOIN tenmo_user d ON d.user_id = b.user_id " +
                "WHERE (transfer.account_from = ( " +
                "SELECT account_id " +
                "FROM account " +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.username = ? " +
                "    ) " +
                "OR transfer.account_to = ( " +
                "SELECT account_id " +
                "FROM account " +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.username = ? " +
                ")) AND transfer_status_id = 2 AND transfer_id = ? " +
                "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username, username, transferId);
        TransferDetails transferDetails;
        if (rowSet.next()) {
            transferDetails = mapRowToTransferDetails(rowSet);
        } else {
            throw new TransferNotFoundException();
        }

        return transferDetails;
    }

    @Override
    public List<TransferDetails> findUserTransferDetails(String username) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, c.username AS sendername, d.username AS receivername " +
                "FROM transfer " +
                "JOIN account a ON a.account_id = transfer.account_from " +
                "JOIN account b ON b.account_id = transfer.account_to " +
                "JOIN tenmo_user c ON c.user_id = a.user_id " +
                "JOIN tenmo_user d ON d.user_id = b.user_id " +
                "WHERE (transfer.account_from = ( " +
                "SELECT account_id " +
                "FROM account " +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.username = ? " +
                "    ) " +
                "OR transfer.account_to = ( " +
                "SELECT account_id " +
                "FROM account " +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.username = ? " +
                ")) AND transfer_status_id = 2 " +
                "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username, username);
        List<TransferDetails> transferDetailsList = new ArrayList<>();
        while (rowSet.next()) {
            TransferDetails transferDetails = mapRowToTransferDetails(rowSet);
            transferDetailsList.add(transferDetails);
        }

        return transferDetailsList;
    }

    public List<TransferDetails> listRequests(String username){
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, c.username AS sendername, d.username AS receivername\n" +
                "FROM transfer\n" +
                "JOIN account a ON a.account_id = transfer.account_from\n" +
                "JOIN account b ON b.account_id = transfer.account_to\n" +
                "JOIN tenmo_user c ON c.user_id = a.user_id\n" +
                "JOIN tenmo_user d ON d.user_id = b.user_id\n" +
                "WHERE (transfer.account_from = (\n" +
                "\t\tSELECT account_id\n" +
                "\t\tFROM account\n" +
                "\t\t\tJOIN tenmo_user ON tenmo_user.user_id = account.user_id\n" +
                "\t\tWHERE tenmo_user.username = ?\n" +
                "    )\n" +
                ") AND transfer_status_id = 1\n" +
                "ORDER BY transfer_id;";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        List<TransferDetails> transferDetailsList = new ArrayList<>();
        while (rowSet.next()) {
            TransferDetails transferDetails = mapRowToTransferDetails(rowSet);
            transferDetailsList.add(transferDetails);
        }

        return transferDetailsList;
    }

    private TransferDetails mapRowToTransferDetails(SqlRowSet rowSet) {
        TransferDetails transferDetails = new TransferDetails(rowSet.getInt("transfer_id"), rowSet.getInt("transfer_type_id"),
                rowSet.getInt("transfer_status_id"), rowSet.getInt("account_from"), rowSet.getInt("account_to"), rowSet.getBigDecimal("amount"),
                rowSet.getString("sendername"), rowSet.getString("receivername"));
        return transferDetails;
    }
}
