package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exceptions.AccountNotFoundException;
import com.techelevator.tenmo.exceptions.InvalidTransferException;
import com.techelevator.tenmo.exceptions.UserNotFoundException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
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
    public BigDecimal findBalance(Principal principal) throws AccountNotFoundException {
        //Gets current user's balance

        String username = principal.getName();
        int accountId = this.transferDao.getUserAccountId(username);
        return this.transferDao.getBalance(accountId);
    }

    @GetMapping(path = "/users")
    public List<User> listUsers(Principal principal) {
        //Lists users besides current user

        List<User> allUsers = this.userDao.findAll();

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
    public List<Transfer> viewTransfers(Principal principal) throws AccountNotFoundException {
        //Lists all transfers for current user

        String username = principal.getName();

        int accountId = this.transferDao.getUserAccountId(username);

        List<Transfer> currentUserTransfers = this.transferDao.findUserTransfers(accountId);

        return currentUserTransfers;

    }

    @GetMapping(path = "/viewtransfers/{id}")
    public Transfer viewTransferById(@PathVariable int id) {

        Transfer transfer = this.transferDao.findTransferById(id);
        return transfer;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/transfer")
    public void addTransfer(@Valid @RequestBody Transfer transfer) throws InvalidTransferException, AccountNotFoundException {
        //Enters Transfer into database

        this.transferDao.send(transfer);
    }

    @GetMapping(path = "/user")
    public User getUser(Principal principal) {
        //Gets current user

        String username = principal.getName();
        User user = userDao.findByUsername(username);
        return user;
    }

    @GetMapping(path = "/username/{id}")
    public String getUsernameByAccountId(Principal principal, @PathVariable int id) throws UserNotFoundException {

        String username = transferDao.getUsernameByAccountId(id);
        return username;
    }

    @GetMapping(path = "/account")
    public int getAccountId(Principal principal) throws AccountNotFoundException {
        //Gets current user's account Id

        int accountId = transferDao.getUserAccountId(principal.getName());
        return accountId;
    }

    @GetMapping(path = "/account/{username}")
    public int getAccountIdByUsername(@PathVariable String username) throws AccountNotFoundException {
        int accountId = transferDao.getUserAccountId(username);
        return accountId;
    }


}
