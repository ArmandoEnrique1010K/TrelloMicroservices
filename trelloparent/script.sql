# BASE DE DATOS: identity-postgres-db
SELECT * FROM public.users
ORDER BY id ASC 

# La contraseña de los primeros 4 usuarios es: 12345678B#
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('af6cf6ea-05c3-4233-a546-f942386f43ad', true, '2026-09-10 22:00:18.212341', 'enrique@gmail.com', 'Armando', 'Enrique', '$2a$12$1uBxH6DxdowCMyD7J0IH1.LXQcW4g5CsPabKRM7IVRYIkwNnpvcI2');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('3e54e44f-8f87-445b-8817-8c13010f5da5', true, '2026-09-08 22:00:18.212341', 'example@gmail.com', 'Jhon', 'Doe', '$2a$12$RiKg8xqlTX8SJTCpTdh/x.DlA54baqZTvk7Ggz4p3fzokZiDFhPIi');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('5d3498c0-43f1-4760-a4f0-dc0e97bcb641', true, '2026-04-26 10:17:46', 'gnestor0@gmail.com', 'wnestor0', 'Nestor', '$2a$12$epo4h0tkVXtvgR4u7OOMgOnuOz2GgujpPW56eW6PT8MuWI7sotr8q');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('858d7655-6178-458f-b880-3db6aa3beff3', true, '2026-03-23 05:48:46', 'cmclese1@gmail.com', 'dmclese1', 'McLese', '$2a$12$45KSi0LYjFf0I6u7p4cgs.74/vAFA4LJu8T1HZ6Alixy1euOeJw0.');

# Contraseñas generadas de forma aleatoria
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('3916a4c0-879c-4935-a69f-62e39c3014df', false, '2025-11-12 16:33:51', 'kheaton2@acquirethisname.com', 'aheaton2', 'Heaton', '$2a$04$j6952313TYnQktDfQ4QDJOQ2r.yx1iavLFIAMJkKH8/ZisR0RrKKK');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('26b430c5-37ef-499d-9a31-7369cc7a572f', false, '2026-05-13 18:14:48', 'dgully3@desdev.cn', 'ygully3', 'Gully', '$2a$04$jcX4Yic.cWvAw.97vwlvMuLR7oz7knPfQaI.j8OB/xYl.24/OwiZG');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('a2e507fc-0cf7-4fe2-904e-818a7645d478', false, '2025-11-21 11:42:19', 'bhawkeridge4@aboutads.info', 'whawkeridge4', 'Hawkeridge', '$2a$04$ixnCuEjEJWAJe0S3j40QJOuuyVHd9qqtuEBfLPX1bSUsW.A957HZm');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('48f51b0d-202c-4e5b-9b0a-8ae2df02349e', false, '2025-09-28 07:45:47', 'rhartwell5@ustream.tv', 'ahartwell5', 'Hartwell', '$2a$04$jaNVKP.iapl4SY1rMJKofusVFUPJnU3MURv6GdmV9OL2hQbwYVrP2');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('b483a1cd-9acc-4c89-a211-979d30b8da98', false, '2025-11-25 20:16:56', 'lemney6@istockphoto.com', 'temney6', 'Emney', '$2a$04$R3lr9wUkHvDrdYNSpJwkBefM2m9eOan8yedB2lPJ3pilzYf5OGr4W');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('c54e96c0-fde8-45eb-8923-61e41ce9a248', false, '2026-04-18 18:41:32', 'dgreene7@forbes.com', 'tgreene7', 'Greene', '$2a$04$du.ue3a0jICJDKSA8N/qjOqR0NGTUYmogc.dmA4em4/SShgGYVmlG');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('cbdd2b41-09b4-4e9b-adeb-79ce7dc0abb0', false, '2026-06-01 15:48:06', 'opiddington8@moonfruit.com', 'spiddington8', 'Piddington', '$2a$04$vThykyjtlGacHtN1ZlGW6ekWt2HWcRpjYtcGy/ssHtI13dQDfnYw2');
insert into users (id, confirmed, created_at, email, first_name, last_name, password) values ('81190c5f-8199-4939-a5da-fc0a9241a035', true, '2026-06-22 15:02:44', 'emantha9@gmpg.org', 'mmantha9', 'Mantha', '$2a$04$p1cA.HP5QcIhwtWhcwZbbO2TD0zFlC63IYl10dgg/6qhctVvmCKqS');


