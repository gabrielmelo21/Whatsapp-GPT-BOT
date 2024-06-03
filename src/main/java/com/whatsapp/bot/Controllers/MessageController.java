package com.whatsapp.bot.Controllers;


import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.whatsapp.bot.Services.ChatGPTService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {



    @Autowired
    ChatGPTService chatGPTService;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;


    @Setter
    @Getter
    @AllArgsConstructor
    public static class MessageRequest {
        private String to;
        private String body;
    }


    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageRequest messageRequest) {
        Message message = Message.creator(
                new PhoneNumber("whatsapp:" + messageRequest.getTo()),
                new PhoneNumber(fromPhoneNumber),
                messageRequest.getBody()).create();

        return message.getSid();
    }


    @PostMapping("/receive")
    public void receiveMessage(@RequestParam Map<String, String> requestParams) {
        String from = requestParams.get("From");
        String body = requestParams.get("Body");

        System.out.println("Received message from " + from + ": " + body);


        // Remove o prefixo "whatsapp:" do número de telefone
        if (from != null && from.startsWith("whatsapp:")) {
            from = from.substring(9); // Remove "whatsapp:"
        }


        String chatGPTResponse = chatGPTService.gptConnection(body);

        System.out.println("ChatGpt: " + chatGPTResponse);

        MessageRequest messageRequest = new MessageRequest(from, chatGPTResponse);

        String response = sendMessage(messageRequest);

        System.out.println(response);


    }


}