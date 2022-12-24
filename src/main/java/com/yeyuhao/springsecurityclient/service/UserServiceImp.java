package com.yeyuhao.springsecurityclient.service;

import com.yeyuhao.springsecurityclient.entity.PasswordResetToken;
import com.yeyuhao.springsecurityclient.entity.User;
import com.yeyuhao.springsecurityclient.entity.VerificationToken;
import com.yeyuhao.springsecurityclient.repository.PasswordResetTokenRepository;
import com.yeyuhao.springsecurityclient.repository.UserRepository;
import com.yeyuhao.springsecurityclient.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.yeyuhao.springsecurityclient.model.UserModel;

import java.beans.Encoder;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImp implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

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
    public User findUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user;
    }

    @Override
    public void saveVerficationTokenForUser(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(user, token);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        // Find the old token
        VerificationToken verificationToken
                = verificationTokenRepository.findByToken(oldToken);
        //Generate a new token and save the token
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken ;

    }


    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(
                passwordResetTokenRepository
                .findByToken(token)
                .getUser());

    }

    @Override
    public void createPasswordResetToken(User user, String token) {
        PasswordResetToken  passwordResetToken
                 = new PasswordResetToken(user,token);

        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken
                = passwordResetTokenRepository.findByToken(token);

        if(passwordResetToken == null){
            return "invalid";
        }

        // Check if the token is expired or not
        User user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();
        if((passwordResetToken.getExpirationTime().getTime() - cal.getTime().getTime()) <=0){
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }
        return "valid";
    }

    @Override
    public String validVerificationToken(String token) {
        // Find the specific token
        VerificationToken findVerificationToken
                = verificationTokenRepository.findByToken(token);
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

    @Override
    public boolean comparePassword(UserModel userModel) {
        User user =  userRepository.findByEmail(userModel.getEmail());
        String password = user.getPassword();
        return passwordEncoder.matches(userModel.getPassword() , password);
    }
}
