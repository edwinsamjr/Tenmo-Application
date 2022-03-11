package com.techelevator;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;

public class TransferDaoTest {

    private UserDao userDao;

    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    JdbcTransferDao jdbcTransferDao = new JdbcTransferDao(jdbcTemplate);

    static final Transfer TRANSFER_1001_DOLLARS = new Transfer(1, 2, 2, 2001, 2002, new BigDecimal("1001.00"));
    static final Transfer TRANSFER_1000_DOLLARS = new Transfer(1, 2, 2, 2001, 2002, new BigDecimal("1000.00"));
    static final Transfer TRANSFER_300_DOLLARS = new Transfer(1, 2, 2, 2001, 2002, new BigDecimal("300.00"));

    @Test
    public void check_sufficient_funds_returns_false() {
        boolean result = jdbcTransferDao.checkSufficientFunds(new BigDecimal("1000.00"), TRANSFER_1001_DOLLARS.getAmount());
        Assert.assertFalse("Incorrectly returns true", result);
    }

    @Test
    public void check_sufficient_funds_returns_true() {
        boolean result = jdbcTransferDao.checkSufficientFunds(new BigDecimal("1000.00"), TRANSFER_1000_DOLLARS.getAmount());
        Assert.assertTrue("Incorrectly returns false", result);
    }

    @Test
    public void correctly_withdraws_funds() {
        BigDecimal startingBalance = new BigDecimal("1000.00");
        BigDecimal finalBalance = jdbcTransferDao.getFinalBalanceWithdraw(startingBalance, TRANSFER_300_DOLLARS.getAmount());
        Assert.assertEquals("Withdraws incorrect amount", new BigDecimal("700.00"), finalBalance);
    }

    @Test
    public void correctly_deposits_funds() {
        BigDecimal startingBalance = new BigDecimal("1000.00");
        BigDecimal finalBalance = jdbcTransferDao.getFinalBalanceDeposit(startingBalance, TRANSFER_300_DOLLARS.getAmount());
        Assert.assertEquals("Deposits incorrect amount", new BigDecimal("1300.00"), finalBalance);
    }
}
