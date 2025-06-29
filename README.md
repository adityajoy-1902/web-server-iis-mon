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

### Step 1: Configure WinRM on Windows VM

**IMPORTANT**: Run these PowerShell commands **as Administrator** on your Windows VM:

#### 1.1 Enable WinRM Service
```powershell
# Enable WinRM service
Enable-PSRemoting -Force
```

#### 1.2 Configure WinRM for HTTP (Port 5985)
```powershell
# Configure WinRM for HTTP (port 5985)
winrm quickconfig -transport:http

# Configure basic authentication
Set-Item -Path WSMan:\localhost\Service\Auth\Basic -Value $true

# Configure WinRM to allow unencrypted traffic (for basic auth)
Set-Item -Path WSMan:\localhost\Service\AllowUnencrypted -Value $true

# Configure WinRM to allow credentials delegation
Set-Item -Path WSMan:\localhost\Service\Auth\CredSSP -Value $true
```

#### 1.3 Configure Windows Firewall
```powershell
# Allow HTTP WinRM through Windows Firewall
New-NetFirewallRule -DisplayName "Windows Remote Management (HTTP-In)" -Profile Any -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5985

# Allow HTTPS WinRM (optional, for enhanced security)
New-NetFirewallRule -DisplayName "Windows Remote Management (HTTPS-In)" -Profile Any -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5986
```

#### 1.4 Restart WinRM Service
```powershell
# Restart WinRM service
Restart-Service WinRM

# Verify WinRM is running
Get-Service WinRM

# Test WinRM locally
Test-WSMan -ComputerName localhost
```

### Step 2: Create Admin User (if needed)

```powershell
# Create a new administrator user
New-LocalUser -Name "admin" -Password (ConvertTo-SecureString "YourSecurePassword123!" -AsPlainText -Force) -FullName "Administrator" -Description "Admin user for Ansible"

# Add user to Administrators group
Add-LocalGroupMember -Group "Administrators" -Member "admin"

# Verify user creation
Get-LocalUser -Name "admin"
```

### Step 3: Verify IIS Installation

```powershell
# Check if IIS is installed
Get-WindowsFeature -Name Web-Server

# Install IIS if not present
Install-WindowsFeature -Name Web-Server -IncludeManagementTools

# Check IIS service status
Get-Service -Name W3SVC

# Start IIS if not running
Start-Service -Name W3SVC
```

## ☁️ Google Cloud Platform (GCP) Firewall Setup

### Step 1: Install Google Cloud CLI

```bash
# macOS (using Homebrew)
brew install google-cloud-sdk

# Ubuntu/Debian
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
echo "deb https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
sudo apt-get update && sudo apt-get install google-cloud-cli

# Windows
# Download from: https://cloud.google.com/sdk/docs/install
```

### Step 2: Authenticate with GCP

```bash
# Login to your Google account
gcloud auth login

# Set your project ID
gcloud config set project YOUR_PROJECT_ID

# Verify current project
gcloud config get-value project
```

### Step 3: Create Firewall Rules

#### 3.1 Create WinRM HTTP Rule (Port 5985)
```bash
# Create firewall rule for WinRM HTTP
gcloud compute firewall-rules create allow-winrm-http \
    --allow tcp:5985 \
    --source-ranges 0.0.0.0/0 \
    --description "Allow WinRM HTTP connections" \
    --direction INGRESS
```

#### 3.2 Create WinRM HTTPS Rule (Port 5986) - Optional
```bash
# Create firewall rule for WinRM HTTPS
gcloud compute firewall-rules create allow-winrm-https \
    --allow tcp:5986 \
    --source-ranges 0.0.0.0/0 \
    --description "Allow WinRM HTTPS connections" \
    --direction INGRESS
```

