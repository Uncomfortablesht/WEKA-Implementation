# WEKA Clustering Service for Render

A Spring Boot REST API service that provides WEKA clustering capabilities.

## 🚀 Quick Deploy to Render

1. **Push to GitHub**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/yourusername/weka-service.git
   git push -u origin main
   ```

2. **Deploy on Render**
   - Go to https://render.com
   - Click "New +" → "Web Service"
   - Connect GitHub repository
   - Render will auto-detect Java
   - Click "Create Web Service"
   - Wait for deployment (~5-10 minutes)

3. **Get Your URL**
   - After deployment: `https://weka-clustering-service.onrender.com`
   - Use this URL in your PHP application

## 📝 API Usage

### Cluster Students
```bash
POST /api/cluster
Content-Type: application/json

{
  "students": [
    {
      "user_id": 1,
      "literacy_score": 85.5,
      "math_score": 78.2,
      "games_played": 10,
      "total_score": 500
    }
  ],
  "category": "all",
  "clusters": 3
}
```

### Health Check
```bash
GET /api/health
```

## 🔧 Local Development

```bash
# Build
mvn clean install

# Run
java -jar target/weka-service-1.0.0.jar

# Or with Maven
mvn spring-boot:run
```

## 📦 Dependencies

- Spring Boot 3.1.5
- WEKA 3.8.6
- Java 17

## 🔒 Security Notes

- Update CORS settings in `application.properties` for production
- Add API key authentication if needed
- Use environment variables for sensitive config

