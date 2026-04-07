package com.vetivet.controller;

import com.vetivet.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    /**
     * Generate a custom WhatsApp link.
     * The phone number stays server-side for security.
     */
    @GetMapping("/link")
    public ResponseEntity<Map<String, String>> getWhatsAppLink(
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String appointmentType,
            @RequestParam(required = false) String petName) {
        String link = whatsAppService.generateWhatsAppLink(message, appointmentType, petName);
        return ResponseEntity.ok(Map.of("link", link));
    }

    /**
     * Quick link for bath/grooming appointments
     */
    @GetMapping("/bath")
    public ResponseEntity<Map<String, String>> getBathAppointmentLink() {
        String link = whatsAppService.generateBathAppointmentLink();
        return ResponseEntity.ok(Map.of("link", link));
    }

    /**
     * Quick link for consultation appointments
     */
    @GetMapping("/consultation")
    public ResponseEntity<Map<String, String>> getConsultationLink() {
        String link = whatsAppService.generateConsultationLink();
        return ResponseEntity.ok(Map.of("link", link));
    }
}
