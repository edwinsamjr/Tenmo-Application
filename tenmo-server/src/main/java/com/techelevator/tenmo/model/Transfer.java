package com.techelevator.tenmo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class Transfer {


    private int transfer_id;
    private int transfer_type;
    private int transfer_status_id;
    @Positive(message = "Sender cannot be blank.")
    private int account_from;
    @Positive(message = "Receiver cannot be blank.")
    private int account_to;
    @Positive(message = "Transfer amount must be positive.")
    private BigDecimal amount;



    public Transfer() {

    }

    public Transfer(int transfer_id, int transfer_type, int transfer_status_id, int account_from, int account_to, BigDecimal amount){
        this.transfer_id = transfer_id;
        this.transfer_type = transfer_type;
        this.transfer_status_id = transfer_status_id;
        this.account_from = account_from;
        this.account_to = account_to;
        this.amount = amount;
    }

    public int getTransfer_id() {
        return transfer_id;
    }

    public void setTransfer_id(int transfer_id) {
        this.transfer_id = transfer_id;
    }

    public int getTransfer_type() {
        return transfer_type;
    }

    public void setTransfer_type(int transfer_type) {
        this.transfer_type = transfer_type;
    }

    public int getTransfer_status_id() {
        return transfer_status_id;
    }

    public void setTransfer_status_id(int transfer_status_id) {
        this.transfer_status_id = transfer_status_id;
    }

    public int getAccount_from() {
        return account_from;
    }

    public void setAccount_from(int account_from) {
        this.account_from = account_from;
    }

    public int getAccount_to() {
        return account_to;
    }

    public void setAccount_to(int account_to) {
        this.account_to = account_to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    }
