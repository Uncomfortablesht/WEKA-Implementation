# ✅ Deployment Checklist for Render

## 📦 What You're Deploying

**Only the `weka-service` folder** - That's it! Everything is included.

---

## ✅ Files Included (All Ready!)

- ✅ `pom.xml` - Maven configuration with dependencies
- ✅ `render.yaml` - Render deployment config
- ✅ `README.md` - Documentation
- ✅ `.gitignore` - Git ignore rules
- ✅ `src/main/java/com/clustering/` - All Java source files:
  - `WekaServiceApplication.java` - Main application
  - `ClusteringController.java` - REST API endpoints
  - `ClusteringService.java` - WEKA clustering logic
  - `ClusteringRequest.java` - Request model
  - `ClusteringResponse.java` - Response model
- ✅ `src/main/resources/application.properties` - Spring Boot config

---

## 🚀 Deployment Steps

### 1. Initialize Git (if not done)
```bash
cd weka-service
git init
git add .
git commit -m "Initial WEKA service for Render"
```

### 2. Create GitHub Repository
- Go to https://github.com/new
- Name: `weka-clustering-service`
- Make it **Public** (required for Render free tier)
- Click "Create repository"

### 3. Push to GitHub
```bash
git remote add origin https://github.com/YOUR_USERNAME/weka-clustering-service.git
git branch -M main
git push -u origin main
```

### 4. Deploy to Render
- Go to https://render.com
- Sign up/Login (free)
- Click "New +" → "Web Service"
- Connect GitHub → Select `weka-clustering-service`
- Render will auto-detect:
  - ✅ Environment: Java
  - ✅ Build Command: `mvn clean install`
  - ✅ Start Command: `java -jar target/weka-service-1.0.0.jar`
- Click "Create Web Service"
- Wait ~5-10 minutes for first build

### 5. Get Your URL
After deployment: `https://weka-clustering-service.onrender.com/api`

---

## ✅ That's It!

You only need to deploy the `weka-service` folder. Everything else (PHP files, database, etc.) stays on Hostinger.

---

## 🔗 After Deployment

Update your PHP file:
```php
// In api/clustering-weka-render.php, line 15
define('RENDER_WEKA_API_URL', 'https://your-service.onrender.com/api');
```

---

## 🎉 You're Done!

The `weka-service` folder is a complete, standalone Spring Boot application ready for Render!

