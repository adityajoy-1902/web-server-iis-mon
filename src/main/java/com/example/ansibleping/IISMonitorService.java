package com.example.ansibleping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;

@Service
public class IISMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(IISMonitorService.class);
    
    @Autowired
    private AnsibleService ansibleService;
    
    // Configuration for monitoring
    private String monitoredHost = "34.93.43.151";
    private String monitoredUser = "admin";
    private String monitoredPass = "Pwvp_ae{Q0Zg+B:";
    
    private boolean monitoringEnabled = true;

    /**
     * Scheduled task to check IIS status every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void scheduledIISStatusCheck() {
        if (!monitoringEnabled) {
            logger.debug("IIS monitoring is disabled - skipping scheduled check");
            return;
        }
        
        logger.info("Starting scheduled IIS status check...");
        try {
            // Check IIS status
            String statusResult = checkIISStatus();
            logger.info("IIS Status Check Result: {}", statusResult);
            
            // Check if IIS is running
            if (!isIISRunning(statusResult)) {
                logger.warn("IIS is not running - attempting to start it automatically");
                startIISAutomatically();
            } else {
                logger.info("IIS is running normally - no action needed");
            }
            
        } catch (Exception e) {
            logger.error("Error during scheduled IIS status check: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if IIS is running based on the status result
     */
    private boolean isIISRunning(String statusResult) {
        if (statusResult == null || statusResult.isEmpty()) {
            return false;
        }
        
        // First check if the response indicates failure or unreachable
        if (statusResult.contains("FAILED") || statusResult.contains("UNREACHABLE")) {
            logger.warn("IIS status check failed or host unreachable");
            return false;
        }
        
        // Check for specific running state
        if (statusResult.contains("\"state\": \"running\"")) {
            return true;
        }
        
        // Check for specific stopped state
        if (statusResult.contains("\"state\": \"stopped\"")) {
            logger.info("IIS service is detected as stopped");
            return false;
        }
        
        // If we can't determine the state clearly, log it and assume it's not running
        logger.warn("Could not determine IIS state clearly from response: {}", statusResult);
        return false;
    }

    /**
     * Automatically start IIS when it's down
     */
    private void startIISAutomatically() {
        try {
            logger.info("Attempting to start IIS automatically...");
            String startResult = startIIS();
            logger.info("IIS Auto-Start Result: {}", startResult);
            
            if (startResult.contains("SUCCESS") || startResult.contains("CHANGED")) {
                logger.info("IIS has been successfully started automatically");
            } else {
                logger.error("Failed to start IIS automatically: {}", startResult);
            }
            
        } catch (Exception e) {
            logger.error("Error during automatic IIS start: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger to check IIS status immediately
     */
    public String checkIISStatusNow() {
        logger.info("Manual IIS status check triggered");
        try {
            String result = checkIISStatus();
            logger.info("Manual IIS Status Check Result: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error during manual IIS status check: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Enable or disable monitoring
     */
    public void setMonitoringEnabled(boolean enabled) {
        this.monitoringEnabled = enabled;
        logger.info("IIS monitoring has been {}", enabled ? "enabled" : "disabled");
    }

    /**
     * Get current monitoring status
     */
    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    /**
     * Update monitoring configuration
     */
    public void updateMonitoringConfig(String host, String user, String pass) {
        this.monitoredHost = host;
        this.monitoredUser = user;
        this.monitoredPass = pass;
        logger.info("Monitoring configuration updated for host: {}", host);
    }

    /**
     * Get current monitoring configuration
     */
    public String getMonitoringConfig() {
        return String.format("Host: %s, User: %s, Monitoring: %s", 
                           monitoredHost, monitoredUser, monitoringEnabled ? "Enabled" : "Disabled");
    }

    /**
     * Check IIS status using Ansible
     */
    private String checkIISStatus() {
        String command = String.format(
            "ansible all -i \"%s,\" -m win_service -a \"name=W3SVC\" -e \"ansible_user=%s ansible_password='%s' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985\"",
            monitoredHost, monitoredUser, monitoredPass
        );
        
        return executeCommand(command);
    }
    
    /**
     * Start IIS using Ansible
     */
    private String startIIS() {
        String command = String.format(
            "ansible all -i \"%s,\" -m win_service -a \"name=W3SVC state=started\" -e \"ansible_user=%s ansible_password='%s' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985\"",
            monitoredHost, monitoredUser, monitoredPass
        );
        
        return executeCommand(command);
    }

    /**
     * Execute a command and return the result
     */
    private String executeCommand(String command) {
        StringBuilder result = new StringBuilder();
        
        try {
            // Set environment variables for macOS multiprocessing
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.environment().put("OBJC_DISABLE_INITIALIZE_FORK_SAFETY", "YES");
            pb.environment().put("ANSIBLE_FORKS", "1");
            
            Process process = pb.start();
            
            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            
            // Read error output
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    result.append("ERROR: ").append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Command failed with exit code: {}", exitCode);
            }
            
        } catch (Exception e) {
            logger.error("Error executing command: {}", e.getMessage(), e);
            result.append("ERROR: ").append(e.getMessage());
        }
        
        return result.toString();
    }
} 