package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    public Transfer[] findUserTransfers(){
        Transfer[] transfers = null;
        try{
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "viewtransfers",
                    HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public String getUsernameByAccountId(int accountId){
        String username = null;
        try{
            ResponseEntity<String> response = restTemplate.exchange(API_BASE_URL + "username/" + accountId,
                    HttpMethod.GET, makeAuthEntity(), String.class);
            username = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return username;
    }

    public int getAccountIdByUsername() {
        int accountId = -1;

        try{
            ResponseEntity<Integer> response = restTemplate.exchange(API_BASE_URL + "account" ,
                    HttpMethod.GET, makeAuthEntity(), int.class);
            accountId = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }

        return  accountId;
    }

    public BigDecimal getBalance(){
        BigDecimal balance = new BigDecimal("0.00");
        try{
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL + "balance",
                    HttpMethod.GET, makeAuthEntity(), BigDecimal.class);
            balance = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }

        return balance;
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        return new HttpEntity<>(headers);
    }

}
