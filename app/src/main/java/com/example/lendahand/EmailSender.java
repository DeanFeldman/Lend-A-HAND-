package com.example.lendahand;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailSender {
    //lendahandconfirmation@gmail.com
    //Email@Contact1
    //app password: attm tzsu usem htej


    private String username = "lendahandconfirmation@gmail.com";
    private String password = "attm tzsu usem htej";
    private String host = "127.0.0.1";


    public EmailSender() {
        this.username = username;
        this.password = password;
    }

    public EmailSender(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "LendAHand Support"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Mail successfully sent");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.println("Encoding error when setting sender name: " + e.getMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("SMTP Error: " + e.getMessage());
        }

    }
    }


