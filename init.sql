ALTER SYSTEM SET max_connections = 50;
ALTER SYSTEM RESET shared_buffers;
CREATE DATABASE workshop;
CREATE USER workshop WITH PASSWORD 'workshop';
GRANT ALL PRIVILEGES ON DATABASE "workshop" to workshop;