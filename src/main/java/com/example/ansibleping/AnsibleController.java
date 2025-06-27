package com.example.ansibleping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@CrossOrigin(origins = "*")
public class AnsibleController {

    @Autowired
    private AnsibleService ansibleService;

    @Autowired
    private IISMonitorService iisMonitorService;

    @PostMapping("/ping-vm")
    public ResponseEntity<String> pingVM(@RequestBody PingRequest request) {
        try {
            ansibleService.validateInput(request.getHost(), request.getUser(), request.getPass());
            String result = ansibleService.pingVM(request.getHost(), request.getUser(), request.getPass());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/check-iis-status")
    public ResponseEntity<String> checkIISStatus(@RequestBody PingRequest request) {
        try {
            ansibleService.validateInput(request.getHost(), request.getUser(), request.getPass());
            String result = ansibleService.checkIISStatus(request.getHost(), request.getUser(), request.getPass());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/start-iis")
    public ResponseEntity<String> startIIS(@RequestBody PingRequest request) {
        try {
            ansibleService.validateInput(request.getHost(), request.getUser(), request.getPass());
            String result = ansibleService.startIIS(request.getHost(), request.getUser(), request.getPass());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/stop-iis")
    public ResponseEntity<String> stopIIS(@RequestBody PingRequest request) {
        try {
            ansibleService.validateInput(request.getHost(), request.getUser(), request.getPass());
            String result = ansibleService.stopIIS(request.getHost(), request.getUser(), request.getPass());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/restart-iis")
    public ResponseEntity<String> restartIIS(@RequestBody PingRequest request) {
        try {
            ansibleService.validateInput(request.getHost(), request.getUser(), request.getPass());
            String result = ansibleService.restartIIS(request.getHost(), request.getUser(), request.getPass());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // GET methods for browser compatibility
    @GetMapping("/ping-vm")
    public ResponseEntity<String> pingVMGet(
            @RequestParam String host,
            @RequestParam String user,
            @RequestParam String pass) {
        try {
            ansibleService.validateInput(host, user, pass);
            String result = ansibleService.pingVM(host, user, pass);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/check-iis-status")
    public String checkIISStatus(@RequestParam String host, 
                                @RequestParam String user, 
                                @RequestParam String pass) {
        try {
            String decodedPass = URLDecoder.decode(pass, StandardCharsets.UTF_8.toString());
            return ansibleService.checkIISStatus(host, user, decodedPass);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/start-iis")
    public ResponseEntity<String> startIISGet(
            @RequestParam String host,
            @RequestParam String user,
            @RequestParam String pass) {
        try {
            ansibleService.validateInput(host, user, pass);
            String result = ansibleService.startIIS(host, user, pass);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/stop-iis")
    public ResponseEntity<String> stopIISGet(
            @RequestParam String host,
            @RequestParam String user,
            @RequestParam String pass) {
        try {
            ansibleService.validateInput(host, user, pass);
            String result = ansibleService.stopIIS(host, user, pass);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/restart-iis")
    public ResponseEntity<String> restartIISGet(
            @RequestParam String host,
            @RequestParam String user,
            @RequestParam String pass) {
        try {
            ansibleService.validateInput(host, user, pass);
            String result = ansibleService.restartIIS(host, user, pass);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // New monitoring endpoints
    @GetMapping("/monitor/status")
    public String getMonitoringStatus() {
        return iisMonitorService.getMonitoringConfig();
    }

    @PostMapping("/monitor/enable")
    public String enableMonitoring() {
        iisMonitorService.setMonitoringEnabled(true);
        return "Monitoring enabled successfully";
    }

    @PostMapping("/monitor/disable")
    public String disableMonitoring() {
        iisMonitorService.setMonitoringEnabled(false);
        return "Monitoring disabled successfully";
    }

    @PostMapping("/monitor/check-now")
    public String checkIISStatusNow() {
        return iisMonitorService.checkIISStatusNow();
    }

    @PostMapping("/monitor/update-config")
    public String updateMonitoringConfig(@RequestBody PingRequest request) {
        try {
            iisMonitorService.updateMonitoringConfig(request.host, request.user, request.pass);
            return "Monitoring configuration updated successfully";
        } catch (Exception e) {
            return "Error updating monitoring configuration: " + e.getMessage();
        }
    }

    // Request body class for POST method
    public static class PingRequest {
        private String host;
        private String user;
        private String pass;

        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }

        public String getPass() { return pass; }
        public void setPass(String pass) { this.pass = pass; }
    }
}
