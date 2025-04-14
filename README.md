# Resume Extractor

A Spring Boot application that extracts and summarizes information from PDF resumes, making it easier for recruiters to analyze candidate profiles.

## Features

- Upload PDF resumes through a modern drag-and-drop interface
- Extract key information including:
  - Skills
  - Experience
  - Education
  - Summary
- Search through uploaded resumes
- View detailed resume information
- H2 database for data persistence
- Responsive UI using Bootstrap

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Setup and Running

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access the application at `http://localhost:8080`
5. Access H2 Console at `http://localhost:8080/h2-console` (if needed)
   - JDBC URL: `jdbc:h2:file:./data/resumedb`
   - Username: sa
   - Password: password

## Deployment to Netlify

1. Ensure you have a Netlify account and are logged in to the Netlify CLI

2. Initialize your Git repository if not already done:
```bash
git init
git add .
git commit -m "Initial commit"
```

3. Deploy to Netlify:
```bash
netlify deploy --prod
```

4. Set environment variables in Netlify:
   - Go to your site's dashboard on Netlify
   - Navigate to Site settings > Build & deploy > Environment variables
   - Add these variables:
     - `OPENAI_API_KEY`: Your OpenAI API key
     - `JAVA_HOME`: Set to "/opt/buildhome/.jdk"

## Docker Deployment

### Prerequisites

- Docker and Docker Compose installed
- OpenAI API key

### Build and Run

```bash
# Build the Docker image
docker build -t resume-extractor .

# Run the container
docker run -d \
  --name resume-extractor \
  -p 8080:8080 \
  -e OPENAI_API_KEY="your-api-key-here" \
  resume-extractor
```

### Using Docker Compose

1. Create a `.env` file with your environment variables:
```bash
echo "OPENAI_API_KEY=your-api-key-here" > .env
```

2. Run with Docker Compose:
```bash
docker-compose up -d
```

### Access the Application

Once running, access the application at:
- http://localhost:8080

## Technical Stack

- Backend:
  - Spring Boot 3.4.4
  - Spring Data JPA
  - Apache PDFBox 3.0.1
  - H2 Database
- Frontend:
  - Thymeleaf
  - Bootstrap 5.3
  - HTML5/CSS3/JavaScript

## Project Structure

```
src/main/java/com/springboot/resume/extractor/
├── controller/
│   └── ResumeController.java
├── model/
│   └── Resume.java
├── repository/
│   └── ResumeRepository.java
├── service/
│   └── ResumeService.java
└── Application.java
```

## Usage

1. Open the application in your web browser
2. Use the drag-and-drop interface or click to select a PDF resume
3. Click "Upload Resume" to process the file
4. View the extracted information on the details page
5. Use the search functionality to find specific resumes

## Future Enhancements

- AI/ML integration for better information extraction
- Support for more document formats
- Advanced search filters
- Batch upload functionality
- Export functionality
