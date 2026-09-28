# AI-Powered Recruitment & Job Matching Platform

[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.2.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![React 18](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16_pgvector-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800?style=for-the-badge&logo=grafana&logoColor=white)](https://grafana.com/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)

An enterprise-grade, cloud-native recruitment intelligence platform built with **Java (Spring Boot 3)**, **React 18**, and **Semantic AI / Vector Embeddings**. The platform automates resume ingestion and entity parsing, decomposes job descriptions into structured schema, evaluates candidate-job compatibility through a deterministic hybrid scoring model, coordinates multi-stage recruitment pipelines, schedules timezone-aware interviews, generates role-specific interview preparation guides, provides administrative oversight, and delivers observability through Prometheus and Grafana.

---

## Table of Contents
1. [Key Features](#-key-features)
2. [System Architecture](#-system-architecture)
3. [Entity Relationship Model](#-entity-relationship-model)
4. [Technology Stack](#-technology-stack)
5. [Local Development & Setup Guide](#-local-development--setup-guide)
6. [Docker & Containerized Deployment](#-docker--containerized-deployment)
7. [Cloud Architecture & AWS Deployment Strategy](#-cloud-architecture--aws-deployment-strategy)
8. [Observability & Monitoring](#-observability--monitoring)
9. [Security Architecture](#-security-architecture)
10. [Automated Testing Suite](#-automated-testing-suite)
11. [CI/CD Pipelines](#-cicd-pipelines)
12. [Known Limitations & Technical Roadmap](#-known-limitations--technical-roadmap)

---

## 🌟 Key Features

### 1. Hybrid AI Matching & Recommendation Engine
* **Evidence-Based Multi-Signal Compatibility**: Combines Hard Skills (35%), Semantic Vector Similarity (25%), Seniority/Experience (15%), Relevant Technologies (10%), Preferred Skills (10%), and Education Alignment (5%).
* **Explainable Matching Outputs**: Generates transparent score breakdowns, evidence notes, and compatibility bands (`EXCELLENT_MATCH`, `STRONG_MATCH`, `MODERATE_MATCH`, `WEAK_MATCH`).
* **Dense Embedding Generation**: Produces normalized 128-dimensional dense vector embeddings with semantic domain anchors, non-linear activation (`tanh`), and Cauchy-Schwarz compliant cosine similarity.
* **Skill-Gap Analysis**: Detects missing requirements and provides targeted learning roadmaps.

### 2. Candidate & Resume Ingestion Engine
* **Document Parsing**: Ingests **PDF** (Apache PDFBox) and **DOCX** (Apache POI) formats with magic byte inspection (`%PDF`, `PK\x03\x04`), 10MB file bounds, and SHA-256 file hashing.
* **Structured Profile Management**: Tracks career experience, accredited education, categorized candidate skills, and biographical summaries.

### 3. Recruitment Pipeline & Interview Management
* **Deterministic Stage Progression**: Enforces valid status transitions: `APPLIED` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `SHORTLISTED` $\rightarrow$ `INTERVIEW` $\rightarrow$ `HIRED` / `REJECTED`.
* **Timezone-Aware Interview Scheduling**: Manages interview slots, URL/location endpoints, conflict checking, and timezone conversion using `java.time.ZonedDateTime`.
* **AI Interview Preparation Assistant**: Decomposes requisitions and applicant profiles into structured interview guides containing core preparation topics, technical questions, role-specific architecture questions, behavioral HR inquiries, and follow-up probes.
* **Multichannel Notifications**: Decoupled dispatch architecture supporting `EMAIL`, `IN_APP`, and `SMS` delivery channels with delivery failure auditing.

### 4. Administrative Governance & Analytics Hub
* **User & Employer Moderation**: Administrative role elevation, account suspension toggles, self-lockout prevention, company profile verification, and job moderation.
* **Database Aggregation Analytics**: High-performance SQL/JPQL `GROUP BY` aggregations computing active jobs, role distributions, pipeline stages, interview statuses, and AI workloads without memory overhead.
* **Autonomous Security Audit Trail**: Non-blocking `REQUIRES_NEW` transaction logging capturing actor, action, resource, timestamp, and result, with regex-based credential scrubbing (`[REDACTED]`).
* **Dynamic Platform Configurations**: Runtime key-value tuning for auto-shortlisting thresholds, upload boundaries, and maintenance windows.

---

## 🏗️ System Architecture

```mermaid
graph TD
    subgraph ClientLayer [Client Presentation Layer]
        Browser[React 18 + TypeScript SPA]
    end

    subgraph IngressLayer [Ingress & Reverse Proxy]
        Nginx[Nginx Alpine Container :80]
    end

    subgraph AppLayer [Application Core - Spring Boot 3 Modular Monolith]
        SecurityFilter[MdcLoggingFilter & JwtAuthFilter]
        Controllers[REST Controllers - Jobs, Candidates, Admin, Pipeline, AI]
        
        subgraph CoreServices [Business Services]
            CandidateSvc[Candidate Service]
            JobSvc[Job & Search Service]
            WorkflowSvc[Application Workflow Service]
            InterviewSvc[Interview & Notification Service]
            AdminSvc[Admin & Analytics Service]
        end

        subgraph AiSubsystem [AI & Intelligence Subsystem]
            JobAnalyzer[AiJobDescriptionAnalyzer]
            EmbeddingEngine[DenseEmbeddingService 128-D]
            MatchingEngine[HybridJobMatchingService]
            InterviewAI[AiInterviewAssistantService]
        end

        subgraph ObservabilityCore [Telemetry & Audit]
            MetricsSvc[ObservabilityMetricsService]
            AuditSvc[AuditService - REQUIRES_NEW]
        end
    end

    subgraph DataStorageLayer [Data Persistence & Migration]
        FlywayEngine[Flyway Migration Engine]
        PostgreSQL[(PostgreSQL 16 + pgvector)]
        FileStore[Local File System / EBS Volume / S3]
    end

    subgraph MonitoringStack [Observability Stack]
        Prometheus[Prometheus Server :9090]
        Grafana[Grafana Dashboards :3000]
    end

    Browser -->|HTTP / SPA Navigation| Nginx
    Nginx -->|Proxy /api/ & /actuator/| SecurityFilter
    SecurityFilter --> Controllers
    Controllers --> CoreServices
    CoreServices --> AiSubsystem
    CoreServices --> ObservabilityCore
    CoreServices --> FlywayEngine
    FlywayEngine --> PostgreSQL
    CoreServices --> FileStore
    Prometheus -->|Scrape /actuator/prometheus :5s| AppLayer
    Grafana -->|PromQL Queries| Prometheus
```

---

## 🗄️ Entity Relationship Model

```mermaid
erDiagram
    USERS ||--o{ CANDIDATES : owns
    COMPANIES ||--o{ JOBS : sponsors
    CANDIDATES ||--o{ EDUCATIONS : possesses
    CANDIDATES ||--o{ EXPERIENCES : maintains
    CANDIDATES ||--o{ CANDIDATE_SKILLS : tags
    CANDIDATES ||--o{ RESUME_ANALYSES : uploads
    CANDIDATES ||--o{ CANDIDATE_EMBEDDINGS : generates
    CANDIDATES ||--o{ APPLICATIONS : submits
    JOBS ||--o{ APPLICATIONS : receives
    JOBS ||--o{ JOB_ANALYSES : decomposes
    CANDIDATES ||--o{ INTERVIEWS : attends
    JOBS ||--o{ INTERVIEWS : evaluates
    NOTIFICATIONS }o--|| USERS : delivers_to
    AUDIT_LOGS }o--|| USERS : audited_by
    PLATFORM_CONFIGS }o--|| USERS : modified_by

    USERS {
        bigint id PK
        string email UK
        string password_hash
        string first_name
        string last_name
        string role
        boolean active
        timestamp created_at
    }

    COMPANIES {
        bigint id PK
        string name UK
        string website
        string industry
        string location
        boolean verified
        text admin_notes
    }

    JOBS {
        bigint id PK
        bigint company_id FK
        string title
        string department
        string location
        string status
        int min_experience_years
        text description
        text skills
    }

    CANDIDATES {
        bigint id PK
        string full_name
        string email UK
        string phone
        string location
        string current_title
        int years_experience
        text skills_summary
    }

    APPLICATIONS {
        bigint id PK
        bigint candidate_id FK
        bigint job_id FK
        string status
        double match_score
        text match_breakdown_json
        timestamp applied_at
    }

    INTERVIEWS {
        bigint id PK
        bigint candidate_id FK
        bigint job_id FK
        string interview_type
        timestamp scheduled_start_time
        timestamp scheduled_end_time
        string time_zone
        string status
    }

    AUDIT_LOGS {
        bigint id PK
        string actor_username
        string action
        string resource_type
        string resource_id
        string result
        text metadata
        timestamp timestamp
    }
```

---

## 💻 Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend Framework** | Java 17, Spring Boot 3.2.3 (Web, Security, Data JPA, Validation, Actuator) |
| **Database & ORM** | PostgreSQL 16 (pgvector), Hibernate 6.4, Flyway Migration 9.x |
| **Document Processing** | Apache PDFBox 3.0.1, Apache POI 5.2.5 |
| **Security & Auth** | Spring Security 6, JJWT 0.12.5 (HS256), BCrypt (work factor 12) |
| **Frontend Framework** | React 18, TypeScript 5.4, Vite 5.1, TailwindCSS 3.4 |
| **State & Query** | Zustand 4.5, TanStack React Query 5.28, Axios 1.6 |
| **Observability** | Micrometer, Prometheus 2.50, Grafana 10.3, Logback, SLF4J MDC |
| **Testing** | JUnit 5, Mockito, MockMvc, AssertJ, Vitest 1.6, Testing Library |
| **Containerization** | Docker (multi-stage Alpine builds), Docker Compose v2 |
| **CI/CD Automation** | GitHub Actions |

---

## 🚀 Local Development & Setup Guide

### Prerequisites
* **Java**: JDK 17 (Eclipse Temurin recommended)
* **Maven**: 3.9+
* **Node.js**: 20+ and npm
* **Docker & Docker Compose**: v2+

### 1. Clone Repository
```bash
git clone https://github.com/Mallukodanda/AI-Powered-Recruitment-Job-Matching-Platform.git
cd AI-Powered-Recruitment-Job-Matching-Platform
```

### 2. Run Backend Locally (H2 In-Memory Mode)
```bash
cd backend
mvn clean spring-boot:run
```
* Backend starts at `http://localhost:8080`.
* Swagger API Documentation: `http://localhost:8080/swagger-ui.html`.
* H2 Database Console: `http://localhost:8080/h2-console` (`JDBC URL: jdbc:h2:mem:recruitmentdb`, User: `sa`, Password: empty).

### 3. Run Frontend Locally
```bash
cd ../frontend
npm install
npm run dev
```
* Frontend starts at `http://localhost:5173`.

---

## 🐳 Docker & Containerized Deployment

Run the complete production cluster locally with a single command:

```bash
docker compose up --build -d
```

### Verified Service Endpoints
| Service | Internal Host / Port | Host Port | Health Check Endpoint | Credentials |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend SPA** | `frontend:80` | `http://localhost:80` | `http://localhost:80/healthz` | N/A |
| **Backend API** | `backend:8080` | `http://localhost:8080` | `http://localhost:8080/actuator/health` | N/A |
| **PostgreSQL** | `postgres:5432` | `localhost:5432` | `pg_isready -U postgres` | `postgres` / `password` |
| **Prometheus** | `prometheus:9090` | `http://localhost:9090` | `http://localhost:9090/-/healthy` | N/A |
| **Grafana** | `grafana:3000` | `http://localhost:3000` | `http://localhost:3000/api/health` | `admin` / `admin` |

To tear down the cluster and wipe volumes:
```bash
docker compose down -v
```

---

## ☁️ Cloud Architecture & AWS Deployment Strategy

```mermaid
graph TD
    Internet([Public Internet]) --> CloudFront[Amazon CloudFront CDN / HTTPS]
    CloudFront -->|Static SPA Assets| S3Web[S3 Bucket - Frontend Hosting]
    CloudFront -->|/api/* & /actuator/*| ALB[Application Load Balancer]
    
    subgraph VPC [VPC - 10.0.0.0/16]
        subgraph PublicSubnets [Public Subnets - Multi-AZ]
            ALB
            NAT[NAT Gateway]
        end

        subgraph PrivateAppSubnets [Private App Subnets - Multi-AZ]
            ECS[AWS ECS Fargate Cluster]
            Task1[Backend Task Instance A]
            Task2[Backend Task Instance B]
            ECS --- Task1
            ECS --- Task2
        end

        subgraph PrivateDataSubnets [Private Isolated Data Subnets - Multi-AZ]
            RDS[(Amazon RDS PostgreSQL Multi-AZ)]
            S3Storage[Amazon S3 - Resume Storage Bucket]
            SecretsMgr[AWS Secrets Manager]
        end
    end

    ALB -->|Target Group HTTP:8080| Task1
    ALB -->|Target Group HTTP:8080| Task2
    Task1 --> NAT
    Task2 --> NAT
    Task1 -->|JDBC TLS:5432| RDS
    Task2 -->|JDBC TLS:5432| RDS
    Task1 -->|IAM Role Uploads| S3Storage
    Task2 -->|IAM Role Uploads| S3Storage
    Task1 -.->|Fetch DB Credentials & JWT Secret| SecretsMgr
    Task2 -.->|Fetch DB Credentials & JWT Secret| SecretsMgr
```

### AWS Service Selection & Cost Analysis
| Tier | Recommended AWS Service | Rationale | Cost Consideration | Alternative Evaluated |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | Amazon S3 + CloudFront | Serverless static asset hosting, global edge CDN, low-latency SSL termination | ~$1–3 / month | EC2 Nginx (Higher maintenance & cost) |
| **Backend** | AWS ECS Fargate | Serverless container runtime, automatic zero-downtime rolling deploys, no EC2 patching | ~$25–40 / month (0.5 vCPU, 1GB RAM) | AWS EKS (Over-engineered for monolith) |
| **Database** | Amazon RDS PostgreSQL (db.t4g.micro) | Managed automated backups, multi-AZ failover, point-in-time recovery, encryption at rest | ~$15–20 / month | Self-hosted EC2 Postgres (High operational risk) |
| **Storage** | Amazon S3 (Standard + Glacier) | Scalable object storage for resumes with bucket encryption and signed URLs | < $1 / month | Elastic File System (More expensive) |
| **Secrets** | AWS Secrets Manager | Auto-rotation, KMS encryption, zero plaintext credentials in Git or environments | ~$0.40 / secret / month | Plain environment variables (Security risk) |

---

## 📈 Observability & Monitoring

The platform provides end-to-end telemetry across application, database, and AI workloads:

### 1. Prometheus Metrics Scraped
* **HTTP Latency**: `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le, uri))`
* **Error Rate**: `sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m]))`
* **AI Processing Latency**: `recruitment_ai_latency_seconds_max`
* **AI Failures**: `sum(recruitment_ai_failures_total) by (operation, reason)`
* **Connection Pool**: `hikaricp_connections_active`, `hikaricp_connections_idle`, `hikaricp_connections_pending`
* **JVM Heap**: `jvm_memory_used_bytes{area="heap"}`

### 2. Grafana Dashboard
* Auto-provisioned in Docker Compose at `http://localhost:3000`.
* Panels pre-configured for throughput, p95 latency, 5xx error spikes, AI operational health, JVM heap memory, and HikariCP connection health.

### 3. Structured Logging
* Configured in `logback-spring.xml` with MDC request tracing.
* Every HTTP request receives an `X-Request-ID` and is tagged in logs:
  ```text
  2026-09-28 23:30:06.966 INFO  [main] c.r.p.s.a.AiInterviewAssistantService [user=admin@example.com traceId=a1b2c3d4] : Generating AI interview preparation guide for job ID 3
  ```

---

## 🔒 Security Architecture

1. **Defense-in-Depth Authentication**: Stateless JWT Bearer authentication with 24-hour expiration, cryptographic signature validation, and BCrypt password hashing.
2. **Administrative Self-Protection Guardrails**: Explicit business logic preventing administrative users from demoting, disabling, or deleting their own active profile.
3. **Sensitive Data Redaction in Audit Logging**: Regex-based token sanitization in `AuditService` preventing passwords, API keys, and bearer tokens from entering audit tables or forensic logs.
4. **File Ingestion Security**:
   - MIME type and magic byte verification.
   - Strict 10MB payload size limits.
   - Path traversal prevention (`..`, `/`, `\`) with UUID-based storage keys.
5. **CORS & CSRF Hardening**: Configurable allowed origins, stateless session configuration, and secure credential handling.

---

## 🧪 Automated Testing Suite

To execute the test suites:

### Backend Tests (86 Tests)
```bash
cd backend
mvn clean test
```
* **Coverage**: Domain models, repositories, Spring Security authorization, workflow state machines, AI structured output parsing, vector mathematics, and integration scenarios.

### Frontend Tests (8 Tests)
```bash
cd frontend
npm test
```
* **Coverage**: UI component unit tests, Candidate lifecycle simulation, Recruiter lifecycle simulation.

---

## 🔁 CI/CD Pipelines

Implemented with **GitHub Actions**:
1. **Pull Request Workflow (`.github/workflows/pull-request.yml`)**:
   - Triggers on PRs to `main`.
   - Executes backend tests (`mvn verify`).
   - Executes frontend build & Vitest test suite (`npm run build && npm test`).
   - Validates `docker-compose.yml` syntax.
2. **Main Branch Workflow (`.github/workflows/main.yml`)**:
   - Triggers on merges to `main`.
   - Runs full regression test suite.
   - Builds and tags multi-stage Docker container images with Git SHA.
   - Archives release bundle artifacts.

---

## 📋 Known Limitations & Technical Roadmap

* **Distributed Rate Limiting**: The platform enforces single-node memory-based limits; multi-instance clustering requires an external Redis token-bucket implementation *(Planned)*.
* **Asynchronous OCR Worker Queue**: Multi-page PDF text extraction is handled within worker threads; decoupling via RabbitMQ or AWS SQS is recommended for high-volume enterprise ingestion *(Planned)*.
* **Vector Index Scaling**: The current 128-dimensional dense vector embeddings run in-memory and via PostgreSQL pgvector; scaling to 10M+ documents warrants an OpenSearch or Milvus index cluster *(Planned)*.

---

## 📜 License
This project is licensed under the MIT License.
