package com.yeyuhao.springsecurityclient.event.listener;
import com.yeyuhao.springsecurityclient.entity.User;
import com.yeyuhao.springsecurityclient.event.RegistrationCompleteEvent;
import com.yeyuhao.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener
        implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    public UserService userService;


    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        // Create the verification Token for the user with the Link
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerficationTokenForUser(token,user);

        // Send Mail to User
        String url = event.getApplicationUrl() +
                "/verifyRegistration?token="+
                token;

        //
        log.info("Click the link to verify your account: {}",url);
    }
}
