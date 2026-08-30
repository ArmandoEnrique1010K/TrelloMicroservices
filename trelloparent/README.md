# Generación de claves RSA para JWT

El microservicio `identity` utiliza un par de claves RSA para firmar y validar los tokens JWT:

- `private-key.pem`: clave privada utilizada para **firmar** los JWT.
- `public-key.pem`: clave pública utilizada para **verificar** los JWT.

> **Importante:** nunca compartas ni subas `private-key.pem` al repositorio. La clave privada debe mantenerse protegida.

## 1. Instalar OpenSSL

Si OpenSSL todavía no está instalado, abre PowerShell y ejecuta:

```powershell
winget install openssl
```

Cuando se solicite, escribe `Y` para aceptar los términos y condiciones.

Después de la instalación, **cierra PowerShell y abre una nueva terminal**.

Comprueba que OpenSSL está instalado:

```powershell
winget list openssl
```

Deberías obtener una salida similar a:

```text
Name                   Id                       Version Source
---------------------------------------------------------------
OpenSSL 4.0.1 (64-bit) ShiningLight.OpenSSL.Dev 4.0.1   winget
```

## 2. Verificar que OpenSSL esté disponible en el PATH

Ejecuta:

```powershell
where.exe openssl
```

Si no encuentra el ejecutable, puedes localizarlo con:

```powershell
Get-ChildItem "C:\Program Files" -Filter openssl.exe -Recurse -ErrorAction SilentlyContinue
```

La instalación de OpenSSL utilizada por el proyecto debe encontrarse en:

```text
C:\Program Files\OpenSSL-Win64\bin\openssl.exe
```

Comprueba directamente su versión:

```powershell
& "C:\Program Files\OpenSSL-Win64\bin\openssl.exe" version
```

Deberías obtener:

```text
OpenSSL 4.0.1 9 Jun 2026 (Library: OpenSSL 4.0.1 9 Jun 2026)
```

## 3. Agregar OpenSSL al PATH

Si el comando `openssl` no es reconocido directamente desde PowerShell, agrega la siguiente ruta a las variables de entorno:

```text
C:\Program Files\OpenSSL-Win64\bin
```

Pasos:

1. Presiona `Windows + R`.
2. Ejecuta `sysdm.cpl`.
3. Ve a **Opciones avanzadas**.
4. Selecciona **Variables de entorno**.
5. En **Variables de usuario**, selecciona `Path`.
6. Pulsa **Editar**.
7. Pulsa **Nuevo**.
8. Agrega:

```text
C:\Program Files\OpenSSL-Win64\bin
```

9. Acepta todas las ventanas.
10. Cierra todas las terminales abiertas.
11. Abre una nueva terminal de PowerShell.

Finalmente, verifica:

```powershell
openssl version
```

y:

```powershell
where.exe openssl
```

La segunda orden debería mostrar:

```text
C:\Program Files\OpenSSL-Win64\bin\openssl.exe
```

## 4. Generar las claves RSA

Ubícate en el directorio `keys` del microservicio `identity`:

```powershell
cd businessdomain\identity\keys
```

Genera la **clave privada**:

```powershell
openssl genrsa -out private-key.pem 2048
```

Luego genera la **clave pública** a partir de la clave privada:

```powershell
openssl rsa -in private-key.pem -pubout -out public-key.pem
```

El directorio debe quedar de esta manera:

```text
businessdomain/
└── identity/
    └── keys/
        ├── private-key.pem
        └── public-key.pem
```

## 5. Verificar las claves

Puedes comprobar que la clave privada se haya generado correctamente con:

```powershell
openssl rsa -in private-key.pem -check
```

Deberías obtener:

```text
RSA key ok
```

También puedes comprobar la clave pública:

```powershell
openssl rsa -pubin -in public-key.pem -text -noout
```

## 6. No subir la clave privada a Git

Agrega la clave privada al `.gitignore`:

```gitignore
# JWT RSA keys
businessdomain/identity/keys/private-key.pem
```

Si las claves se generan únicamente para desarrollo local, también puedes ignorar todo el directorio:

```gitignore
businessdomain/identity/keys/*.pem
```

