package com.vetivet.dto;

import com.vetivet.model.Patient.Gender;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientDTO {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "La especie es requerida")
    @Size(max = 50)
    private String species;

    @Size(max = 80)
    private String breed;

    private Gender gender;

    private LocalDate birthDate;

    @DecimalMin(value = "0.0", message = "El peso debe ser positivo")
    private BigDecimal weight;

    @Size(max = 100)
    private String color;

    @Size(max = 500)
    private String medicalNotes;

    @Size(max = 50)
    private String microchipNumber;

    @NotNull(message = "El dueño es requerido")
    private Long ownerId;

    // Populated on response
    private String ownerName;
    private String ownerPhone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
