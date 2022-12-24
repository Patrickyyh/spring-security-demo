package com.yeyuhao.springsecurityclient.controller;
import com.yeyuhao.springsecurityclient.entity.User;
import com.yeyuhao.springsecurityclient.entity.VerificationToken;
import com.yeyuhao.springsecurityclient.event.RegistrationCompleteEvent;
import com.yeyuhao.springsecurityclient.model.PasswordModel;
import com.yeyuhao.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yeyuhao.springsecurityclient.model.UserModel;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request){
        // use userSerivce to save record
        User user = userService.registerUser(userModel);

        // publish the event to send the verification link
        publisher.publishEvent(new RegistrationCompleteEvent(user , applicationUrl(request)));
        return "Success";
    }


    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token){

       String result = userService.validVerificationToken(token);
       if(result.equalsIgnoreCase("valid")){
           return "User Verifies successfully";
       }
       return "Bad User";
    }


    @GetMapping("/resendVerifyToken")
    public String resendVerification(@RequestParam("token") String oldToken,
                                     HttpServletRequest request){
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user , applicationUrl(request) ,  verificationToken);
        return "Verification Link Sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel,
                                 HttpServletRequest request){
        User user =  userService.findUserByEmail(passwordModel.getEmail());
        String url = "";
        if(user != null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetToken(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request),token);
        }
        return url;

    }

    @PostMapping("/login")
    public HashMap<String ,String> loginUser(@RequestBody UserModel userModel){
        boolean passwordCompareResult = userService.comparePassword(userModel);
        if(passwordCompareResult){
//            return "Password Correct !"
            User user = userService.findUserByEmail(userModel.getEmail());
            HashMap<String ,String> returnMap = new HashMap<>();
            returnMap.put("email" , user.getEmail());
            returnMap.put("lastName" , user.getLastName());
            returnMap.put("firstName" , user.getFirstName());

            return returnMap;

        }else{
            HashMap<String ,String> returnMap = new HashMap<>();
            returnMap.put("error-message" , "Sorry, the password is incorrect");
            return returnMap;
        }

    }


    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel){

        // Verify the token
        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")){
            return "Invalid Token";
        }

        // update password
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.updatePassword(user.get(),passwordModel.getNewPassword());
            return "Password Reset successfully";
        }else{
            return "Invalid Token";
        }

    }


    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl
                + "/savePassword?token="
                + token;
        log.info("Click the link to Reset: {}",url);
        return url;
    }

    private void resendVerificationTokenMail(User user, String applicationUrl ,VerificationToken verificationToken ) {
        // Send Mail to User
        String url = applicationUrl +
                "/verifyRegistration?token="
                + verificationToken.getToken() ;
        // Log the link on the console.
        log.info("Click the link to verify your account: {}",url);

    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }


}
