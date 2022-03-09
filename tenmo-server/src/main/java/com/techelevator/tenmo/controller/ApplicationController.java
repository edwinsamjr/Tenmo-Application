package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;

@PreAuthorize("isAuthenticated()")
@RestController
public class ApplicationController {

    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    TransferDao transferDao;

    public ApplicationController() {
        this.transferDao = new JdbcTransferDao(jdbcTemplate);
    }

    @GetMapping(path ="balance")
    public BigDecimal findBalance(Principal principal) {
        String username = principal.getName();
        int accountId = this.transferDao.getUserAccountId(username);
        return this.transferDao.getBalance(accountId);
    }

}
