package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class TenmoService {

    private static final String API_BASE_URL = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Transfer[] findUserTransfers() {
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

    public String getUsernameByAccountId(int accountId) {
        String username = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_BASE_URL + "username/" + accountId,
                    HttpMethod.GET, makeAuthEntity(), String.class);
            username = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return username;
    }

    public int getAccountIdByUsername() {
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

    public void transfer(Transfer transfer) {
        try {
            boolean hasSufficientFunds = getBalance().compareTo(transfer.getAmount()) < 0;
            boolean positiveTransferAmount = transfer.getAmount().compareTo(new BigDecimal("0.00")) > 0;
            if (hasSufficientFunds || !positiveTransferAmount) {
                throw new IllegalArgumentException();
            }
            ResponseEntity<Transfer> response = restTemplate.exchange(API_BASE_URL + "transfer",
                    HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
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


    public User[] getUserList() {
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


}

/*
```
-------------------------------------------
Users
ID          Name
-------------------------------------------
313         Bernice
54          Larry
---------
 */
