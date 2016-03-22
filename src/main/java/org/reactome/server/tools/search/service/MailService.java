package org.reactome.server.tools.search.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

/**
 * Mail Service
 * Created by gsviteri on 15/10/2015.
 */
@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private MailSender mailSender; // MailSender interface defines a strategy
    // for sending simple mails

    public void send(String toAddress, String fromAddress, String subject, String msgBody, Boolean sendEmailCopy) throws Exception {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(fromAddress);
            simpleMailMessage.setTo(toAddress);

            if (sendEmailCopy){
                simpleMailMessage.setBcc(fromAddress);
            }

            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(msgBody);

            mailSender.send(simpleMailMessage);
        }catch (Exception e){
            logger.error("[MAILSRVErr] The email could not be sent [To: " + toAddress + " From: " + fromAddress + " Subject: " + subject);
            throw new Exception("Mail has not been sent");
        }
    }

}
