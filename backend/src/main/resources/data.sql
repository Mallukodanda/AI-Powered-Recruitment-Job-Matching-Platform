-- Pre-populated sample jobs for immediate platform testing
INSERT INTO jobs (id, title, department, location, job_type, experience_level, min_experience_years, salary_range, description, requirements, skills, status, created_at)
VALUES 
(1, 'Senior Full-Stack Java & React Engineer', 'Engineering', 'Remote / New York', 'FULL_TIME', 'SENIOR', 5, '$140,000 - $175,000',
'Lead the development of scalable microservices and high-performance React frontends. You will architect event-driven architectures, design resilient REST APIs, and collaborate closely with product and AI teams.',
'Bachelor or Master in Computer Science or equivalent. 5+ years building distributed Java applications with Spring Boot. Deep proficiency in modern React, TypeScript, and cloud deployment pipelines.',
'Java, Spring Boot, React, TypeScript, REST API, Docker, PostgreSQL, Microservices, Git',
'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO jobs (id, title, department, location, job_type, experience_level, min_experience_years, salary_range, description, requirements, skills, status, created_at)
VALUES 
(2, 'Machine Learning & NLP Specialist', 'AI Research', 'San Francisco, CA (Hybrid)', 'FULL_TIME', 'MID_SENIOR', 3, '$150,000 - $190,000',
'Design and train transformer models, LLM retrieval-augmented generation (RAG) pipelines, and intelligent semantic ranking algorithms for our enterprise talent intelligence platform.',
'Strong foundation in Natural Language Processing (NLP), semantic search, vector embeddings, and Python/PyTorch. Familiarity with Java or Spring integration is a strong plus.',
'Python, NLP, PyTorch, Transformers, LLMs, Vector Databases, Semantic Search, FastAPI, Docker, Java',
'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO jobs (id, title, department, location, job_type, experience_level, min_experience_years, salary_range, description, requirements, skills, status, created_at)
VALUES 
(3, 'Cloud Infrastructure & DevOps Architect', 'DevOps', 'Austin, TX / Remote', 'FULL_TIME', 'LEAD', 6, '$160,000 - $200,000',
'Own the reliability, security, and scalability of multi-region cloud infrastructure on AWS and Kubernetes. Automate CI/CD pipelines and implement Terraform infrastructure as code.',
'Proven track record managing Kubernetes clusters in production. Expert knowledge of AWS, Terraform, Docker, monitoring stacks (Prometheus/Grafana), and zero-downtime deployment strategies.',
'AWS, Kubernetes, Docker, Terraform, CI/CD, Linux, Prometheus, Grafana, Java, Python',
'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO jobs (id, title, department, location, job_type, experience_level, min_experience_years, salary_range, description, requirements, skills, status, created_at)
VALUES 
(4, 'Frontend UI/UX Engineer', 'Design & Product', 'Seattle, WA / Remote', 'FULL_TIME', 'MID', 3, '$115,000 - $145,000',
'Craft engaging, ultra-fast web experiences and reusable design component libraries using React, modern CSS, and Vite. Champion accessibility, responsive design, and fluid animations.',
'3+ years experience building interactive web applications with React. Deep mastery of modern CSS/SCSS, performance profiling, responsive layouts, and state management.',
'React, JavaScript, TypeScript, CSS3, HTML5, Vite, Redux, UI/UX, Web Accessibility',
'ACTIVE', CURRENT_TIMESTAMP);

-- Sample candidates
INSERT INTO candidates (id, full_name, email, phone, location, current_title, years_experience, highest_education, skills_summary, bio, created_at)
VALUES
(1, 'Alex Rivera', 'alex.rivera@example.com', '+1 (555) 234-5678', 'New York, NY', 'Senior Software Engineer', 6, 'M.S. in Computer Science',
'Java, Spring Boot, React, TypeScript, PostgreSQL, Docker, Microservices, REST API, Git, AWS',
'Experienced full-stack engineer with 6 years leading cloud-native product development using Java Spring Boot and React. Passionate about clean code, unit testing, and distributed systems.', CURRENT_TIMESTAMP);

INSERT INTO candidates (id, full_name, email, phone, location, current_title, years_experience, highest_education, skills_summary, bio, created_at)
VALUES
(2, 'Dr. Priya Sharma', 'priya.sharma@example.com', '+1 (555) 876-5432', 'San Francisco, CA', 'Lead AI Research Engineer', 5, 'Ph.D. in Artificial Intelligence',
'Python, NLP, PyTorch, Transformers, LLMs, Vector Databases, Semantic Search, FastAPI, Docker',
'AI researcher with a Ph.D. specializing in Natural Language Processing and information retrieval. Authored papers on transformer fine-tuning and semantic document ranking.', CURRENT_TIMESTAMP);

INSERT INTO candidates (id, full_name, email, phone, location, current_title, years_experience, highest_education, skills_summary, bio, created_at)
VALUES
(3, 'Marcus Vance', 'marcus.vance@example.com', '+1 (555) 345-6789', 'Austin, TX', 'Senior Cloud/DevOps Engineer', 7, 'B.S. in Software Engineering',
'AWS, Kubernetes, Docker, Terraform, CI/CD, Linux, Prometheus, Grafana, Bash, Python',
'DevOps engineer passionate about GitOps, Kubernetes cluster scaling, and zero-trust cloud infrastructure on AWS. Spearheaded migration of 50+ microservices.', CURRENT_TIMESTAMP);

INSERT INTO candidates (id, full_name, email, phone, location, current_title, years_experience, highest_education, skills_summary, bio, created_at)
VALUES
(4, 'Elena Rostova', 'elena.rostova@example.com', '+1 (555) 456-7890', 'Boston, MA', 'Frontend Developer', 3, 'B.S. in Computer Science',
'React, JavaScript, TypeScript, CSS3, HTML5, Vite, Redux, TailwindCSS, Figma',
'Frontend specialist focused on design systems, accessible UI interactions, and snappy micro-frontends with React and modern CSS.', CURRENT_TIMESTAMP);
