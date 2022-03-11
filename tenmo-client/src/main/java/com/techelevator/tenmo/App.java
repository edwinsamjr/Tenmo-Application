package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.*;
import io.cucumber.java.bs.A;

import java.math.BigDecimal;
import java.util.Arrays;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    private final TenmoService tenmoService = new TenmoService();




    public static void main(String[] args) {

        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
                tenmoService.setAuthToken(currentUser.getToken());
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
        BigDecimal balance = tenmoService.getBalance();
        System.out.println("```");
        System.out.println("Your current account balance is: $" + balance);
        System.out.println("```");
        System.out.println();
	}

	private void viewTransferHistory() {
        System.out.println();
        System.out.println("'''");
        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.println("ID          From/To                 Amount");
        System.out.println("-------------------------------------------");


        Transfer[] transfers = tenmoService.findUserTransfers();



        for (Transfer transfer : transfers) {
            int transferId = transfer.getTransfer_id();
            String senderName = tenmoService.getUsernameByAccountId(transfer.getAccount_from());
            String receiverName = tenmoService.getUsernameByAccountId(transfer.getAccount_to());
            BigDecimal amount = transfer.getAmount();

            String fieldToPrint = null;

            int currentUserAccountId = tenmoService.getAccountIdByUsername();
            boolean currentUserIsSender = currentUserAccountId == transfer.getAccount_from();



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

            for (Transfer transfer : transfers) {


                if (transfer.getTransfer_id() == userSelection) {
                    String senderName = tenmoService.getUsernameByAccountId(transfer.getAccount_from());
                    String receiverName = tenmoService.getUsernameByAccountId(transfer.getAccount_to());
                    transfer.printDetails(senderName, receiverName, "Send", "Approved");
                }

            }


        }



		
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		
	}

	private void sendBucks() {
		// TODO Auto-generated method stub
		
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}

}
