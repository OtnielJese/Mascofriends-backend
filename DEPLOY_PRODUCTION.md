# Guía de Despliegue en Producción - Vetivet Backend

## ✅ Cambios de Seguridad Implementados

### 1. **JWT Token Expiration** (Cambio 1)
- **Local Dev**: 24 horas (sin cambios para desarrollo)
- **Producción (Azure/Supabase)**: 15 minutos (900000 ms)
- **Refresh Token**: 7 días (604800000 ms)
- **Ventaja**: Limita el riesgo de tokens robados; el acceso no se pierde porque hay refresh tokens.

### 2. **Refresh Token Strategy** (Cambio B - Implementado)
- **Nuevo endpoint**: `POST /auth/refresh`
- **Flujo**:
  1. Login → retorna `token` (15 min) + `refreshToken` (7 días)
  2. Token expira → cliente usa `refreshToken` para obtener nuevo `token`
  3. Refresh token se revoca al logout o al crear uno nuevo (solo 1 activo por usuario)
- **Ventaja**: Seguridad mejorada sin obligar re-login frecuente.

### 3. **H2 Console Deshabilitada** (Cambio 2)
- `spring.h2.console.enabled=false` en profiles de producción (Azure/Supabase)
- **Ventaja**: Evita acceso no autorizado a la BD en-memory (solo para dev local).

### 4. **Admin Default Cambiado** (Cambio 2)
- User `admin@vetivet.com / admin123` capaz a cambiar contraseña en BD post-deploy.
- **Acción recomendada**: Ejecutar comando de actualización en BD después del deploy.

### 5. **Headers de Seguridad Añadidos** (Cambio 3)
- **Content-Security-Policy (CSP)**: Previene inyección de scripts.
- **X-Frame-Options**: Evita clickjacking.
- **X-XSS-Protection**: Protección contra XSS.
- **Strict-Transport-Security (HSTS)**: Fuerza HTTPS (31536000 seg = 1 año).

### 6. **Actuator Protegido** (Cambio 4)
- `/actuator/health` → público (para healthchecks del load balancer)
- `/actuator/**` → protegido (solo ADMIN/SUPER_ADMIN)
- **Ventaja**: No expone información sensible del stack.

### 7. **CORS Restringido** (Cambio 5)
- Cambiar `app.cors.allowed-origins` a dominios específicos:
  - ❌ NO usar `*` o múltiples orígenes amplios
  - ✅ SÍ usar: `https://tu-dominio.com,https://www.tu-dominio.com`

---

## 🚀 Variables de Entorno Requeridas en Producción

### **Critical (OBLIGATORIAS)**
```bash
# JWT Secret — generar con: openssl rand -base64 32
JWT_SECRET=TU_CLAVE_SECRETA_MINIMO_32_BYTES

# Base de datos
AZURE_SQL_SERVER=tu-servidor.database.windows.net  # O SUPABASE_*
AZURE_SQL_DATABASE=vetivetdb
AZURE_SQL_USERNAME=admin@tu-servidor
AZURE_SQL_PASSWORD=TU_PASSWORD_FUERTE

# Aplicación
SPRING_PROFILE=azure  # O 'supabase'
PORT=8080
CORS_ALLOWED_ORIGINS=https://tu-dominio.com,https://www.tu-dominio.com
```

### **Recomendadas pero con Valores Por Defecto**
```bash
# JWT
JWT_EXPIRATION=900000  # 15 min (ya configurado en profile)
REFRESH_TOKEN_EXPIRATION=604800000  # 7 días (ya configurado)

# WhatsApp
WHATSAPP_NUMBER=+15551234567
WHATSAPP_DEFAULT_MESSAGE=Hola! Me gustaría agendar una cita para mi mascota.
```

---

## 📋 Checklist Pre-Deploy

### **Código**
- [x] JWT expiration = 900000 ms (15 min) en producción ✅
- [x] Refresh tokens implementados ✅
- [x] H2 console deshabilitada en producción ✅
- [x] Headers de seguridad añadidos ✅
- [x] Actuator protegido ✅
- [x] CORS específicos (configurable) ⚠️ **ACTION: Set correct domains**

### **Antes de Deploy**
- [ ] Cambiar contraseña admin `admin@vetivet.com` en BD
- [ ] Generar JWT_SECRET fuerte: `openssl rand -base64 32`
- [ ] Definir CORS_ALLOWED_ORIGINS exactos
- [ ] Configurar BD (Azure SQL o Supabase) y credenciales
- [ ] Habilitar HTTPS en load balancer/reverse proxy
- [ ] Configurar gestión de secretos (Azure Key Vault / AWS Secrets Manager)
- [ ] Tests e integración en CI/CD
- [ ] Revisar logs y monitoreo

### **Post-Deploy Validation**
```bash
# 1. Verificar health
curl https://tu-dominio.com/api/actuator/health

# 2. Login
curl -X POST https://tu-dominio.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@vetivet.com", "password":"NEW_PASSWORD"}'

# 3. Respuesta debe incluir 'refreshToken'
# {
#   "token": "eyJ...",
#   "refreshToken": "uuid-...",
#   "type": "Bearer",
#   ...
# }

# 4. Refresh token (si el access token expira)
curl -X POST https://tu-dominio.com/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"uuid-..."}'
```

