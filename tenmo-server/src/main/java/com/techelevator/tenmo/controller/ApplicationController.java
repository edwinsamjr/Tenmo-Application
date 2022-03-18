package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exceptions.*;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //Gets current user's balance
        return this.transferDao.getBalance(principal.getName());
    }


    @GetMapping(path = "/users")
    public List<User> listUsers(Principal principal) {
        //Lists users besides current user

        List<User> sendableUsers = this.userDao.findSendableUsers(principal.getName());
        return sendableUsers;

    }


    @GetMapping(path = "/viewtransfers")
    public List<Transfer> viewTransfers(Principal principal){
        //Lists all transfers for current user

        List<Transfer> currentUserTransfers = this.transferDao.findUserTransfers(principal.getName());
        return currentUserTransfers;

    }

    @GetMapping(path = "/viewtransfers/{id}")
    public Transfer viewTransferById(Principal principal, @PathVariable int id) throws TransferNotFoundException {

        Transfer transfer = this.transferDao.findTransferById(principal.getName(), id);
        return transfer;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/send")
    public void addTransfer(Principal principal, @Valid @RequestBody Transfer transfer) throws InvalidTransferException, AccountNotFoundException {
        //Enters Transfer into database

        this.transferDao.send(transfer, principal.getName());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/request")
    public void request(Principal principal, @Valid @RequestBody Transfer transfer) throws InvalidTransferException{

        this.transferDao.request(transfer, principal.getName());
    }


    @GetMapping(path = "/viewrequests")
    public List<Transfer> viewRequests(Principal principal){
        return transferDao.viewRequests(principal.getName());
    }


    @PostMapping(path = "/approve/{id}")
    public void approveRequest(Principal principal, @PathVariable int id) throws InvalidTransferException {
        this.transferDao.approveRequest(id, principal.getName());
    }

    @PostMapping(path = "/reject/{id}")
    public void rejectRequest(Principal principal, @PathVariable int id) throws IllegalTranscationException {
        this.transferDao.rejectRequest(id, principal.getName());
    }

    @GetMapping(path = "/user")
    public User getUser(Principal principal) {
        //Gets current user

        User user = userDao.findByUsername(principal.getName());
        return user;
    }


    @GetMapping(path = "/username/{id}")
    public String getUsernameByAccountId(Principal principal, @PathVariable int id) {

        String username = transferDao.getUsernameByAccountId(id);
        return username;
    }

    @GetMapping(path = "/account")
    public int getAccountId(Principal principal){
        //Gets current user's account Id

        int accountId = transferDao.getUserAccountId(principal.getName());
        return accountId;
    }


    @GetMapping(path = "/account/{username}")
    public int getAccountIdByUsername(@PathVariable String username) {
        int accountId = transferDao.getUserAccountId(username);
        return accountId;
    }

    @GetMapping (path = "/usermap")
    public Map<Integer, String> getUserMap() {
        Map<Integer, String> userMap = userDao.getUserMap();
        //run method to create HashMap

        return userMap;
    }


}
