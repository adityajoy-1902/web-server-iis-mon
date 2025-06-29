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
        
        String result = executeCommand(command);
        return parsePingResult(result);
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
        
        String result = executeCommand(command);
        return parseIISStatusResult(result);
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
        
        String result = executeCommand(command);
        return parseServiceActionResult(result, "started");
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
        
        String result = executeCommand(command);
        return parseServiceActionResult(result, "stopped");
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
        
        String result = executeCommand(command);
        return parseServiceActionResult(result, "restarted");
    }

    /**
     * Parse ping result and return clean status
     */
    private String parsePingResult(String result) {
        if (result.contains("SUCCESS") && result.contains("ping") && result.contains("pong")) {
            return "🟢 CONNECTED - VM is reachable and responding";
        } else if (result.contains("UNREACHABLE")) {
            return "🔴 UNREACHABLE - Cannot connect to VM";
        } else if (result.contains("FAILED")) {
            return "🔴 FAILED - Connection failed";
        } else {
            return "🟡 UNKNOWN - Unexpected response: " + result;
        }
    }

    /**
     * Parse IIS status result and return clean status
     */
    private String parseIISStatusResult(String result) {
        if (result.contains("SUCCESS") && result.contains("state") && result.contains("running")) {
            return "🟢 RUNNING - IIS is active and serving requests";
        } else if (result.contains("SUCCESS") && result.contains("state") && result.contains("stopped")) {
            return "🔴 STOPPED - IIS is not running";
        } else if (result.contains("UNREACHABLE")) {
            return "🔴 UNREACHABLE - Cannot connect to VM";
        } else if (result.contains("FAILED")) {
            return "🔴 FAILED - Failed to check IIS status";
        } else {
            return "🟡 UNKNOWN - Unexpected response: " + result;
        }
    }

    /**
     * Parse service action result and return clean status
     */
    private String parseServiceActionResult(String result, String action) {
        // Check for successful service start/stop with state confirmation
        if ((result.contains("SUCCESS") || result.contains("CHANGED")) && 
            result.contains("changed") && result.contains("true") && 
            result.contains("\"state\": \"running\"")) {
            return "✅ SUCCESS - IIS has been " + action + " successfully";
        } else if ((result.contains("SUCCESS") || result.contains("CHANGED")) && 
                   result.contains("changed") && result.contains("false")) {
            return "ℹ️ NO CHANGE - IIS was already " + action;
        } else if (result.contains("UNREACHABLE")) {
            return "🔴 UNREACHABLE - Cannot connect to VM";
        } else if (result.contains("FAILED")) {
            return "🔴 FAILED - Failed to " + action + " IIS";
        } else if ((result.contains("SUCCESS") || result.contains("CHANGED")) && 
                   result.contains("changed") && result.contains("true")) {
            return "✅ SUCCESS - IIS has been " + action + " successfully";
        } else {
            return "🟡 UNKNOWN - Unexpected response: " + result;
        }
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