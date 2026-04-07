package com.vetivet.service;

import com.vetivet.dto.OwnerDTO;
import com.vetivet.model.Owner;
import com.vetivet.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;

    @Transactional(readOnly = true)
    public List<OwnerDTO> getAllOwners() {
        return ownerRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OwnerDTO getOwnerById(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dueño no encontrado con ID: " + id));
        return toDTO(owner);
    }

    @Transactional(readOnly = true)
    public List<OwnerDTO> searchOwners(String search) {
        return ownerRepository.searchOwners(search).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OwnerDTO createOwner(OwnerDTO dto) {
        if (ownerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        Owner owner = toEntity(dto);
        owner = ownerRepository.save(owner);
        return toDTO(owner);
    }

    @Transactional
    public OwnerDTO updateOwner(Long id, OwnerDTO dto) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dueño no encontrado con ID: " + id));

        owner.setFirstName(dto.getFirstName());
        owner.setLastName(dto.getLastName());
        owner.setPhone(dto.getPhone());
        owner.setAddress(dto.getAddress());
        owner.setDocumentId(dto.getDocumentId());

        // Email change needs uniqueness check
        if (!owner.getEmail().equals(dto.getEmail())) {
            if (ownerRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("El email ya está registrado.");
            }
            owner.setEmail(dto.getEmail());
        }

        owner = ownerRepository.save(owner);
        return toDTO(owner);
    }

    @Transactional
    public void deleteOwner(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dueño no encontrado con ID: " + id));
        // Soft delete
        owner.setActive(false);
        ownerRepository.save(owner);
    }

    private OwnerDTO toDTO(Owner o) {
        return OwnerDTO.builder()
                .id(o.getId())
                .firstName(o.getFirstName())
                .lastName(o.getLastName())
                .email(o.getEmail())
                .phone(o.getPhone())
                .address(o.getAddress())
                .documentId(o.getDocumentId())
                .active(o.isActive())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private Owner toEntity(OwnerDTO dto) {
        return Owner.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .documentId(dto.getDocumentId())
                .active(true)
                .build();
    }
}