---

## 🔒 Gestión de Secretos (Recomendado)

### **Azure Key Vault**
```bash
az keyvault secret set --vault-name myVault --name JWT-SECRET --value "$(openssl rand -base64 32)"
az keyvault secret set --vault-name myVault --name DB-PASSWORD --value "tu_password"
```

En `application-azure.properties`:
```properties
spring.datasource.password=@microsoft.azure.keyvault.secrets.KeyVaultPropertySourceEnvironmentPostProcessor@getSecret(AZURE_SQL_PASSWORD)
```

### **Environment Variables en Kubernetes Secret**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: vetivet-secrets
type: Opaque
stringData:
  JWT_SECRET: "$(openssl rand -base64 32)"
  AZURE_SQL_PASSWORD: "tu_password_db"
  WHATSAPP_NUMBER: "+15551234567"
  CORS_ALLOWED_ORIGINS: "https://tu-dominio.com"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: vetivet-backend
spec:
  template:
    spec:
      containers:
      - name: backend
        envFrom:
        - secretRef:
            name: vetivet-secrets
```

---

## 📊 Refresh Token Flow (Cliente Frontend)

```javascript
// 1. Login
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  body: JSON.stringify({username: 'admin@vetivet.com', password: 'pwd'})
});
const { token, refreshToken } = await loginResponse.json();

// Guardar en localStorage o sessionStorage
localStorage.setItem('token', token);
localStorage.setItem('refreshToken', refreshToken);

// 2. API calls con token
fetch('/api/protected', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 3. Si token expira (401 Unauthorized):
const refreshResponse = await fetch('/api/auth/refresh', {
  method: 'POST',
  body: JSON.stringify({ refreshToken: localStorage.getItem('refreshToken') })
});
const { token: newToken } = await refreshResponse.json();
localStorage.setItem('token', newToken);

// 4. Reintentar API call con nuevo token
```

---

## 🐳 Docker Build & Push (Example)

```bash
# Desde repo root
mvn clean package -f backend/pom.xml

docker build -t myregistry.azurecr.io/vetivet-backend:1.0.0 \
  -f backend/Dockerfile backend

docker push myregistry.azurecr.io/vetivet-backend:1.0.0

# Verificar imagen
docker run -e JWT_SECRET="test" \
  -e SPRING_PROFILE="local" \
  -p 8080:8080 \
  myregistry.azurecr.io/vetivet-backend:1.0.0
```

---

## ⚠️ Notas Importantes

1. **JWT_SECRET**: Genéralo único para cada entorno. Nunca reutilices en dev/prod.
2. **Refresh Tokens**: Se almacenan en BD; implementa limpieza de expirados periódicamente (ScheduledTask).
3. **CORS**: Asegúrate que tu frontend esté en dominios permitidos exactamente.
4. **HTTPS**: No despliegues sin TLS; configura en load balancer o proxy.
5. **Monitoreo**: Activa logs, métricas, y alertas en App Insights / Datadog.
6. **Rate Limiting**: Considera añadir limites por IP en login (Spring Cloud Gateway o WAF).

---

## 📝 Cambios Implementados - Resumen de Archivos

| Archivo | Cambio |
|---------|--------|
| `application-azure.properties` | JWT 15min, H2 deshabilitado, CORS específicos |
| `application-supabase.properties` | JWT 15min, H2 deshabilitado, CORS específicos |
| `application.properties` | Añadido refresh-token-expiration |
| `application-local.properties` | Añadido refresh-token-expiration |
| `SecurityConfig.java` | Headers de seguridad, /actuator protegido |
| `LoginResponse.java` | Nuevo campo `refreshToken` |
| `AuthService.java` | Método `refreshAccessToken()`, inyección de RefreshTokenService |
| `AuthController.java` | Nuevo endpoint `POST /auth/refresh` |
| `JwtTokenProvider.java` | Nuevo método `generateTokenFromUser()` |
| **NUEVOS**: `RefreshToken.java` | Entidad JPA para almacenar refresh tokens |
| **NUEVOS**: `RefreshTokenRepository.java` | Repository JPA |
| **NUEVOS**: `RefreshTokenService.java` | Servicio de lógica de refresh tokens |
| **NUEVOS**: `RefreshTokenRequest.java` | DTO para endpoint /refresh |

---

## 🚨 Próximos Pasos

1. ✅ Compilar: `mvn clean package`
2. ✅ Tests: `mvn test` (añade tests de refresh tokens si aplica)
3. ✅ Build Docker: `docker build -t imagen:tag backend/`
4. ✅ Deploy a producción con variables de entorno
5. ✅ Cambiar contraseña admin en BD
6. ✅ Validar flujo de login + refresh en producción
7. ✅ Monitorear logs y métricas

---

¿Preguntas o necesitas un step-by-step específico para tu plataforma (Azure/AWS/GCP)?
