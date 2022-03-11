package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class ApplicationController {

    TransferDao transferDao;
    UserDao userDao;

    public ApplicationController(TransferDao transferDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    @GetMapping(path = "/balance")
    public BigDecimal findBalance(Principal principal) {
        String username = principal.getName();
        int accountId = this.transferDao.getUserAccountId(username);
        return this.transferDao.getBalance(accountId);
    }

    @GetMapping(path = "/users")
    public List<User> listUsers(Principal principal) {
        List<User> allUsers = new ArrayList<>();
        allUsers = this.userDao.findAll();

        List<User> sendableUsers = new ArrayList<>();

        String username = principal.getName();

        for (User user : allUsers) {
            User currentUser = this.userDao.findByUsername(username);
            boolean loggedInUser = user.getUsername().equals(currentUser.getUsername());
            if (!loggedInUser) {
                sendableUsers.add(user);
            }
        }

        return sendableUsers;

    }

    @GetMapping(path = "/viewtransfers")
    public List<Transfer> viewTransfers(Principal principal) {
        List<Transfer> currentUserTransfers = new ArrayList<>();
        String username = principal.getName();

        int accountId = this.transferDao.getUserAccountId(username);
        currentUserTransfers = this.transferDao.findUserTransfers(accountId);

        return currentUserTransfers;

    }

    @GetMapping(path = "/viewtransfers/{id}")
    public Transfer viewTransferById(@PathVariable int id) {
        Transfer transfer = this.transferDao.findTransfer(id);
        return transfer;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/transfer")
    public void addTransfer(@Valid @RequestBody Transfer transfer) {
        this.transferDao.create(transfer);


        // TODO - Add exception if create doesn't work
    }

    @PreAuthorize("permitAll")
    @GetMapping(path = "/user")
    public User getUser(Principal principal){
        String username = principal.getName();
        User user = userDao.findByUsername(username);
        return user;
    }

    @GetMapping(path = "/username/{id}")
    public String getUsernameByAccountId(Principal principal, @PathVariable int id) {
        String username = transferDao.getUsernameByAccountId(id);
        return username;
    }

    @GetMapping(path = "/account")
        public int getAccountId(Principal principal) {
        int accountId = transferDao.getUserAccountId(principal.getName());
        return  accountId;
    }


}
