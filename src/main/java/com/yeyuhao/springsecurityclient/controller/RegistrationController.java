package com.yeyuhao.springsecurityclient.controller;
import com.yeyuhao.springsecurityclient.entity.User;
import com.yeyuhao.springsecurityclient.entity.VerificationToken;
import com.yeyuhao.springsecurityclient.event.RegistrationCompleteEvent;
import com.yeyuhao.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import com.yeyuhao.springsecurityclient.model.UserModel;

import javax.servlet.http.HttpServletRequest;
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
