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

> **Advertencia:** eliminar `postgres_data` elimina los datos almacenados en esa instancia de PostgreSQL. Hazlo solamente si no necesitas conservarlos.

### Estructura de acceso

Al finalizar la configuración, la arquitectura local queda así:

```text
                    ┌──────────────────────┐
                    │   Microservicio      │
                    │      Identity        │
                    │                      │
                    │      :8081           │
                    └──────────┬───────────┘
                               │
                               │ localhost:5432
                               ▼
                    ┌──────────────────────┐
                    │      PostgreSQL      │
                    │                      │
                    │     identity_db      │
                    │        :5432         │
                    └──────────▲───────────┘
                               │
                               │ Docker Network
                               │ identity-postgres-db
                               │
                    ┌──────────┴───────────┐
                    │       pgAdmin 4      │
                    │                      │
                    │       :5050          │
                    └──────────────────────┘
```