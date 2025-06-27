package com.example.ansibleping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Scheduled task to check IIS status every 30 minutes
     * Fixed rate of 30 minutes = 30 * 60 * 1000 milliseconds
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void monitorIISStatus() {
        if (!monitoringEnabled) {
            logger.info("IIS monitoring is disabled");
            return;
        }
        
        logger.info("Starting scheduled IIS status check...");
        
        try {
            // Check IIS status
            String statusResult = ansibleService.checkIISStatus(monitoredHost, monitoredUser, monitoredPass);
            logger.info("IIS Status Check Result: {}", statusResult);
            
            // Check if IIS is running
            if (isIISRunning(statusResult)) {
                logger.info("IIS is running normally - no action needed");
            } else {
                logger.warn("IIS is not running - attempting to start it automatically");
                startIISAutomatically();
            }
            
        } catch (Exception e) {
            logger.error("Error during scheduled IIS monitoring: {}", e.getMessage(), e);
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
            String startResult = ansibleService.startIIS(monitoredHost, monitoredUser, monitoredPass);
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
            String result = ansibleService.checkIISStatus(monitoredHost, monitoredUser, monitoredPass);
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
} 