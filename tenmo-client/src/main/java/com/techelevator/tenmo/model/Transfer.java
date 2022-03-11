package com.techelevator.tenmo.model;


import java.math.BigDecimal;

public class Transfer {

    private int transfer_id;

    private int transfer_type;

    private int transfer_status_id;

    private int account_from;

    private int account_to;

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


    public void printDetails(String senderName, String receiverName, String typeName, String statusName) {
        System.out.println();
        System.out.println("```");
        System.out.println("--------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("--------------------------------------------");
        System.out.println("Id: " + this.transfer_id);
        System.out.println("From: " + senderName);
        System.out.println("To: " + receiverName);
        System.out.println("Type: " + typeName);
        System.out.println("Status: " + statusName);
        System.out.println("Amount: $" + this.amount);
        System.out.println("```");
        System.out.println();

    }


}
