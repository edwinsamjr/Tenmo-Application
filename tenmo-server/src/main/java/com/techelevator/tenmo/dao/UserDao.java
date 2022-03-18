package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.util.List;
import java.util.Map;

public interface UserDao {

    List<User> findAll();

    //Added
    List<User> findSendableUsers(String username);

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    Map<Integer, String> getUserMap();

}