En producción, la clave privada debería gestionarse mediante un mecanismo seguro de secretos y **no almacenarse directamente en el repositorio**.

# Configuración de la infraestructura

El microservicio **Identity** utiliza PostgreSQL como sistema de gestión de base de datos y pgAdmin 4 como herramienta para su administración.

La infraestructura se encuentra definida en `docker-compose.yml`.

## 1. Levantar los contenedores

Ubícate en el directorio donde se encuentra el archivo `docker-compose.yml` y ejecuta:

```bash
docker compose up -d
```

Esto levantará los siguientes servicios:

| Servicio   | Contenedor          | Puerto |
| ---------- | ------------------- | -----: |
| PostgreSQL | `identity-db`       | `5432` |
| pgAdmin 4  | `pgadmin4`          | `5050` |

Puedes comprobar que ambos contenedores estén ejecutándose con:

```bash
docker compose ps
```

También puedes utilizar:

```bash
docker ps
```

## 2. Verificar PostgreSQL

Para comprobar que PostgreSQL se inició correctamente, revisa los logs del contenedor:

```bash
docker logs identity-db
```

El contenedor debe mostrar un mensaje similar a:

```text
database system is ready to accept connections
```

Este mensaje indica que PostgreSQL está ejecutándose y aceptando conexiones.

La configuración utilizada por PostgreSQL es:

```text
Database: identity_db
Username: postgres
Password: admin
Port: 5432
```

> **Importante:** `POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD` solamente se utilizan para inicializar PostgreSQL cuando el directorio de datos está vacío.

## 3. Verificar pgAdmin 4

Para comprobar que pgAdmin se inició correctamente, revisa los logs:

```bash
docker logs pgadmin4
```

Debe aparecer un mensaje similar a:

```text
[INFO] Listening at: http://[::]:80 (1)
```

Esto indica que pgAdmin está escuchando correctamente dentro del contenedor.

## 4. Acceder a pgAdmin

Abre el siguiente enlace en el navegador:

