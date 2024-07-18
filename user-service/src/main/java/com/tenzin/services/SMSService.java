package com.tenzin.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SMSService {
	
	@Value("${twilio.account-SID}")
	private String ACCOUNT_SID;
	
	@Value("${twilio.auth-token}")
	private String AUTH_TOKEN;
	
	@Value("${twilio.sms-number}")
	private String OUTGOING_SMS_NUMBER;
	
	public SMSService() {
		log.info("Sending SMS message with Twilio number: " + OUTGOING_SMS_NUMBER);
	}
	
	@PostConstruct
	private void setup() {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
	}
	
	@Async
	public String sendSMS(String smsNumber, String smsMessage) {
		try {
	        Message message = Message.creator(
	                new PhoneNumber(smsNumber), 
	                new PhoneNumber(OUTGOING_SMS_NUMBER),
	                smsMessage)
	                .create();
	        log.info("SMS sending : " + message.getBody().toString());
	        return message.getBody().toString();
	    } catch (Exception e) {
	        log.error("Error sending SMS: " + e.getMessage());
	        return "Failed to send SMS: " + e.getMessage();
	    }
	}
	
	
}
