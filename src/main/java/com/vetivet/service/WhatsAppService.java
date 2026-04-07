package com.vetivet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class WhatsAppService {

    @Value("${app.whatsapp.number}")
    private String whatsappNumber;

    @Value("${app.whatsapp.message}")
    private String defaultMessage;

    /**
     * Generates a WhatsApp redirect link for appointment scheduling.
     * The phone number is stored securely server-side.
     * 
     * @param message Custom message (optional)
     * @param appointmentType Type of appointment (optional)
     * @param petName Pet name (optional)
     * @return WhatsApp API link
     */
    public String generateWhatsAppLink(String message, String appointmentType, String petName) {
        StringBuilder messageBuilder = new StringBuilder();

        if (message != null && !message.isBlank()) {
            messageBuilder.append(message);
        } else {
            messageBuilder.append(defaultMessage);
        }

        if (appointmentType != null && !appointmentType.isBlank()) {
            messageBuilder.append("\n\n📋 Tipo de cita: ").append(appointmentType);
        }

        if (petName != null && !petName.isBlank()) {
            messageBuilder.append("\n🐾 Mascota: ").append(petName);
        }

        String encodedMessage = URLEncoder.encode(messageBuilder.toString(), StandardCharsets.UTF_8);
        String cleanNumber = whatsappNumber.replaceAll("[^0-9]", "");

        return String.format("https://wa.me/%s?text=%s", cleanNumber, encodedMessage);
    }

    /**
     * Quick link for bath/grooming appointment
     */
    public String generateBathAppointmentLink() {
        return generateWhatsAppLink(
                "Hola! Me gustaría agendar una cita de baño y corte para mi mascota.",
                "Baño y Corte",
                null
        );
    }

    /**
     * Quick link for consultation appointment
     */
    public String generateConsultationLink() {
        return generateWhatsAppLink(
                "Hola! Me gustaría agendar una consulta veterinaria.",
                "Consulta",
                null
        );
    }
}
