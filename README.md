# AI-Powered Recruitment & Job Matching Platform

[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.2.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![React 18](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-5.1-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

An enterprise-grade, full-stack recruitment intelligence platform built with **Java (Spring Boot 3)**, **React 18**, and **Semantic AI / NLP Algorithms**. It automates resume parsing, multi-factor job compatibility scoring, candidate leaderboard ranking, and recruiter workflow pipeline management.

---

## 🌟 Key Features

- **🧠 Multi-Factor AI Matching Engine**:
  - **Hard Skills Alignment (45%)**: Canonical skill taxonomy extraction with alias resolution (`k8s` $\rightarrow$ `Kubernetes`, `reactjs` $\rightarrow$ `React`).
  - **Seniority & Experience (25%)**: Proportional experience evaluation against role minimums.
  - **Education Level (15%)**: Weighted scoring for Ph.D., Master's, and Bachelor's degrees.
  - **NLP Semantic Similarity (15%)**: Vector-space Cosine similarity across normalized resume and job text.
- **📄 Automated Resume Parser**:
  - Native document parsing for **PDF** (Apache PDFBox) and **DOCX** (Apache POI).
  - Regex-based entity recognition for emails, phone numbers, and career history.
- **🏆 Candidate Ranking Arena**:
  - Live AI Leaderboards with dynamic rank badges (#1 Gold, #2 Silver, #3 Bronze).
  - Visual breakdown of matched skills vs. missing skill gaps.
- **🔍 Resume Scanner & Skill Gap Analyzer**:
  - Real-time candidate CV analysis with actionable upskilling recommendations.
- **📊 Recruiter Workflow Kanban**:
  - Visual stage management (`APPLIED` $\rightarrow$ `AI_SCREENED` $\rightarrow$ `SHORTLISTED` $\rightarrow$ `INTERVIEW_SCHEDULED` $\rightarrow$ `OFFERED`).
  - Automated shortlisting rule for candidates with match scores $\ge 75\%$.
  - In-app interview scheduler and calendar coordinator.

---

## 🏗️ Architecture Overview

```mermaid
graph TD
    Client[React 18 + Vite Frontend]
    Gateway[REST API Controller Layer]
    Parser[Resume Parser Engine (PDFBox / POI)]
    Engine[AI Matching Engine (Cosine + TF-IDF)]
    DB[(H2 Database / PostgreSQL)]

    Client -->|JSON / Multipart| Gateway
    Gateway --> Parser
    Gateway --> Engine
    Gateway --> DB
    Engine -->|Multi-Factor Score Vector| Gateway
    Parser -->|Structured Entities| Gateway
```

---

## 📂 Repository Structure

```
AI-Powered-Recruitment-Job-Matching-Platform/
├── backend/                             # Java Spring Boot 3 Application
│   ├── pom.xml                          # Maven build dependencies
│   ├── Dockerfile                       # Multi-stage container build
│   └── src/
│       └── main/
│           ├── java/com/recruitment/platform/
│           │   ├── RecruitmentPlatformApplication.java
│           │   ├── config/              # CORS & Swagger OpenAPI specs
│           │   ├── controller/          # REST endpoints (Jobs, Candidates, Matching, Pipeline)
│           │   ├── dto/                 # Request/Response data transfer models
│           │   ├── model/               # JPA entities (Job, Candidate, Application)
│           │   ├── repository/          # Spring Data JPA repositories
│           │   └── service/             # AI Matching & Resume Parsing engines
│           └── resources/
│               ├── application.yml      # DB and upload limits config
│               └── data.sql             # Pre-seeded sample jobs and candidate profiles
├── frontend/                            # React 18 + Vite Application
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   ├── Dockerfile
│   └── src/
│       ├── App.jsx                      # Main UI state and view coordinator
│       ├── index.css                    # Design system (Glassmorphism & animations)
│       ├── components/                  # UI Modules (Leaderboard, Scanner, Kanban, Modals)
│       └── services/api.js              # Live REST bindings with offline mock fallback
└── docker-compose.yml                   # Multi-service stack (Postgres + Backend + Frontend)
```

---

## 🚀 Quickstart Guide

### Option 1: Run with Docker Compose (Recommended)
```bash
docker-compose up --build
```
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Swagger Documentation**: http://localhost:8080/swagger-ui.html

---

### Option 2: Run Locally

#### 1. Backend (Spring Boot 3)
Requirements: **JDK 17+** and **Maven 3.8+**
```bash
cd backend
mvn spring-boot:run
```
- Spring Boot starts on `http://localhost:8080`
- In-memory H2 Console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:recruitmentdb`, Username: `sa`, Password: *(empty)*)
- Pre-populated with realistic jobs and candidates via `data.sql`.

#### 2. Frontend (React + Vite)
Requirements: **Node.js 18+**
```bash
cd frontend
npm install
npm run dev
```
- Open `http://localhost:5173` in your browser.
- The UI includes a built-in hybrid mode that seamlessly links to the Spring Boot REST API when live.

---

## 📡 REST API Specifications

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/jobs` | Retrieve all job requisitions or search by keyword |
| `POST` | `/api/v1/jobs` | Create a new job requisition with skill requirements |
| `GET` | `/api/v1/candidates` | List candidates or search by skills/name |
| `POST` | `/api/v1/candidates/upload-resume` | Upload PDF/DOCX to parse entities and register candidate |
| `POST` | `/api/v1/matching/analyze` | Run multi-factor semantic match between job and resume |
| `GET` | `/api/v1/matching/rank-candidates/{jobId}` | Get AI-ranked candidate leaderboard for a specific job |
| `GET` | `/api/v1/matching/recommend-jobs/{candidateId}` | Get personalized job recommendations for a candidate |
| `GET` | `/api/v1/applications` | List recruitment pipeline applications |
| `PATCH`| `/api/v1/applications/{id}/status` | Advance candidate stage in the recruitment pipeline |
| `GET` | `/api/v1/analytics/dashboard` | Executive KPI metrics and recruitment statistics |

---

## ⚖️ License
Distributed under the Apache 2.0 License.
