# Ansible Windows VM Manager

A Spring Boot application that provides automated monitoring and management of Windows VMs using Ansible. The system includes automatic IIS monitoring with auto-recovery capabilities.

## 🚀 Features

- **VM Connectivity Testing** - Ping Windows VMs via WinRM
- **IIS Management** - Start, stop, restart, and check IIS status
- **Automated Monitoring** - Automatic IIS status monitoring every 5 minutes
- **Auto-Recovery** - Automatically restart IIS if it goes down
- **Web UI** - User-friendly interface for all operations
- **REST API** - Full API access for integration

## 📋 Prerequisites

### Host Machine Requirements

#### 1. Java Development Kit (JDK)
```bash
# Check if Java is installed
java -version

# Install Java 17+ if needed
# macOS (using Homebrew)
brew install openjdk@17

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel
```

#### 2. Maven
```bash
# Check if Maven is installed
mvn -version

# Install Maven if needed
# macOS (using Homebrew)
brew install maven

# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven
```

#### 3. Ansible
```bash
# Check if Ansible is installed
ansible --version

# Install Ansible if needed
# macOS (using Homebrew)
brew install ansible

# Ubuntu/Debian
sudo apt install ansible

# CentOS/RHEL
sudo yum install ansible

# Using pip (alternative)
pip3 install ansible

# Install Windows collection (REQUIRED for Windows management)
ansible-galaxy collection install ansible.windows
```

#### 4. Environment Variables (macOS)
Due to macOS multiprocessing issues with Ansible, set these environment variables:
```bash
export OBJC_DISABLE_INITIALIZE_FORK_SAFETY=YES
export ANSIBLE_FORKS=1
```

Add to your shell profile (`~/.zshrc` or `~/.bash_profile`):
```bash
echo 'export OBJC_DISABLE_INITIALIZE_FORK_SAFETY=YES' >> ~/.zshrc
echo 'export ANSIBLE_FORKS=1' >> ~/.zshrc
source ~/.zshrc
```

## 🖥️ Windows VM Setup

### 1. Enable WinRM on Windows VM

Run these PowerShell commands **as Administrator** on your Windows VM:

```powershell
# Enable WinRM service
Enable-PSRemoting -Force

# Configure WinRM for HTTPS (recommended for production)
winrm quickconfig -transport:https

# Configure NTLM authentication (more secure than basic)
Set-Item -Path WSMan:\localhost\Service\Auth\NTLM -Value $true
Set-Item -Path WSMan:\localhost\Service\Auth\Basic -Value $false

# Configure HTTPS listener on port 5986
New-Item -Path WSMan:\localhost\Listener -Address * -Transport HTTPS -Hostname $env:COMPUTERNAME -Port 5986 -CertificateThumbprint (Get-ChildItem Cert:\LocalMachine\My | Where-Object {$_.Subject -like "*$env:COMPUTERNAME*"} | Select-Object -First 1).Thumbprint -Force

# Restart WinRM service
Restart-Service WinRM

# Verify HTTPS listener
Get-ChildItem WSMan:\localhost\Listener

# Allow HTTPS WinRM through Windows Firewall
New-NetFirewallRule -DisplayName "Windows Remote Management (HTTPS-In)" -Profile Any -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5986
```

**Note**: This configuration uses HTTPS WinRM (port 5986) with NTLM authentication for enhanced security.

### 2. Configure Windows Firewall

```powershell
# Allow WinRM traffic through Windows Firewall
New-NetFirewallRule -DisplayName "Windows Remote Management (HTTP-In)" -Profile Any -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5985

# Allow HTTPS WinRM (recommended for production)
New-NetFirewallRule -DisplayName "Windows Remote Management (HTTPS-In)" -Profile Any -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5986
```

### 3. Create Admin User (if needed)

```powershell
# Create a new administrator user
New-LocalUser -Name "admin" -Password (ConvertTo-SecureString "YourSecurePassword123!" -AsPlainText -Force) -FullName "Administrator" -Description "Admin user for Ansible"

# Add user to Administrators group
Add-LocalGroupMember -Group "Administrators" -Member "admin"

# Verify user creation
Get-LocalUser -Name "admin"
```

