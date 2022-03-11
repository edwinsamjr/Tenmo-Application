package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.LoginResponse;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import org.apache.commons.logging.Log;
import org.springframework.web.client.RestTemplate;

public class LoginResponseService {

    RestTemplate restTemplate = new RestTemplate();

    public UserCredentials getUserCredentials() {
        String url = "http://localhost:8080/user";
        UserCredentials userCredentials = this.restTemplate.getForObject(url, UserCredentials.class);
        return userCredentials;
    }

    public LoginResponse getLoginResponse() {
        String url = "http://localhost:8080/login";
        UserCredentials userCredentials = new UserCredentials("ajohnson23", "password23");
        return this.restTemplate.postForObject(url, getUserCredentials(), LoginResponse.class);
    }



}
