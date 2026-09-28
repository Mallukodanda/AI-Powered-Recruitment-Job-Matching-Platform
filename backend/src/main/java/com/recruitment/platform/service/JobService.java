package com.recruitment.platform.service;

import com.recruitment.platform.model.Job;
import com.recruitment.platform.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getActiveJobs() {
        return jobRepository.findByStatus("ACTIVE");
    }

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public Job createJob(Job job) {
        if (job.getStatus() == null) {
            job.setStatus("ACTIVE");
        }
        return jobRepository.save(job);
    }

    public Job updateJob(Long id, Job updatedJob) {
        return jobRepository.findById(id).map(existing -> {
            existing.setTitle(updatedJob.getTitle());
            existing.setDepartment(updatedJob.getDepartment());
            existing.setLocation(updatedJob.getLocation());
            existing.setJobType(updatedJob.getJobType());
            existing.setExperienceLevel(updatedJob.getExperienceLevel());
            existing.setMinExperienceYears(updatedJob.getMinExperienceYears());
            existing.setSalaryRange(updatedJob.getSalaryRange());
            existing.setDescription(updatedJob.getDescription());
            existing.setRequirements(updatedJob.getRequirements());
            existing.setSkills(updatedJob.getSkills());
            existing.setStatus(updatedJob.getStatus());
            return jobRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public List<Job> searchJobs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getActiveJobs();
        }
        return jobRepository.searchJobs(query.trim());
    }
}
