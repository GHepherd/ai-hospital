package com.gjh.xiaozi.controller;

import com.gjh.xiaozi.dto.MessageDTO;
import com.gjh.xiaozi.entity.Appointment;
import com.gjh.xiaozi.service.AppointmentService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {


    @Value("classpath:templates/zhaozhi-prompt-template.txt")
    private Resource template;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/chat")
    public String chat(@RequestBody MessageDTO dto) {
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("current_date", new Date()));
        return chatClient.prompt(prompt)
                .user(dto.getMessage())
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,dto.getChatId()))
                .call().content();
    }

    @PostMapping("/bookAppointment")
    public Appointment bookAppointment(@RequestBody Appointment appointment) {
        return appointmentService.checkExist(appointment);
    }
}
