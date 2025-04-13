package com.springboot.resume.extractor.repository;

import com.springboot.resume.extractor.model.Resume;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Query("SELECT r FROM Resume r WHERE LOWER(r.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Resume> searchResumes(@Param("keyword") String keyword, Pageable pageable);

    Page<Resume> findByContentContainingIgnoreCaseOrSkillsContainingIgnoreCaseOrExperienceContainingIgnoreCaseOrEducationContainingIgnoreCase(
        String content, String skills, String experience, String education, Pageable pageable);

        Optional<Resume> findByOriginalFileName(String fileName);
}
