package com.yeyuhao.springsecurityclient.service;
import com.yeyuhao.springsecurityclient.entity.User;
import com.yeyuhao.springsecurityclient.model.UserModel;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerficationTokenForUser(String token,User user);

    String validVerificationToken(String token);
}
