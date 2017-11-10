package com.JustinThyme.justinthymer.controllers.TwilioReminder;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;



public class TwillSend {

    // Setup your own account for personal SID, TOKEN, and phone number
    //public static final String ACCOUNT_SID = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    //note original heroku twill error len    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //public static final String ACCOUNT_SID;// = String ACCOUNT_SID;
    public static final String ACCOUNT_SID = System.getenv("ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("AUTH_TOKEN");

    //public static final String  AUTH_TOKEN = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";



    public static void twill_away(String message, String number) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message twill_message = Message.creator(new PhoneNumber (number),//number to (receiving)
               // new PhoneNumber("XXXXXXXXXXX"),//number from (this will be your Twillio number)

                new PhoneNumber("5037838539"),

                message).create();//message from form and passed via model

        System.out.println(twill_message.getSid());
    }
}
