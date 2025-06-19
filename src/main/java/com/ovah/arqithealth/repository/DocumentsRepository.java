package com.ovah.arqithealth.repository;

import com.ovah.arqithealth.model.SharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentsRepository extends JpaRepository<SharedDocument, UUID> {

}
