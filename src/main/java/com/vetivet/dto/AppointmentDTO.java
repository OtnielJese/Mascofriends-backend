package com.vetivet.dto;

import com.vetivet.model.Appointment.AppointmentStatus;
import com.vetivet.model.Appointment.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppointmentDTO {

    private Long id;

    @NotNull(message = "El paciente es requerido")
    private Long patientId;

    @NotNull(message = "El dueño es requerido")
    private Long ownerId;

    @NotNull(message = "La fecha es requerida")
    @Future(message = "La fecha debe ser en el futuro")
    private LocalDateTime appointmentDate;

    @NotNull(message = "El tipo de cita es requerido")
    private AppointmentType type;

    private AppointmentStatus status;

    @Size(max = 500)
    private String notes;

    // Populated on response
    private String patientName;
    private String ownerName;
    private String ownerPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
