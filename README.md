# Resume Extractor

A Spring Boot application that extracts and summarizes information from PDF resumes, making it easier for recruiters to analyze candidate profiles.

---

## ✨ Features

- ✅ Upload PDF resumes through a modern drag-and-drop interface
- ✅ Automatically extract key information including:
  - Skills
  - Experience
  - Education
  - Summary
- ✅ Search and filter resumes
- ✅ View detailed parsed resume data
- ✅ H2 database for development/testing
- ✅ Clean UI built with Bootstrap

---

## 🧰 Prerequisites

- **Java** 17 or higher  
- **Maven** 3.6 or higher  
- **Docker** and **Docker Compose** (optional, for containerized deployment)

---

## ▶️ Running the Application

### 🖥️ Option 1: Run Locally (Without Docker)

1. **Clone the Repository**  
   ```bash
   git clone https://github.com/your-username/resume-extractor.git
   cd resume-extractor
   ```

2. **Build the project**  
   ```bash
   mvn clean install
   ```

3. **Run the Spring Boot App**  
   ```bash
   mvn spring-boot:run
   ```

4. **Access the app**  
   Open your browser and go to: [http://localhost:8080](http://localhost:8080)

---

### 🚣 Option 2: Run with Docker

> This method uses Docker Compose to run the app in an isolated container.

#### 1. **Create a `.env` file**

Create a `.env` file in the project root:

```env
PORT=8080
DB_URL=jdbc:h2:mem:resume-db
DB_USERNAME=sa
DB_PASSWORD=
OPENAI_API_KEY=your-api-key-here
```

> 📝 Replace `your-api-key-here` with your actual OpenAI API key.

#### 2. **Build and Start the Container**

```bash
docker compose up --build
```

#### 3. **Access the App**

Once the container is up, open your browser:

[http://localhost:8080](http://localhost:8080)

---

## 📃 Project Structure

```
resume-extractor/
├── src/
├── Dockerfile
├── docker-compose.yml
├── .env
├── pom.xml
└── README.md
```

---

## 🚜 Future Improvements

- Integration with cloud storage (S3, GCS)
- Support for DOCX format
- Admin dashboard for user management
- Scoring and ranking of resumes

---