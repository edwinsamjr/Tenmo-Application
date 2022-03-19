package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferDetails extends Transfer {

    private String senderUsername;
    private String receiverUsername;
    private String transferTypeName;
    private String transferStatusName;


    public TransferDetails() {

    }

    public TransferDetails(int transfer_id, int transfer_type_id, int transfer_status_id, int account_from, int account_to, BigDecimal amount) {
        super(transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount);
    }

    public TransferDetails(int transfer_id, int transfer_type_id, int transfer_status_id, int account_from, int account_to, BigDecimal amount, String senderUsername, String receiverUsername) {
        super(transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount);
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        if (transfer_type_id == 1) {
            this.transferTypeName = "Request";
        } else if (transfer_type_id == 2) {
            this.transferTypeName = "Send";
        }
        this.transferStatusName = transferStatusName;
        if(transfer_status_id == 1){
            this.transferStatusName = "Pending";
        } else if(transfer_status_id == 2){
            this.transferStatusName = "Approved";
        } else if(transfer_status_id == 3){
            this.transferStatusName = "Rejected";
        }
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getTransferTypeName() {
        return transferTypeName;
    }

    public void setTransferTypeName(String transferTypeName) {
        this.transferTypeName = transferTypeName;
    }

    public String getTransferStatusName() {
        return transferStatusName;
    }

    public void setTransferStatusName(String transferStatusName) {
        this.transferStatusName = transferStatusName;
    }
}
