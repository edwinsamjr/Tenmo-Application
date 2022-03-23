package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.TransferDetailsDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exceptions.*;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDetails;
import com.techelevator.tenmo.model.User;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
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
    TransferDetailsDao transferDetailsDao;

    public ApplicationController(TransferDao transferDao, UserDao userDao, TransferDetailsDao transferDetailsDao) {
        this.transferDetailsDao = transferDetailsDao;
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    @GetMapping (path = "/balance")
    @Operation (summary = "Returns current user's balance")
    public BigDecimal findBalance(Principal principal) {
        //Gets current user's balance
        return this.transferDao.getBalance(principal.getName());
    }


    @GetMapping(path = "/users")
    @Operation (summary = "Returns list who you can send money to")
    public List<User> listUsers(Principal principal) {
        //Lists users besides current user

        List<User> sendableUsers = this.userDao.findSendableUsers(principal.getName());
        return sendableUsers;

    }


    @GetMapping(path = "/viewtransfers")
    @Operation (summary = "Returns all completed incoming or outgoing transfers for current user")
    public List<Transfer> viewTransfers(Principal principal) {
        //Lists all transfers for current user

        List<Transfer> currentUserTransfers = this.transferDao.findUserTransfers(principal.getName());
        return currentUserTransfers;

    }

    @GetMapping(path = "/viewtransferdetails")
    @Operation (summary = "Returns current user's transfer with additional details")
    public List<TransferDetails> viewTransferDetails(Principal principal) {
        //Lists all transfers with details for current user

        List<TransferDetails> currentUserTransfers = this.transferDetailsDao.findUserTransferDetails(principal.getName());
        return currentUserTransfers;

    }

    @GetMapping(path = "/viewtransfers/{id}")
    @Operation (summary = "Gets transfer by transferId")
    public Transfer viewTransferById(Principal principal, @PathVariable int id) throws TransferNotFoundException {

        Transfer transfer = this.transferDao.findTransferById(principal.getName(), id);
        return transfer;
    }

    @GetMapping(path = "/viewtransferdetails/{id}")
    @Operation (summary = "Gets transfer by transferId with additional details")
    public TransferDetails viewTransferDetailsById(Principal principal, @PathVariable int id) throws TransferNotFoundException {

        TransferDetails transferDetails = this.transferDetailsDao.findTransferById(principal.getName(), id);
        return transferDetails;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/send")
    @Operation (summary = "Send money to another user")
    public void addTransfer(Principal principal, @Valid @RequestBody Transfer transfer) throws InvalidTransferException, AccountNotFoundException {
        //Enters Transfer into database

        this.transferDao.send(transfer, principal.getName());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/request")
    @Operation (summary = "Request money from another user")
    public void request(Principal principal, @Valid @RequestBody Transfer transfer) throws InvalidTransferException {

        this.transferDao.request(transfer, principal.getName());
    }


    @GetMapping(path = "/viewrequests")
    @Operation (summary = "View current user's pending requests")
    public List<Transfer> viewRequests(Principal principal) {
        return transferDao.viewRequests(principal.getName());
    }

    @GetMapping(path = "/listrequests")
    @Operation (summary = "View current user's pending requests with additional details")
    public List<TransferDetails> listRequests(Principal principal) {
        return transferDetailsDao.listRequests(principal.getName());
    }


    @PostMapping(path = "/approve/{id}")
    @Operation (summary = "Approve a transfer request by transferId")
    public void approveRequest(Principal principal, @PathVariable int id) throws InvalidTransferException {
        this.transferDao.approveRequest(id, principal.getName());
    }

    @PostMapping(path = "/reject/{id}")
    @Operation (summary = "Reject a transfer request by transferId")
    public void rejectRequest(Principal principal, @PathVariable int id) throws IllegalTranscationException {
        this.transferDao.rejectRequest(id, principal.getName());
    }

    @GetMapping(path = "/user")
    @Operation (summary = "Returns current logged in user")
    public User getUser(Principal principal) {
        //Gets current user

        User user = userDao.findByUsername(principal.getName());
        return user;
    }

    @GetMapping(path = "/account")
    @Operation (summary = "Returns current user's accountId")
    public int getAccountId(Principal principal) {
        //Gets current user's account Id

        int accountId = transferDao.getUserAccountId(principal.getName());
        return accountId;
    }


    @GetMapping(path = "/account/{username}")
    @Operation (summary = "Retrieves accountId by username")
    public int getAccountIdByUsername(@PathVariable String username) {
        int accountId = transferDao.getUserAccountId(username);
        return accountId;
    }

    @GetMapping(path = "/usermap")
    public Map<Integer, String> getUserMap() {
        Map<Integer, String> userMap = userDao.getUserMap();
        //run method to create HashMap

        return userMap;
    }


}