#### 3.3 Create RDP Rule (Port 3389) - For VM Access
```bash
# Create firewall rule for RDP
gcloud compute firewall-rules create allow-rdp \
    --allow tcp:3389 \
    --source-ranges 0.0.0.0/0 \
    --description "Allow RDP connections" \
    --direction INGRESS
```

### Step 4: Verify Firewall Rules

```bash
# List all firewall rules
gcloud compute firewall-rules list

# Check specific rule
gcloud compute firewall-rules describe allow-winrm-http

# Test connectivity to your VM
nc -zv YOUR_VM_IP 5985
```

### Step 5: Apply Firewall Rules to VM (if using network tags)

If your VM uses network tags, apply the rules:

```bash
# Add network tag to your VM
gcloud compute instances add-tags YOUR_VM_NAME \
    --tags winrm-enabled

# Update firewall rule to target specific VMs
gcloud compute firewall-rules update allow-winrm-http \
    --target-tags winrm-enabled
```

## 🧪 Testing the Setup

### Step 1: Test WinRM Connection

From your host machine, test the WinRM connection:

```bash
# Test basic connectivity with environment variables
export OBJC_DISABLE_INITIALIZE_FORK_SAFETY=YES
export ANSIBLE_FORKS=1

# Test Ansible connectivity
ansible all -i "YOUR_VM_IP," -m win_ping -e "ansible_user=admin ansible_password='YOUR_PASSWORD' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985"
```

### Step 2: Test IIS Management

```bash
# Check IIS status
ansible all -i "YOUR_VM_IP," -m win_service -a "name=W3SVC" -e "ansible_user=admin ansible_password='YOUR_PASSWORD' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985"

# Start IIS
ansible all -i "YOUR_VM_IP," -m win_service -a "name=W3SVC state=started" -e "ansible_user=admin ansible_password='YOUR_PASSWORD' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985"
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
- Check firewall rules (both Windows and GCP)
- Ensure credentials are correct
- Test with basic Ansible command first

#### 3. GCP Firewall Issues
```bash
# Check if firewall rules exist
gcloud compute firewall-rules list --filter="name~allow-winrm"

# Check VM network configuration
gcloud compute instances describe YOUR_VM_NAME --zone=YOUR_ZONE

# Test network connectivity
nc -zv YOUR_VM_IP 5985
```

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

# Check IIS status
Get-Service -Name W3SVC | Select-Object Name, Status, StartType, DisplayName
```

#### On Host Machine
```bash
# Test Ansible connectivity
ansible all -i "YOUR_VM_IP," -m win_ping -e "ansible_user=admin ansible_password='YOUR_PASSWORD' ansible_connection=winrm ansible_winrm_server_cert_validation=ignore ansible_winrm_transport=basic ansible_port=5985"

# Check application logs
tail -f logs/application.log

# Test network connectivity
nc -zv YOUR_VM_IP 5985
```

## 🚀 Running the Application

### Step 1: Clone and Build
```bash
# Clone the repository
git clone <repository-url>
cd ansible-ping

# Build the application
mvn clean install
```

### Step 2: Run the Application
```bash
# Run with Maven
mvn spring-boot:run

# Or run the JAR file
java -jar target/ansible-ping-0.0.1-SNAPSHOT.jar
```

### Step 3: Access the Web Interface
Open your browser and navigate to: `http://localhost:8080`

### Step 4: Configure VM Details
1. Enter your Windows VM IP address
2. Enter the admin username
3. Enter the admin password
4. Test connectivity using the "Ping VM" button

## 📝 API Endpoints

- `POST /ping-vm` - Test VM connectivity
- `POST /check-iis-status` - Check IIS service status
- `POST /start-iis` - Start IIS service
- `POST /stop-iis` - Stop IIS service
- `POST /restart-iis` - Restart IIS service
- `GET /monitor/status` - Get monitoring status
- `POST /monitor/enable` - Enable auto-monitoring
- `POST /monitor/disable` - Disable auto-monitoring
- `POST /monitor/check-now` - Trigger immediate status check

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