### 4. Test WinRM Connection

From your host machine, test the WinRM connection:

```bash
# Test basic connectivity
ansible -i "34.93.43.151," -m win_ping -e "ansible_user=admin ansible_password='Pwvp_ae{Q0Zg+B:' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic"
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Ansible "Worker in dead state" Error (macOS)
```bash
# Set environment variables
export OBJC_DISABLE_INITIALIZE_FORK_SAFETY=YES
export ANSIBLE_FORKS=1
```

#### 2. WinRM Connection Failed
- Verify WinRM is enabled on Windows VM
- Check firewall rules (both Windows and cloud provider)
- Ensure credentials are correct
- Test with basic Ansible command first

#### 3. URL Parsing Errors
- Use POST requests instead of GET for endpoints with special characters
- URL-encode special characters in passwords for GET requests

#### 4. IIS Not Starting
- Check if IIS is installed on Windows VM
- Verify service dependencies are running
- Check Windows Event Logs for errors

### Verification Commands

#### On Windows VM
```powershell
# Check WinRM status
Get-Service WinRM

# Check IIS service
Get-Service -Name W3SVC

# Test WinRM connectivity
Test-WSMan -ComputerName localhost

# Check firewall rules
Get-NetFirewallRule | Where-Object {$_.DisplayName -like "*WinRM*"}
```

#### On Host Machine
```bash
# Test Ansible connectivity
ansible -i "YOUR_VM_IP," -m win_ping -e "ansible_user=admin ansible_password='YOUR_PASSWORD' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic"

# Check application logs
tail -f logs/application.log
```

## 📊 Monitoring

### Automatic Monitoring
- **Frequency**: Every 5 minutes
- **Action**: Checks IIS status and auto-restarts if down
- **Logs**: All activities logged with timestamps

### Manual Monitoring
- Use web interface or API to check status anytime
- Enable/disable monitoring as needed
- View monitoring configuration

## 🔒 Security Considerations

### Production Recommendations
1. **Use HTTPS WinRM** instead of HTTP
2. **Implement proper authentication** (Kerberos, certificates)
3. **Restrict firewall rules** to specific IP ranges
4. **Use strong passwords** and rotate regularly
5. **Enable Windows Event Logging** for audit trails
6. **Use service accounts** instead of admin accounts

### Current Setup (Development/Testing)
- Basic authentication enabled
- Unencrypted traffic allowed
- Wide firewall rules for testing

## 📝 Logs and Monitoring

### Application Logs
- Spring Boot logs show all operations
- IIS monitoring activities logged with timestamps
- Error details and stack traces for debugging

### Key Log Messages
```
INFO  - Starting scheduled IIS status check...
INFO  - IIS Status Check Result: [details]
INFO  - IIS is running normally - no action needed
WARN  - IIS is not running - attempting to start it automatically
INFO  - IIS has been successfully started automatically
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For issues and questions:
1. Check the troubleshooting section
2. Review application logs
3. Test individual components
4. Create an issue with detailed information

---

**Note**: This setup is configured for development/testing. For production use, implement proper security measures as outlined in the Security Considerations section.

### **3. Cloud Provider Firewall Configuration**

#### **Google Cloud Platform (GCP)**
```bash
# Create firewall rule to allow HTTPS WinRM (port 5986)
gcloud compute firewall-rules create allow-winrm-https \
    --allow tcp:5986 \
    --description "Allow HTTPS WinRM connections" \
    --direction INGRESS \
    --network default \
    --priority 1000 \
    --source-ranges 0.0.0.0/0 \
    --target-tags winrm-https

# Apply the network tag to your VM
gcloud compute instances add-tags YOUR_VM_NAME --tags winrm-https
```

**Note**: The application now uses HTTPS WinRM (port 5986) with NTLM authentication for enhanced security. 