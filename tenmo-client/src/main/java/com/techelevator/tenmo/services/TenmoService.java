package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDetails;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import io.cucumber.java.sl.In;
import org.apiguardian.api.API;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TenmoService {

    private static final String API_BASE_URL = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();
    private ConsoleService consoleService = new ConsoleService();

    private String authToken;

    private AuthenticatedUser currentUser;

    public void setCurrentUser(AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Transfer[] findUserTransfers() {
        //Gets all transfer for current user

        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "viewtransfers",
                    HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public TransferDetails[] findUserTransferDetails() {
        //Gets all transfer details for current user

        TransferDetails[] transferDetails = null;
        try {
            ResponseEntity<TransferDetails[]> response = restTemplate.exchange(API_BASE_URL + "viewtransferdetails",
                    HttpMethod.GET, makeAuthEntity(), TransferDetails[].class);
            transferDetails = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transferDetails;
    }

    public TransferDetails[] findUserRequests() {
        //Gets all requests for current user

        TransferDetails[] requests = null;
        try {
            ResponseEntity<TransferDetails[]> response = restTemplate.exchange(API_BASE_URL + "listrequests",
                    HttpMethod.GET, makeAuthEntity(), TransferDetails[].class);
            requests = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return requests;
    }

    public User[] getUserList() {
        //Lists all users

        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL + "users",
                    HttpMethod.GET, makeAuthEntity(), User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    public int getAccountIdByUsername() {
        //Gets account Id for current user
        int accountId = -1;

        try {
            ResponseEntity<Integer> response = restTemplate.exchange(API_BASE_URL + "account",
                    HttpMethod.GET, makeAuthEntity(), int.class);
            accountId = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }

        return accountId;
    }

    public int getAccountIdByUsername(String username) {
        //Gets account Id given a username
        int accountId = -1;

        try {
            ResponseEntity<Integer> response = restTemplate.exchange(API_BASE_URL + "account/" + username,
                    HttpMethod.GET, makeAuthEntity(), int.class);
            accountId = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }

        return accountId;
    }


    public BigDecimal getBalance() {
        //Gets current user's balance

        BigDecimal balance = new BigDecimal("0.00");
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL + "balance",
                    HttpMethod.GET, makeAuthEntity(), BigDecimal.class);
            balance = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }

        return balance;
    }

    public boolean transfer(Transfer transfer) {
        //Check user has sufficient funds and positive transfer amount
        //Completes transfer

        try {
            boolean hasSufficientFunds = getBalance().compareTo(transfer.getAmount()) >= 0;
            boolean positiveTransferAmount = transfer.getAmount().compareTo(new BigDecimal("0.00")) > 0;
            if (!hasSufficientFunds || !positiveTransferAmount) {
                throw new IllegalArgumentException();
            }
            restTemplate.exchange(API_BASE_URL + "send", HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
            return true;
        } catch (RestClientResponseException | ResourceAccessException | NullPointerException e) {
            BasicLogger.log(e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("Invalid Transfer Amount");
            return false;
        }
    }

    public void request(Transfer transfer) {
        try {
            boolean positiveTransferAmount = transfer.getAmount().compareTo(new BigDecimal("0.00")) > 0;
            if (!positiveTransferAmount) {
                throw new IllegalArgumentException();
            }
            restTemplate.exchange(API_BASE_URL + "request", HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("Invalid Transfer Amount");
        }
    }


    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.authToken);
        return new HttpEntity<>(transfer, headers);
    }

    public void printUserList(User[] users) {
        System.out.println("'''");
        System.out.println("-------------------------------------------");
        System.out.println("Users");
        System.out.println("ID          Name");
        System.out.println("-------------------------------------------");
        for (User user : users) {
            System.out.printf("%-12d%s%n", user.getId(), user.getUsername());
        }
        System.out.println("---------");
        System.out.println();
    }

    public void sendBucks() {
        try {
            User[] users = getUserList();
            printUserList(users);

            int userSelection = consoleService.promptForInt("Enter ID of user you are sending to (0 to cancel): ");
            User selectedUser = null;
            for (User user : users) {
                if (user.getId() == userSelection) {
                    selectedUser = user;
                }
            }


            Transfer transfer = new Transfer();

            if (userSelection != 0) {
                BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
                int senderAccount = getAccountIdByUsername();
                int receiverAccount = getAccountIdByUsername(selectedUser.getUsername());
                transfer = new Transfer(2, 2, senderAccount, receiverAccount, amount);
            }

            boolean isValidTransfer = transfer(transfer);
            System.out.println();

            if (isValidTransfer) {
                System.out.println("You have sent $" + transfer.getAmount() + " to " + selectedUser.getUsername());
            }
        } catch (NullPointerException e) {
            System.out.println("Invalid User ID");
        }
    }


    public void requestBucks() {
        try {
            User[] users = getUserList();
            printUserList(users);
            int userSelection = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel): ");
            User selectedUser = null;
            for (User user : users) {
                if (user.getId() == userSelection) {
                    selectedUser = user;
                }
            }

            Transfer transfer;

            if (userSelection != 0) {
                BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
                int receiverAccount = getAccountIdByUsername();
                int senderAccount = getAccountIdByUsername(selectedUser.getUsername());
                transfer = new Transfer(1, 1, senderAccount, receiverAccount, amount);

                request(transfer);
                System.out.println();
                System.out.println("You have requested $" + transfer.getAmount() + " from " + selectedUser.getUsername());
            }

        } catch (NullPointerException e) {
            System.out.println("Invalid User ID");
        }
    }

    public void viewTransfers() {
        System.out.println();
        System.out.println("'''");
        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.println("ID          From/To                 Amount");
        System.out.println("-------------------------------------------");


        TransferDetails[] transfers = findUserTransferDetails();


        for (TransferDetails transfer : transfers) {
            int transferId = transfer.getTransfer_id();
            String senderName = transfer.getSenderUsername();
            String receiverName = transfer.getReceiverUsername();
            BigDecimal amount = transfer.getAmount();

            String fieldToPrint = null;


            boolean currentUserIsSender = currentUser.getUser().getUsername().equals(transfer.getSenderUsername());


            //if current user is the sender, write To and receiver's name
            if (currentUserIsSender) {
                fieldToPrint = "To:    " + receiverName;
            }
            //else if current is the receiver, write from and sender's name
            else if (!currentUserIsSender) {
                fieldToPrint = "From:  " + senderName;
            }

            System.out.printf("%-11d %-22s $%7.2f %n", transferId, fieldToPrint, amount);
        }

        int userSelection = 0;
        if (transfers.length != 0) {
            System.out.println();
            userSelection = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
        }

        if (userSelection != 0) {

            for (TransferDetails transfer : transfers) {


                if (transfer.getTransfer_id() == userSelection) {
                    String senderName = transfer.getSenderUsername();
                    String receiverName = transfer.getReceiverUsername();
                    transfer.printDetails(senderName, receiverName, transfer.getTransferTypeName(), transfer.getTransferStatusName());
                }

            }


        }
    }

    public void viewRequests() {
        System.out.println();
        System.out.println("'''");
        System.out.println("-------------------------------------------");
        System.out.println("Requests");
        System.out.println("ID          From/To                 Amount");
        System.out.println("-------------------------------------------");


        TransferDetails[] transfers = findUserRequests();


        for (TransferDetails transfer : transfers) {
            int transferId = transfer.getTransfer_id();
            String receiverName = transfer.getReceiverUsername();
            BigDecimal amount = transfer.getAmount();

            String fieldToPrint = null;

            boolean currentUserIsSender = currentUser.getUser().getUsername().equals(transfer.getSenderUsername());

            //if current user is the sender, write To and receiver's name
            fieldToPrint = "To:    " + receiverName;

            System.out.printf("%-11d %-22s $%7.2f %n", transferId, fieldToPrint, amount);
        }

        int userSelection = 0;
        if (transfers.length != 0) {
            System.out.println();
            userSelection = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): ");
        }
        int userChoice = -1;

        if (userSelection != 0) {
            boolean foundTransfer = false;

            for (TransferDetails transfer : transfers) {

                if (transfer.getTransfer_id() == userSelection) {
                    userChoice = approveOrRejectPendingTransfer();

                    if (userChoice == 1) {
                        approveRequest(transfer);
                    } else if (userChoice == 2) {
                        rejectRequest(transfer);
                    } else {
                        System.out.println("Request still pending.");
                    }

                    foundTransfer = true;
                }

            }

            if (!foundTransfer) {
                System.out.println("Invalid Transfer Id");
            }

        }


    }

    public void approveRequest(TransferDetails transfer) {
        try {
            restTemplate.exchange(API_BASE_URL + "approve/" + transfer.getTransfer_id(), HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
            System.out.println();
            System.out.println("You have sent $" + transfer.getAmount() + " to " + transfer.getReceiverUsername());
            System.out.println();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("Invalid Transfer Amount");
        }
    }

    public void rejectRequest(Transfer transfer) {
        try {
            restTemplate.exchange(API_BASE_URL + "reject/" + transfer.getTransfer_id(), HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
            System.out.println();
            System.out.println("You have rejected transfer #" + transfer.getTransfer_id());
            System.out.println();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("Invalid Transfer Amount");
        }
    }

    public int approveOrRejectPendingTransfer() {
        System.out.println("'''");
        System.out.println("1: Approve");
        System.out.println("2: Reject");
        System.out.println("0: Don't approve or reject");
        System.out.println("---------");
        int userSelection = consoleService.promptForInt("Please choose an option: ");
        return userSelection;
    }


    public void viewCurrentBalance(BigDecimal balance) {
        System.out.println("```");
        System.out.println("Your current account balance is: $" + balance);
        System.out.println("```");
        System.out.println();
    }


}


