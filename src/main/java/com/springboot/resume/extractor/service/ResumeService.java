package com.springboot.resume.extractor.service;

import com.springboot.resume.extractor.model.Resume;
import com.springboot.resume.extractor.repository.ResumeRepository;
import lombok.extern.log4j.Log4j2;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@Log4j2
public class ResumeService {
    
    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private AIService aiService;

    private final Path uploadDir = Paths.get("uploads");

    public ResumeService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public Resume processAndSaveResume(MultipartFile file) throws IOException {
        // Save the file
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + ".pdf";
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Process PDF
        String content = extractTextFromPDF(filePath);
        
        // Create Resume object
        Resume resume = new Resume();
        resume.setFileName(fileName);
        resume.setOriginalFileName(originalFileName);
        resume.setContent(content);
        
        // Extract sections using AI
        resume.setName(generateName(content));
        resume.setEmail(generateEmail(content));
        resume.setExperience(extractExperience(content));
        resume.setEducation(extractEducation(content));
        resume.setSummary(generateSummary(content));
        resume.setKeywords(aiService.extractKeywords(content));
        Optional<Resume> existingResume = resumeRepository.findByOriginalFileName(fileName);

    if (existingResume.isPresent()) {
        log.info("Resume already exists for file: {}", fileName);
        return existingResume.get();
    } else {
        log.info("Saving new resume for file: {}", fileName);
    return resumeRepository.save(resume);
}

    }

public String extractTextFromPDF(Path filePath) throws IOException {
    try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document).trim();

        if (!text.isEmpty()) {
            return text.replaceAll("[^\\p{L}\\p{N}\\s-]", " ").replaceAll("\\s+", " ").trim();

        }

        // Fallback to OCR if text is empty
        PDFRenderer renderer = new PDFRenderer(document);
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // update path if different
        tesseract.setLanguage("eng"); // or your preferred language

        StringBuilder ocrText = new StringBuilder();
        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage image = renderer.renderImageWithDPI(page, 300);
            try {
                String result = tesseract.doOCR(image);
                ocrText.append(result).append("\n");
            } catch (TesseractException e) {
                System.err.println("OCR failed on page " + page + ": " + e.getMessage());
            }
        }

        String rawText = ocrText.toString();
        String cleanedText = rawText
            .replaceAll("[^\\p{L}\\p{N}\\s-]", " ")  // Remove non-alphanumeric/space/hyphen
            .replaceAll("\\s+", " ")                 // Normalize whitespace
            .trim();                                 // Final trim

        return cleanedText;
    }
}
    private String extractExperience(String content) {
        return aiService.extractExperience(content);
    }

    private String extractEducation(String content) {
        return aiService.extractEducation(content);
    }

    private String generateSummary(String content) {
        return aiService.generateSummary(content);
    }

    private String generateName(String content) {
        return aiService.extractName(content);
    }

    private String generateEmail(String content) {
        return aiService.extractEmail(content);
    }

   public Page<Resume> searchResumes(String keyword, Pageable pageable) {
        // Normalize keyword: replace hyphens with spaces, trim, and split into words
        String normalizedKeyword = keyword.replace("-", " ").trim().toLowerCase();
        String[] keywordWords = normalizedKeyword.split("\\s+");
        System.out.println("Searching for keyword: " + normalizedKeyword + ", words: " + Arrays.toString(keywordWords));

        // Fetch all resumes for the page using the raw keyword (for initial filtering)
        Page<Resume> resumePage = resumeRepository.searchResumes(keyword, pageable);
        System.out.println("Initial matches (before filtering): " + resumePage.getContent().size());

        // Filter resumes based on exact word matching
        List<Resume> filteredResumes = resumePage.getContent().stream()
            .filter(resume -> matchesAnyWord(resume, keywordWords))
            .collect(Collectors.toList());
        System.out.println("Filtered matches: " + filteredResumes.size());

        // Create a custom Page object with filtered content
        return new PageImpl<>(filteredResumes, pageable, resumePage.getTotalElements());
    }

    private boolean matchesAnyWord(Resume resume, String[] keywordWords) {
        // Normalize resume filename: remove .pdf extension and replace hyphens with spaces
        String originalFileName = resume.getOriginalFileName().replace(".pdf", "").replace("-", " ").trim().toLowerCase();
        System.out.println("Processing resume: " + resume.getOriginalFileName() + ", normalized: " + originalFileName);

        // Split into words
        String[] words = originalFileName.split("\\s+");
        Set<String> resumeWords = Arrays.stream(words)
            .filter(word -> !word.isEmpty())
            .collect(Collectors.toSet());
        System.out.println("Resume words: " + resumeWords);

        // Debug keyword words
        System.out.println("Keyword words: " + Arrays.toString(keywordWords));

        // Check match
        boolean match = Arrays.stream(keywordWords)
            .filter(word -> !word.isEmpty())
            .anyMatch(resumeWords::contains);
        System.out.println("Match result for " + resume.getOriginalFileName() + ": " + match);
        return match;
    }
    

    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
    }

    public Page<Resume> getAllResumes(PageRequest pageRequest) {
        return resumeRepository.findAll(pageRequest);
    }

    public Resume getResumeById(Long id) {
        return resumeRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("Resume not found with id: " + id));
    }
}
