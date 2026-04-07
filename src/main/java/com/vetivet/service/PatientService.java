package com.vetivet.service;

import com.vetivet.dto.PatientDTO;
import com.vetivet.model.Owner;
import com.vetivet.model.Patient;
import com.vetivet.repository.OwnerRepository;
import com.vetivet.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final OwnerRepository ownerRepository;

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
        return toDTO(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByOwner(Long ownerId) {
        return patientRepository.findByOwnerIdAndActiveTrue(ownerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> searchPatients(String search) {
        return patientRepository.searchPatients(search).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientDTO createPatient(PatientDTO dto) {
        Owner owner = ownerRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Dueño no encontrado con ID: " + dto.getOwnerId()));

        Patient patient = toEntity(dto, owner);
        patient = patientRepository.save(patient);
        return toDTO(patient);
    }

    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));

        patient.setName(dto.getName());
        patient.setSpecies(dto.getSpecies());
        patient.setBreed(dto.getBreed());
        patient.setGender(dto.getGender());
        patient.setBirthDate(dto.getBirthDate());
        patient.setWeight(dto.getWeight());
        patient.setColor(dto.getColor());
        patient.setMedicalNotes(dto.getMedicalNotes());
        patient.setMicrochipNumber(dto.getMicrochipNumber());

        // Change owner if needed
        if (!patient.getOwner().getId().equals(dto.getOwnerId())) {
            Owner newOwner = ownerRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("Dueño no encontrado con ID: " + dto.getOwnerId()));
            patient.setOwner(newOwner);
        }

        patient = patientRepository.save(patient);
        return toDTO(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
        // Soft delete
        patient.setActive(false);
        patientRepository.save(patient);
    }

    private PatientDTO toDTO(Patient p) {
        return PatientDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .species(p.getSpecies())
                .breed(p.getBreed())
                .gender(p.getGender())
                .birthDate(p.getBirthDate())
                .weight(p.getWeight())
                .color(p.getColor())
                .medicalNotes(p.getMedicalNotes())
                .microchipNumber(p.getMicrochipNumber())
                .ownerId(p.getOwner().getId())
                .ownerName(p.getOwner().getFirstName() + " " + p.getOwner().getLastName())
                .ownerPhone(p.getOwner().getPhone())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private Patient toEntity(PatientDTO dto, Owner owner) {
        return Patient.builder()
                .name(dto.getName())
                .species(dto.getSpecies())
                .breed(dto.getBreed())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .weight(dto.getWeight())
                .color(dto.getColor())
                .medicalNotes(dto.getMedicalNotes())
                .microchipNumber(dto.getMicrochipNumber())
                .owner(owner)
                .active(true)
                .build();
    }
}
