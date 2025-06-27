package com.example.ansibleping;

import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Map;

@Service
public class AnsibleService {

    /**
     * Ping a Windows VM using Ansible
     */
    public String pingVM(String host, String user, String pass) {
        String command = String.format(
            "ansible all -i %s, -m win_ping --user %s " +
            "--connection=winrm --extra-vars \"ansible_password=%s " +
            "ansible_port=5985 ansible_winrm_server_cert_validation=ignore\" " +
            "--forks=1",
            host, user, pass
        );
        return executeAnsibleCommand(command);
    }

    /**
     * Check IIS service status
     */
    public String checkIISStatus(String host, String user, String pass) {
        String command = String.format(
            "ansible all -i %s, -m win_service --user %s " +
            "--connection=winrm --extra-vars \"ansible_password=%s " +
            "ansible_port=5985 ansible_winrm_server_cert_validation=ignore\" " +
            "-a \"name=W3SVC\" --forks=1",
            host, user, pass
        );
        return executeAnsibleCommand(command);
    }

    /**
     * Start IIS service
     */
    public String startIIS(String host, String user, String pass) {
        String command = String.format(
            "ansible all -i %s, -m win_service --user %s " +
            "--connection=winrm --extra-vars \"ansible_password=%s " +
            "ansible_port=5985 ansible_winrm_server_cert_validation=ignore\" " +
            "-a \"name=W3SVC state=started\" --forks=1",
            host, user, pass
        );
        return executeAnsibleCommand(command);
    }

    /**
     * Stop IIS service
     */
    public String stopIIS(String host, String user, String pass) {
        String command = String.format(
            "ansible all -i %s, -m win_service --user %s " +
            "--connection=winrm --extra-vars \"ansible_password=%s " +
            "ansible_port=5985 ansible_winrm_server_cert_validation=ignore\" " +
            "-a \"name=W3SVC state=stopped\" --forks=1",
            host, user, pass
        );
        return executeAnsibleCommand(command);
    }

    /**
     * Restart IIS service
     */
    public String restartIIS(String host, String user, String pass) {
        String command = String.format(
            "ansible all -i %s, -m win_service --user %s " +
            "--connection=winrm --extra-vars \"ansible_password=%s " +
            "ansible_port=5985 ansible_winrm_server_cert_validation=ignore\" " +
            "-a \"name=W3SVC state=restarted\" --forks=1",
            host, user, pass
        );
        return executeAnsibleCommand(command);
    }

    /**
     * Execute Ansible command with proper environment setup
     */
    private String executeAnsibleCommand(String command) {
        StringBuilder result = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(true);
            
            // Set environment variables to fix macOS multiprocessing issue
            Map<String, String> env = pb.environment();
            env.put("OBJC_DISABLE_INITIALIZE_FORK_SAFETY", "YES");
            env.put("ANSIBLE_FORKS", "1");
            
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                result.append("Ansible command failed with exit code: ").append(exitCode);
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

        return result.toString();
    }

    /**
     * Validate input parameters
     */
    public void validateInput(String host, String user, String pass) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (user == null || user.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (pass == null || pass.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
    }
} 