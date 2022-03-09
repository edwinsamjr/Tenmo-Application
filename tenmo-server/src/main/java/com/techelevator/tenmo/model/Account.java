package com.techelevator.tenmo.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class Account {

    @NotBlank(message = "Account ID cannot be blank.")
    private int account_id;
    @NotBlank(message = "User ID cannot be blank.")
    private int user_id;
    @Positive(message = "Balance must be positive.")
    private BigDecimal balance;

    public int getAccount_id() {
        return account_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
