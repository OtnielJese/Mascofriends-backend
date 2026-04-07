# Vetivet Backend - Guía de Configuración de Variables de Entorno

## ✅ Cambios Realizados

Se han removido todos los valores sensibles hardcodeados del repositorio y reemplazado por variables de entorno. Los cambios incluyen:

1. **`src/main/resources/application.properties`** — Actualizado para leer desde variables de entorno
2. **`.env.example`** — Archivo plantilla con todas las variables que necesitas
3. **`src/main/resources/application-local.properties`** — Archivo para desarrollo local (gitignored)

## 📋 Variables de Entorno Requeridas

### Críticas (DEBES establecerlas):

```
JWT_SECRET=tu_clave_secreta_jwt_minimo_32_caracteres
WHATSAPP_NUMBER=+51914867621
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### Opcionales (tienen valores por defecto seguros):

```
SPRING_PROFILE=local
PORT=8080
JWT_EXPIRATION=86400000
SPRING_DATASOURCE_URL=jdbc:h2:mem:vetivetdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=
SPRING_H2_CONSOLE_ENABLED=true
WHATSAPP_DEFAULT_MESSAGE=Hola! Me gustaría agendar una cita para mi mascota.
```

## 🔧 Cómo Establecer Variables en Windows (cmd.exe)

### Opción 1: Para la sesión actual (temporal)

Abre una terminal `cmd.exe` y ejecuta:

```cmd
set JWT_SECRET=tu_clave_secreta_minimo_32_caracteres
set WHATSAPP_NUMBER=+51914867621
set CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

mvn spring-boot:run
```

O si ejecutas el JAR directamente:

```cmd
set JWT_SECRET=tu_clave_secreta_minimo_32_caracteres
set WHATSAPP_NUMBER=+51914867621
set CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

java -jar target\vetivet-backend-1.0.0.jar
```

### Opción 2: Variables de entorno del sistema (permanente)

En `cmd.exe` con permisos de administrador:

```cmd
setx JWT_SECRET "tu_clave_secreta_minimo_32_caracteres"
setx WHATSAPP_NUMBER "+51914867621"
setx CORS_ALLOWED_ORIGINS "http://localhost:5173,http://localhost:3000"
```

**Nota:** `setx` requiere abrir una nueva terminal para que las variables sean visibles.

### Opción 3: Archivo `.env.local` (Recomendado para desarrollo)

1. Copia el archivo `.env.example` a `.env.local`:
   ```cmd
   copy .env.example .env.local
   ```

2. Edita `.env.local` con tus valores:
   ```ini
   JWT_SECRET=tu_clave_secreta_minimo_32_caracteres
   WHATSAPP_NUMBER=+51914867621
   CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
   ```

3. Establece las variables en la sesión:
   ```cmd
   for /f "tokens=1,2 delims==" %a in (.env.local) do set "%a=%b"
   ```

4. Ejecuta la aplicación:
   ```cmd
   mvn spring-boot:run
   ```

**Importante:** `.env.local` está en `.gitignore`, así que no se comiteará al repositorio.

## 🐳 Docker

Si ejecutas con Docker:

```bash
docker run -e JWT_SECRET="tu_clave_secreta" \
           -e WHATSAPP_NUMBER="+51914867621" \
           -e CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000" \
           -p 8080:8080 \
           tu_imagen:latest
```

O en `docker-compose.yml`:

```yaml
services:
  vetivet-backend:
    image: tu_imagen:latest
    ports:
      - "8080:8080"
    environment:
      JWT_SECRET: tu_clave_secreta
      WHATSAPP_NUMBER: +51914867621
      CORS_ALLOWED_ORIGINS: http://localhost:5173,http://localhost:3000
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/vetivet
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: tu_password_db
```

## 🔑 Generar una Clave JWT Segura

Usa OpenSSL para generar una clave fuerte:

```bash
openssl rand -base64 32
```

Ejemplo de salida:
```
5mK9jL2pQr3sT4uV5wX6yZ7aB8cD9eF0gH1iJ2kL3mN4oP5qR6sT7uV8wX9yZ0aB
```

## 📊 Para Producción con Supabase

Si vas a usar Supabase en producción, establece también:

```
SPRING_PROFILE=supabase
SPRING_DATASOURCE_URL=jdbc:postgresql://host.supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=tu_supabase_password
```

El perfil `application-supabase.properties` está configurado para leer estas variables.

## 📊 Para Producción con Azure

Si vas a usar Azure SQL en producción, establece también:

```
SPRING_PROFILE=azure
SPRING_DATASOURCE_URL=jdbc:sqlserver://tu-server.database.windows.net:1433;database=tu-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30
SPRING_DATASOURCE_USERNAME=tu_username
SPRING_DATASOURCE_PASSWORD=tu_password
```

El perfil `application-azure.properties` está configurado para leer estas variables.

## ✅ Validación

Después de establecer las variables, verifica que la aplicación inicie correctamente:

```cmd
set JWT_SECRET=tu_clave_secreta
mvn spring-boot:run
```

Deberías ver en los logs:
```
Vetivet Application Started Successfully
Server running on port 8080
```

## 🛡️ Mejores Prácticas

1. **Nunca** comitas archivos `.env`, `.env.local` o archivos con credenciales reales
2. **Genera** una clave JWT única para cada entorno (dev, staging, prod)
3. **Usa** un gestor de secretos en producción (Azure Key Vault, AWS Secrets Manager)
4. **Rota** periódicamente el JWT_SECRET y otras credenciales
5. **Protege** tus variables de entorno con acceso restrictivo en el servidor

## 📝 Archivos Modificados

- ✅ `src/main/resources/application.properties` — Variables sin defaults sensibles
- ✅ `src/main/resources/application-local.properties` — NUEVO (gitignored)
- ✅ `.env.example` — NUEVO (plantilla de variables)
- ✅ `.gitignore` — Ya contiene protecciones correctas

## 🚀 Próximos Pasos

1. Cierra la aplicación si está en ejecución
2. Establece las variables de entorno según tu entorno (dev/prod)
3. Recompila: `mvn clean package`
4. Ejecuta: `java -jar target/vetivet-backend-1.0.0.jar`
5. Verifica que todo funcione correctamente

¿Preguntas? Revisa el archivo `.env.example` para más detalles sobre cada variable.