# BASE DE DATOS: project-postgres-db
insert into workspaces(id, created_at, description, name, owner_user_id) values ('bf642609-8dbb-44ab-8e50-ccf99346c6ff', '2026-09-11 20:00:00.212341', 'Espacio de trabajo para probar la comunicación entre los 2 microservicios: Project y Workflow', 'Espacio de trabajo de microservicios', 'af6cf6ea-05c3-4233-a546-f942386f43ad');
insert into boards (id, created_at, description, name, workspace_id) values ('6c4f289b-a915-49c6-b18a-9ba2d631834b', '2026-09-11 21:00:00.212341', 'Tablero de prueba del usuario administrador del espacio de trabajo', 'Primer tablero', 'bf642609-8dbb-44ab-8e50-ccf99346c6ff');
insert into boards (id, created_at, description, name, workspace_id) values ('ef777773-49ef-4282-a6f0-c233653f274e', '2026-09-11 21:01:00.212341', 'Tablero de prueba del usuario administrador del espacio de trabajo', 'Segundo tablero', 'bf642609-8dbb-44ab-8e50-ccf99346c6ff');
insert into members (id, joined_at, role, user_id, board_id, active) values ('678b2f7e-d509-4c3a-944a-554e1f1b4de2', '2026-09-11 21:10:00.212341', 'ADMIN', '3e54e44f-8f87-445b-8817-8c13010f5da5', '6c4f289b-a915-49c6-b18a-9ba2d631834b', true);
insert into members (id, joined_at, role, user_id, board_id, active) values ('34cc9e8d-f3e5-4e86-879c-793a2e6f44cd', '2026-09-11 21:20:00.212341', 'MEMBER', '5d3498c0-43f1-4760-a4f0-dc0e97bcb641', '6c4f289b-a915-49c6-b18a-9ba2d631834b', true);
insert into members (id, joined_at, role, user_id, board_id, active) values ('fa32eed5-3bc8-4010-80d0-fa456d977423', '2026-09-11 21:30:00.212341', 'VIEWER', '858d7655-6178-458f-b880-3db6aa3beff3', '6c4f289b-a915-49c6-b18a-9ba2d631834b', true);

insert into members (id, joined_at, role, user_id, board_id, active) values ('15eef7fe-5e12-4224-8487-7454c1c1c3e8', '2026-09-11 22:10:00.212341', 'VIEWER', '3e54e44f-8f87-445b-8817-8c13010f5da5', 'ef777773-49ef-4282-a6f0-c233653f274e', true);
insert into members (id, joined_at, role, user_id, board_id, active) values ('c87a3e7c-e964-475f-b8fb-f94cab942828', '2026-09-11 22:20:00.212341', 'ADMIN', '5d3498c0-43f1-4760-a4f0-dc0e97bcb641', 'ef777773-49ef-4282-a6f0-c233653f274e', true);
insert into members (id, joined_at, role, user_id, board_id, active) values ('4c08b2a7-5124-4250-b74f-6c748a1e51d3', '2026-09-11 22:30:00.212341', 'MEMBER', '858d7655-6178-458f-b880-3db6aa3beff3', 'ef777773-49ef-4282-a6f0-c233653f274e', true);


# BASE DE DATOS: workflow-postgres-db
insert into board (id) values ('6c4f289b-a915-49c6-b18a-9ba2d631834b');
insert into board (id) values ('ef777773-49ef-4282-a6f0-c233653f274e');

insert into board_access (id, role, user_active, user_id, board_id) values ('a7dec456-b7b3-4066-a4aa-28fcf458adb7', 'OWNER', true, 'af6cf6ea-05c3-4233-a546-f942386f43ad', '6c4f289b-a915-49c6-b18a-9ba2d631834b');
insert into board_access (id, role, user_active, user_id, board_id) values ('ef3bcebd-9df5-4f9a-a533-06a1128c0667', 'ADMIN', true, '3e54e44f-8f87-445b-8817-8c13010f5da5', '6c4f289b-a915-49c6-b18a-9ba2d631834b');
insert into board_access (id, role, user_active, user_id, board_id) values ('c8afa273-ab71-4443-a400-4a1491cf0d04', 'MEMBER', true, '5d3498c0-43f1-4760-a4f0-dc0e97bcb641', '6c4f289b-a915-49c6-b18a-9ba2d631834b');
insert into board_access (id, role, user_active, user_id, board_id) values ('26bc127d-869a-4917-976f-eb982b8af257', 'VIEWER', true, '858d7655-6178-458f-b880-3db6aa3beff3', '6c4f289b-a915-49c6-b18a-9ba2d631834b');

insert into board_access (id, role, user_active, user_id, board_id) values ('e2da9c46-152e-4a84-89e3-23260109c112', 'OWNER', true, 'af6cf6ea-05c3-4233-a546-f942386f43ad', 'ef777773-49ef-4282-a6f0-c233653f274e');
insert into board_access (id, role, user_active, user_id, board_id) values ('e755f9c1-bcde-40d3-a445-d79258b759e1', 'VIEWER', true, '3e54e44f-8f87-445b-8817-8c13010f5da5', 'ef777773-49ef-4282-a6f0-c233653f274e');
insert into board_access (id, role, user_active, user_id, board_id) values ('50b0d625-0b6a-4c1b-a7d3-b48dc320dfa3', 'ADMIN', true, '5d3498c0-43f1-4760-a4f0-dc0e97bcb641', 'ef777773-49ef-4282-a6f0-c233653f274e');
insert into board_access (id, role, user_active, user_id, board_id) values ('78610410-f80f-499a-9dcb-a59d062d4d6d', 'MEMBER', true, '858d7655-6178-458f-b880-3db6aa3beff3', 'ef777773-49ef-4282-a6f0-c233653f274e');


# Herramienta de generación de datos: https://www.mockaroo.com/
# Generador de Bcrypt Hash: https://bcrypt-generator.com/