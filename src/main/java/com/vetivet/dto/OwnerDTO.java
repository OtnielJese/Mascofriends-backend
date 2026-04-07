package com.vetivet.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OwnerDTO {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "El teléfono es requerido")
    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String documentId;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
