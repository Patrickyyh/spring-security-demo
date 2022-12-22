package com.yeyuhao.springsecurityclient.service;

import com.yeyuhao.springsecurityclient.entity.User;
import com.yeyuhao.springsecurityclient.entity.VerificationToken;
import com.yeyuhao.springsecurityclient.repository.UserRepository;
import com.yeyuhao.springsecurityclient.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.yeyuhao.springsecurityclient.model.UserModel;

import java.util.Calendar;

@Service
public class UserServiceImp implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserModel userModel) {

        User user = new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
        return user;

    }

    @Override
    public void saveVerficationTokenForUser(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(user, token);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validVerificationToken(String token) {
        // Find the specific token
        VerificationToken findVerificationToken = verificationTokenRepository.findByToken(token);
        if(findVerificationToken == null){
            return "invalid";
        }

        // Get user object
        User user = findVerificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        // Check if time is expired
        if((findVerificationToken.getExpirationTime().getTime() - cal.getTime().getTime()) <=0){
            verificationTokenRepository.delete(findVerificationToken);
            return "expired";
        }

        // update and save user
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";

    }
}
