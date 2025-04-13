package com.springboot.resume.extractor.service;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class AIService {

    @Autowired
    private ChatClient chatClient;


    public String extractName(String content) {
        String promptText = """
            You are a recruiter extracting the candidate's name from a resume extracted from a PDF, which may include OCR noise or unformatted text (e.g., paragraphs, missing lines). Identify the name based on context (e.g., at the top, near contact info, or in headers). Return the name as a single string in title case, just the name nothing else to be in response (e.g., "John Doe"). If no name is found, return "Name not found". Resume: {content}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("content", content);

        return chatClient.call(template.create())
                .getResult()
                .getOutput()
                .getContent();
    }

    public String extractEmail(String content) {
        String promptText = """
            You are a recruiter extracting the candidate's email from a resume extracted from a PDF, which may include OCR noise or unformatted text (e.g., paragraphs, missing characters). Identify the email based on context (e.g., near contact info, in a recognizable format like xxx@xxx.com). Return the email as a single string, just the email nothing else to be in response (e.g., "john.doe@example.com"). If no email is found, return "Email not found". Resume: {content}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("content", content);

        return chatClient.call(template.create())
                .getResult()
                .getOutput()
                .getContent();
    }
     

    public String extractExperience(String content) {
        String promptText = """
            You are a recruiter evaluating a resume extracted from a PDF, which may include OCR noise or unformatted text (e.g., paragraphs, tables, or missing bullets). Extract the candidate's professional experience, don't add notes or unnecessary things in response. For each role, include:
            - Company name
            - Duration (e.g., MM/YYYY - MM/YYYY or "Present") inferred from context if not explicit
            - Job title
            - 2-3 key achievements with quantifiable metrics (e.g., "Increased sales by 20%") inferred from context.
            Handle missing or noisy formatting. Format each role exactly as:
            **[Company Name]** ([Duration])
            • [Title]
            • [Achievement 1]
            • [Achievement 2]
            • [Achievement 3] (if applicable)
            Omit roles without clear details. Resume: {content}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("content", content);

        return chatClient.call(template.create())
                .getResult()
                .getOutput()
                .getContent();
    }

    public String extractEducation(String content) {
        String promptText = """
            You are a recruiter reviewing a resume extracted from a PDF, which may include OCR noise or unformatted text (e.g., paragraphs, tables, or missing bullets). Extract the candidate's education and certifications, don't add notes or unnecessary things in response. Infer details (e.g., year, institution) from context if formatting is lost. Format each entry exactly as:
            • [Degree] in [Field] - [Institution], [Year]
            • [Certification Name] - [Institution/Issuer], [Year] (if applicable)
            Include only verified or reasonably inferred details. Omit incomplete entries. Resume: {content}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("content", content);

        return chatClient.call(template.create())
                .getResult()
                .getOutput()
                .getContent();
    }

    public String generateSummary(String content) {
        String promptText = """
            You are a recruiter creating a concise summary for a resume extracted from a PDF, which may include OCR noise or unformatted text (e.g., paragraphs, tables, or missing bullets). Extract 3-4 key points about the candidate's experience and expertise, don't add notes or unnecessary things in response, focusing on:
            - Years of experience in relevant fields
            - Notable achievements with metrics
            - Core areas of expertise
            Infer details from context if formatting is lost. Format as bullet points for quick scanning, e.g.:
            • [X] years in [field], specializing in [area]
            • Achieved [metric-based result] in [role/context]
            • Expert in [skill/technology]
            Keep each point concise (under 15 words). Resume: {content}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("content", content);

        return chatClient.call(template.create())
                .getResult()
                .getOutput()
                .getContent();
    }

    public String extractKeywords(String content) {
        String promptText = """
            You are a recruiter identifying key terms from a resume extracted from a PDF, which may include OCR noise or unformatted text (e.g., paragraphs, tables, or missing bullets). Extract the most relevant:
            - **Technologies:** Specific tools or platforms (e.g., Java, Docker)
            - **Industry Terms:** Domain-specific jargon (e.g., DevOps, Agile)
            - **Methodologies:** Approaches or frameworks (e.g., Scrum, Lean)
            Infer terms from context if formatting is lost. List each category with 3-5 terms, avoiding overlap. Format exactly as:
            **Technologies:**
            • [Term 1]
            • [Term 2]
            • ...
            **Industry Terms:**
            • [Term 1]
            • [Term 2]
            • ...
            **Methodologies:**
            • [Term 1]
            • [Term 2]
            • ...
            Resume: {content}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("content", content);

        return chatClient.call(template.create())
                .getResult()
                .getOutput()
                .getContent();
    }
    
    public String assessRoleMatch(String resumeContent, String jobDescription) {
        log.debug("Resume Content: {}", resumeContent);
        log.debug("Job Description: {}", jobDescription);
    
        String promptText = """
            You are a recruiter assessing a candidate's fit for a role. The resume is extracted from a PDF, which may include OCR noise or unformatted text (e.g., truncated words, missing characters). Follow these steps:
            1. Extract required skills, qualifications, experience level, industry, and certifications from the job description. Infer skills from context (e.g., "knowledge about travelling" → "Travel Planning", "good in management" → "Management Skills").
            2. Extract the candidate's skills, experience, industry knowledge, and certifications from the resume, inferring details from context if formatting is lost (e.g., "organizing planning" → "Travel Planning", "Amazon Travel" → "Travel Industry Experience").
            3. Compare systematically:
               - Technical skills match
               - Experience level (years and relevance)
               - Industry alignment
               - Certification match
            4. Calculate the match score (0-100%):
               - Start with 100 points
               - -15 points per missing major skill (e.g., core job requirements)
               - -5 points per missing minor skill (e.g., nice-to-have)
               - -20 points for experience level mismatch
               - -15 points for industry mismatch
               - -15 points per missing required certification
               - +5 points per relevant additional skill (max +15)
               - If no significant match (e.g., no skills or experience alignment), assign a low score (0-30%) and provide a reason.
            5. Format the response EXACTLY as:
            **Match Score:** [0-100]%
    
            **Matching Skills (Advantaged, display in green):**
            • [Skill 1]
            • [Skill 2]
            • ...
    
            **Missing Skills (display in red):**
            • [Requirement 1]
            • [Requirement 2]
            • ...
    
            **Additional Relevant Skills (handy, display in blue, max 3):**
            • [Skill 1]
            • [Skill 2]
            • ...
    
            **Match Analysis (neutral text):**
            • [Point 1: e.g., Strong match in travel planning]
            • [Point 2: e.g., Lacks specific management experience]
            • [Point 3: e.g., Relevant industry experience present]
    
            Resume: {resumeContent}
            Job Description: {jobDescription}
            """;
    
        PromptTemplate template = new PromptTemplate(promptText);
        template.add("resumeContent", resumeContent);
        template.add("jobDescription", jobDescription);
    
        String response = chatClient.call(template.create())
                .getResult()
                .getOutput()
                .getContent();
        log.debug("Response: {}", response);
        return response;
    }
}