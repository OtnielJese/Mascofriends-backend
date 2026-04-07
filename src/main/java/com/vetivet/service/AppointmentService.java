package com.vetivet.service;

import com.vetivet.dto.AppointmentDTO;
import com.vetivet.model.*;
import com.vetivet.model.Appointment.AppointmentStatus;
import com.vetivet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final OwnerRepository ownerRepository;

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        return toDTO(apt);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByOwner(Long ownerId) {
        return appointmentRepository.findByOwnerIdOrderByAppointmentDateDesc(ownerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateAsc(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + dto.getPatientId()));
        Owner owner = ownerRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Dueño no encontrado con ID: " + dto.getOwnerId()));

        Appointment apt = Appointment.builder()
                .patient(patient)
                .owner(owner)
                .appointmentDate(dto.getAppointmentDate())
                .type(dto.getType())
                .status(AppointmentStatus.PENDING)
                .notes(dto.getNotes())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .ownerPhone(owner.getPhone())
                .build();

        apt = appointmentRepository.save(apt);
        return toDTO(apt);
    }

    @Transactional
    public AppointmentDTO updateAppointment(Long id, AppointmentDTO dto) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        apt.setAppointmentDate(dto.getAppointmentDate());
        apt.setType(dto.getType());
        if (dto.getStatus() != null) {
            apt.setStatus(dto.getStatus());
        }
        apt.setNotes(dto.getNotes());

        // Update patient if changed
        if (!apt.getPatient().getId().equals(dto.getPatientId())) {
            Patient patient = patientRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + dto.getPatientId()));
            apt.setPatient(patient);
        }

        // Update owner if changed
        if (!apt.getOwner().getId().equals(dto.getOwnerId())) {
            Owner owner = ownerRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("Dueño no encontrado con ID: " + dto.getOwnerId()));
            apt.setOwner(owner);
            apt.setOwnerName(owner.getFirstName() + " " + owner.getLastName());
            apt.setOwnerPhone(owner.getPhone());
        }

        apt = appointmentRepository.save(apt);
        return toDTO(apt);
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        appointmentRepository.delete(apt);
    }

    @Transactional
    public AppointmentDTO updateStatus(Long id, AppointmentStatus status) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        apt.setStatus(status);
        apt = appointmentRepository.save(apt);
        return toDTO(apt);
    }

    private AppointmentDTO toDTO(Appointment a) {
        return AppointmentDTO.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .ownerId(a.getOwner().getId())
                .appointmentDate(a.getAppointmentDate())
                .type(a.getType())
                .status(a.getStatus())
                .notes(a.getNotes())
                .patientName(a.getPatient().getName())
                .ownerName(a.getOwnerName())
                .ownerPhone(a.getOwnerPhone())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
