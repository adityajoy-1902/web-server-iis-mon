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
    private String monitoredHost = "34.93.235.24";
    private String monitoredUser = "admin";
    private String monitoredPass = "I&8j7TbhSrxy1{z";
    
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
            // Check IIS status using AnsibleService for clean output
            String statusResult = ansibleService.checkIISStatus(monitoredHost, monitoredUser, monitoredPass);
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
        
        // Check for clean status messages
        if (statusResult.contains("🟢 RUNNING")) {
            return true;
        }
        
        if (statusResult.contains("🔴 STOPPED") || statusResult.contains("🔴 UNREACHABLE") || statusResult.contains("🔴 FAILED")) {
            return false;
        }
        
        // Fallback to JSON parsing for backward compatibility
        if (statusResult.contains("\"state\": \"running\"")) {
            return true;
        }
        
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
            
            if (startResult.contains("✅ SUCCESS") || startResult.contains("SUCCESS") || startResult.contains("CHANGED")) {
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