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
        validateInput(host, user, pass);
        
        String command = String.format(
            "ansible all -i \"%s,\" -m win_ping -e \"ansible_user=%s ansible_password='%s' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985\"",
            host, user, pass
        );
        
        return executeCommand(command);
    }

    /**
     * Check IIS service status
     */
    public String checkIISStatus(String host, String user, String pass) {
        validateInput(host, user, pass);
        
        String command = String.format(
            "ansible all -i \"%s,\" -m win_service -a \"name=W3SVC\" -e \"ansible_user=%s ansible_password='%s' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985\"",
            host, user, pass
        );
        
        return executeCommand(command);
    }

    /**
     * Start IIS service
     */
    public String startIIS(String host, String user, String pass) {
        validateInput(host, user, pass);
        
        String command = String.format(
            "ansible all -i \"%s,\" -m win_service -a \"name=W3SVC state=started\" -e \"ansible_user=%s ansible_password='%s' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985\"",
            host, user, pass
        );
        
        return executeCommand(command);
    }

    /**
     * Stop IIS service
     */
    public String stopIIS(String host, String user, String pass) {
        validateInput(host, user, pass);
        
        String command = String.format(
            "ansible all -i \"%s,\" -m win_service -a \"name=W3SVC state=stopped\" -e \"ansible_user=%s ansible_password='%s' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985\"",
            host, user, pass
        );
        
        return executeCommand(command);
    }

    /**
     * Restart IIS service
     */
    public String restartIIS(String host, String user, String pass) {
        validateInput(host, user, pass);
        
        String command = String.format(
            "ansible all -i \"%s,\" -m win_service -a \"name=W3SVC state=restarted\" -e \"ansible_user=%s ansible_password='%s' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985\"",
            host, user, pass
        );
        
        return executeCommand(command);
    }

    /**
     * Execute Ansible command with proper environment setup
     */
    private String executeCommand(String command) {
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