[http://localhost:5050/](http://localhost:5050/)

Utiliza las credenciales definidas en `docker-compose.yml`:

```text
Email:    admin@gmail.com
Password: qwerty
```

## 5. Registrar el servidor PostgreSQL en pgAdmin

Una vez dentro de pgAdmin:

1. En el panel izquierdo, haz clic derecho sobre **Servers**.
2. Selecciona **Register → Server...**.

### Pestaña General

En **Name**, puedes utilizar:

```text
identity-postgres-db
```

Este nombre es solamente un identificador dentro de pgAdmin y puede ser diferente al nombre del contenedor.

### Pestaña Connection

Introduce los siguientes valores:

| Campo                | Valor                  |
| -------------------- | ---------------------- |
| Host name/address    | `identity-postgres-db` |
| Port                 | `5432`                 |
| Maintenance database | `identity_db`          |
| Username             | `postgres`             |
| Password             | `admin`                |

El valor `identity-postgres-db` corresponde al **nombre del servicio de Docker Compose**:

```yaml
identity-postgres-db:
```

No se debe utilizar `localhost` como host cuando pgAdmin y PostgreSQL se encuentran en contenedores diferentes dentro de la misma red de Docker Compose.

Después de introducir los datos, pulsa **Save**.

### La base de datos `project-postgres-db`

Repite nuevamente el procedimiento para registrar el servidor PostgreSQL en pgAdmin, para la nueva base de datos, en la pestaña general agregale en **name** el valor: `project-postgres-db`

Y luego en la pestaña connection:

| Campo                | Valor                 |
| -------------------- | --------------------- |
| Host name/address    | `project-postgres-db` |
| Port                 | `5432`                |
| Maintenance database | `project_db`          |
| Username             | `postgres`            |
| Password             | `admin`               |

## 6. Verificar la base de datos

Una vez registrado el servidor, navega en pgAdmin:

```text
Servers
└── identity-postgres-db
    └── Databases
        └── identity_db
            └── Schemas
                └── public
                    └── Tables
```

Dentro de **Tables** deberían aparecer las tablas creadas por el microservicio Identity.

La creación y actualización de las tablas es gestionada por Hibernate mediante:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Por lo tanto, las tablas se generarán o actualizarán cuando el microservicio Identity establezca correctamente la conexión con PostgreSQL.

## 7. Ejecutar el microservicio Identity

Una vez que PostgreSQL esté ejecutándose, inicia el microservicio Identity desde el IDE o mediante Maven.

La aplicación utiliza la siguiente configuración:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/identity_db?sslmode=disable
spring.datasource.username=postgres
spring.datasource.password=admin
```

Como el microservicio Identity se ejecuta directamente en el equipo local y PostgreSQL está exponiendo el puerto `5432`, se utiliza:

```text
localhost:5432
```

## 8. Acceder a Swagger

Una vez que el microservicio Identity esté ejecutándose, Swagger UI estará disponible en:

[http://localhost:8081/business/v1/swagger-ui/index.html](http://localhost:8081/business/v1/swagger-ui/index.html)

La estructura de la URL corresponde a:

```text
http://localhost
       :8081
       /business/v1
       /swagger-ui/index.html
```

Donde:

* `8081` → puerto del microservicio Identity.
* `/business/v1` → `server.servlet.context-path`.
* `/swagger-ui/index.html` → ruta de Swagger UI.

## 9. Detener los contenedores

Para detener la infraestructura:

```bash
docker compose down
```

Esto detiene y elimina los contenedores, pero **no elimina los datos almacenados en `postgres_data`**.

## 10. Cambiar la configuración inicial de PostgreSQL

Si se modifica alguna de las siguientes propiedades:

```yaml
POSTGRES_USER: postgres
POSTGRES_PASSWORD: admin
POSTGRES_DB: identity_db
```

y se necesita que PostgreSQL realice nuevamente su inicialización, primero se debe detener la infraestructura:

```bash
docker compose down
```

Después, elimina el directorio:

```text
postgres_data/
```

Finalmente, vuelve a levantar los servicios:

```bash
docker compose up -d
```

Esto es necesario porque PostgreSQL **no vuelve a ejecutar su proceso de inicialización mientras el directorio `/var/lib/postgresql/data` ya contenga un clúster existente**.

> ⚠️ **Advertencia:** eliminar `postgres_data` elimina los datos almacenados en esa instancia de PostgreSQL. Hazlo solamente si no necesitas conservarlos.

### Estructura de acceso

Al finalizar la configuración, la arquitectura local queda así:

```text
                    ┌──────────────────────┐                    ┌──────────────────────┐
                    │   Microservicio      │                    │   Microservicio      │
                    │      Identity        │                    │      Project         │
                    │                      │                    │                      │
                    │      :8081           │                    │      :8082           │
                    └──────────┬───────────┘                    └──────────┬───────────┘
                               │                                           │
                               │ localhost:5432                            │ localhost:5433
                               ▼                                           ▼
                    ┌──────────────────────┐                    ┌──────────────────────┐
                    │      PostgreSQL      │                    │      PostgreSQL      │
                    │                      │                    │                      │
                    │     identity_db      │                    │      project_db      │
                    │        :5432         │                    │        :5432         │
                    └──────────▲───────────┘                    └──────────▲───────────┘
                               │                                           │
                               │ Docker Network                            │ Docker Network
                               │ identity-postgres-db                      │ project-postgres-db
                               │                                           │
                    ┌──────────┴───────────┐                               │
                    │       pgAdmin 4      │                               │
                    │                      │───────────────────────────────┘
                    │       :5050          │
                    └──────────────────────┘
```


# Comprobar un password BCrypt

Después de registrar un usuario desde Swagger, puedes consultar el password almacenado en PostgreSQL mediante pgAdmin4.

El valor almacenado será un hash BCrypt, por ejemplo:

```text
$2a$10$...
```

Para comprobar si una contraseña corresponde con ese hash puedes utilizar:

[BCrypt Generator](https://bcrypt-generator.com/?utm_source=chatgpt.com)

Introduce:

- **Password:** la contraseña original utilizada durante el registro.
- **Hash:** el valor BCrypt almacenado en la columna `password` de PostgreSQL.

> ⚠️ No utilices contraseñas reales o sensibles en servicios web de terceros. Para pruebas de desarrollo utiliza únicamente contraseñas ficticias.