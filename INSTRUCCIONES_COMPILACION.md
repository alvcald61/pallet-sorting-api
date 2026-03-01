# 🔧 Instrucciones de Compilación

## Problema
El proyecto requiere **Java 17+** pero Maven está configurado con Java 11.

## ✅ Solución Rápida

### Opción 1: Usar el script batch (Windows)
```bash
# Ejecutar desde el directorio del proyecto
compile.bat
```

Este script configura automáticamente Java 17 y compila el proyecto.

---

### Opción 2: Configurar JAVA_HOME permanentemente

#### En Windows (Recomendado):

1. **Abrir Variables de Entorno:**
   - Presiona `Win + R`
   - Escribe `sysdm.cpl` y presiona Enter
   - Ve a la pestaña "Opciones avanzadas"
   - Click en "Variables de entorno"

2. **Configurar JAVA_HOME:**
   - En "Variables del sistema", click en "Nueva"
   - Nombre: `JAVA_HOME`
   - Valor: `C:\Program Files\Java\jdk-17.0.2`
   - Click en "Aceptar"

3. **Actualizar PATH:**
   - Busca la variable `Path` en "Variables del sistema"
   - Click en "Editar"
   - Asegúrate que `%JAVA_HOME%\bin` esté al inicio de la lista
   - Click en "Aceptar"

4. **Reiniciar terminal** y verificar:
```bash
java -version    # Debe mostrar Java 17
mvn -version     # Debe usar Java 17
```

---

### Opción 3: Usar Java 24

Si prefieres usar Java 24 (más reciente), cambia:
- `JAVA_HOME=C:\Program Files\Java\jdk-24`
- En `pom.xml`: `<java.version>24</java.version>`

---

## 🚀 Compilar el Proyecto

```bash
# Compilar sin tests
mvn clean compile -DskipTests

# Compilar con tests
mvn clean test

# Empaquetar
mvn clean package

# Ejecutar aplicación
mvn spring-boot:run
```

---

## 📝 Verificación

Después de configurar JAVA_HOME, ejecuta:
```bash
mvn -version
```

Debes ver algo como:
```
Apache Maven 3.8.4
Maven home: C:\Program Files\apache-maven-3.8.4
Java version: 17.0.2, vendor: Oracle Corporation
Java home: C:\Program Files\Java\jdk-17.0.2
```

---

## ❌ Si Maven sigue usando Java 11

Edita el archivo de Maven:
```
C:\Program Files\apache-maven-3.8.4\conf\settings.xml
```

Agrega dentro de `<profiles>`:
```xml
<profile>
  <id>java-17</id>
  <activation>
    <activeByDefault>true</activeByDefault>
  </activation>
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>
</profile>
```

---

## 🔍 Versiones de Java Disponibles en tu Sistema

- ✅ Java 8 (jdk1.8.0_311)
- ✅ Java 11 (jdk-11.0.2) ← Maven usa este por defecto
- ✅ **Java 17 (jdk-17.0.2)** ← LTS, Recomendado
- ✅ Java 24 (jdk-24) ← Más reciente

---

## 💡 Recomendación

Usa **Java 17** porque:
- Es LTS (Long Term Support)
- Estable y maduro
- Soporta todas las características del código
- Compatible con Spring Boot 3.x

---

**Última actualización:** 7 de febrero de 2026
