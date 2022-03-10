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

    @Test
    public void check_sufficient_funds_returns_false() {
        Transfer transfer = new Transfer(1, 2, 2, 2001, 2002, new BigDecimal("10001.00"));
        boolean result = jdbcTransferDao.checkSufficientFunds(new BigDecimal("10000.00"), transfer.getAmount());
        Assert.assertFalse("Incorrectly returns true", result);
    }

    @Test
    public void check_sufficient_funds_returns_true() {
        Transfer transfer = new Transfer(1, 2, 2, 2001, 2002, new BigDecimal("9999.00"));
        boolean result = jdbcTransferDao.checkSufficientFunds(new BigDecimal("10000.00"), transfer.getAmount());
        Assert.assertTrue("Incorrectly returns false", result);
    }
}
