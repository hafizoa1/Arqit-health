package com.ovah.arqithealth.controller;

import com.ovah.arqithealth.model.DTO.RelationshipAcceptanceDTO;
import com.ovah.arqithealth.model.DTO.RelationshipStatusDTO;
import com.ovah.arqithealth.model.HospitalRelationship;
import com.ovah.arqithealth.model.DTO.RelationshipRequestDTO;
import com.ovah.arqithealth.service.HospitalRelationshipService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;



@RestController
@RequestMapping("/api/v1/ArqitHealth/relationships")
@AllArgsConstructor
public class HospitalRelationships {

    private final HospitalRelationshipService hospitalRelationshipService;


    @PostMapping("/initiate")
    public ResponseEntity<Void> initiateRelationship(
            @Valid @RequestBody RelationshipRequestDTO relationshipRequest) {
        return hospitalRelationshipService.initiateRelationship(relationshipRequest);
    }

    @PostMapping("/request-acceptance")
    public ResponseEntity<Void> requestAcceptance(
            @Valid @RequestBody RelationshipRequestDTO relationshipRequest) {
        return hospitalRelationshipService.handleIncomingInitiation(relationshipRequest);
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRelationship(
            @Valid @RequestBody RelationshipAcceptanceDTO requestDTO) {
        return hospitalRelationshipService.acceptRelationshipAndNotify(requestDTO);
    }

    @PostMapping("/decline")
    public ResponseEntity<Void> declineRelationship(
            @Valid @RequestBody RelationshipAcceptanceDTO requestDTO
    ) {
        return hospitalRelationshipService.declineRelationshipAndNotify(requestDTO);
    }

    @PostMapping("/notify-status")
    public ResponseEntity<Void> notifyStatus(
            @Valid @RequestBody RelationshipStatusDTO requestDTO
    ) {
        return hospitalRelationshipService.handleStatusNotification(requestDTO);
    }


    @GetMapping("/{id}")
    public ResponseEntity<HospitalRelationship> getHospitalRelationships(@PathVariable UUID id) {
        return hospitalRelationshipService.getRelationship(id);
    }

}
