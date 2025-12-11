Deployment checklist for Railway

This file documents the minimal steps to deploy this Spring Boot backend to Railway using the included Dockerfile.

Prerequisites
- Railway account and project
- GitHub/Git repo connected to Railway (this repo)
- Oracle wallet zip (if you use Oracle Autonomous DB) - keep the file private

Steps
1) Push branch with latest changes
   git push origin feature/leonardo-login

2) Connect repo in Railway
   - New Project -> Deploy from GitHub
   - Select this repo and branch `feature/leonardo-login`
   - Railway should detect the Dockerfile at the repo root and use it to build the image

3) Add environment variables in Railway (Service -> Settings -> Variables)
   - SPRING_PROFILES_ACTIVE = prod
   - SPRING_DATASOURCE_URL = jdbc:oracle:thin:@<your_tns_alias_or_connect_string>
   - SPRING_DATASOURCE_USERNAME = <your_db_user>
   - SPRING_DATASOURCE_PASSWORD = <your_db_password>
   - ORACLE_WALLET_BASE64 = <base64-encoded wallet zip> (optional but recommended for Oracle wallet)
       * Create base64 locally (PowerShell):
         $bytes = [System.IO.File]::ReadAllBytes('C:\path\to\wallet.zip')
         [System.Convert]::ToBase64String($bytes) | Out-File -Encoding ASCII wallet.b64.txt
       * Copy the file content and paste into Railway variable value
   - JAVA_TOOL_OPTIONS = -Dserver.port=8085 -Xms128m -Xmx256m -XX:MaxRAMPercentage=50.0

4) Configure Health Check (important)
   - Path: /healthcheck
   - Method: GET
   - Interval: 10s, Timeout: 3s, Failure threshold: 3
   Reason: this endpoint is lightweight and does not depend on DB to report readiness.

5) Deploy
   - Trigger a manual deploy in Railway or push a new commit to trigger auto-deploy

6) Monitor logs
   - Watch build logs and runtime logs in the Railway console
   - Expected startup log lines:
     * "Oracle wallet extracted to ..." (if using ORACLE_WALLET_BASE64)
     * "Tomcat initialized with port 8085"
     * "HikariPool-1 - Start completed"

7) If container is KILLED or restarts repeatedly
   - Likely OOM. Options:
     * Increase Railway service memory plan
     * Reduce JVM heap further via JAVA_TOOL_OPTIONS (e.g. -Xms64m -Xmx128m)

Notes
- The Dockerfile is based on Debian (jammy) to avoid glibc/musl issues with Oracle JDBC.
- The Oracle wallet loader extracts a base64-encoded zip to the container temporary folder and sets `oracle.net.tns_admin` as a system property.
- Do not commit the actual wallet files to the repo. Use Railway env vars or secure storage.

If you want, I can walk you through the Railway UI steps live or run another commit to tweak JVM settings.