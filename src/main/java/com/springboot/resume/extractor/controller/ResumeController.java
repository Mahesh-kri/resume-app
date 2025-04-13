package com.springboot.resume.extractor.controller;

import com.springboot.resume.extractor.model.Resume;
import com.springboot.resume.extractor.service.ResumeService;

import lombok.extern.log4j.Log4j2;

import com.springboot.resume.extractor.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;

@Controller
@Log4j2
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private AIService aiService;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<Resume>resumePage = resumeService.getAllResumes(PageRequest.of(page - 1, size));
        model.addAttribute("resumes", resumePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resumePage.getTotalPages());
        return "index";
    }

    @PostMapping("/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file, Model model) {
        try {
            if (file == null || file.isEmpty()) {
                model.addAttribute("error", "Please select a file to upload.");
                return "index";
            }

            if (!file.getContentType().equals("application/pdf")) {
                model.addAttribute("error", "Only PDF files are allowed.");
                return "index";
            }

            Resume resume = resumeService.processAndSaveResume(file);
            return "redirect:/view/" + resume.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error processing resume: " + e.getMessage());
            return "index";
        }
    }

    @GetMapping({"/resume/{id}", "/view/{id}"})
    public String viewResume(@PathVariable Long id, Model model) {
    Resume resume = resumeService.getResumeById(id);
    model.addAttribute("resume", resume);
    return "view";
}

@GetMapping("/search")
    @ResponseBody
    public Map<String, Object> searchResumes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Resume> resumePage = resumeService.searchResumes(keyword, PageRequest.of(page - 1, size));
        Map<String, Object> response = new HashMap<>();
        response.put("resumes", resumePage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", resumePage.getTotalPages());
        return response;
    }


    @DeleteMapping("/resume/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        try {
            resumeService.deleteResume(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/assess-role")
@ResponseBody
public ResponseEntity<Map<String, Object>> assessRole(
        @RequestBody Map<String, String> request) {
    try {
        Long resumeId = Long.parseLong(request.get("resumeId"));
        String roleTitle = request.get("roleTitle");
        String jobDescription = request.get("jobDescription");

        Resume resume = resumeService.getResumeById(resumeId);
        String assessment = aiService.assessRoleMatch(
            resume.getContent(),
            "Role: " + roleTitle + "\n\n" + jobDescription
        );

        Map<String, Object> response = Map.of(
            "match", assessment // Return the full text for client-side parsing
        );

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Error assessing role match: " + e.getMessage()
        ));
    }
}
}
