#!/bin/bash

# Preparación del entorno
echo "1. Preparando entorno..."
cd /tmp
rm -rf /tmp/iw3_2025
git clone -b main https://github.com/CEGB03/iw3_2025.git

# Construcción
echo "2. Construyendo aplicación con Maven..."
docker run -it --rm -v "$HOME/.m2:/root/.m2" -v /tmp/iw3_2025:/usr/src/mymaven -w /usr/src/mymaven maven:3.9.11-amazoncorretto-21-debian mvn clean package -Dmaven.test.skip=true -Dbuild=war -Dspring.profiles.active=mysqlprod

# Permisos
echo "3. Configurando permisos..."
sudo chmod 664 iw3_2025/target/ROOT.war
sudo chown $USER /tmp/iw3_2025/target/ROOT.war

# Deployment
echo "4. Iniciando deployment..."
docker compose -f /home/user/infra_iw32025/docker-compose.yml stop backend
rm -rf /home/user/infra_iw32025/tomcat/webapps/ROOT
mkdir /home/user/infra_iw32025/tomcat/webapps/ROOT
sudo mv /tmp/iw3_2025/target/ROOT.war /home/user/infra_iw32025/tomcat/webapps/ROOT/ROOT.zip

# Descompresión e inicio
echo "5. Descomprimiendo y reiniciando servicios..."
cd /home/user/infra_iw32025/tomcat/webapps/ROOT
unzip ROOT.zip
rm ROOT.zip
docker compose -f /home/user/infra_iw32025/docker-compose.yml start backend

# Espera y seed de base de datos
echo "6. Esperando a que el backend se levante y cree las tablas..."

# Esperar hasta que aparezca el log de perfil activo (indica que Spring Boot levantó correctamente)
TIMEOUT=120  # Timeout de 2 minutos
ELAPSED=0
until docker logs backend 2>&1 | grep -q "The following 1 profile is active\|profiles are active"; do
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "⚠️  Timeout: El backend no se levantó en $TIMEOUT segundos"
        echo "⚠️  Saltando seed de datos. Revisa los logs con: docker logs backend"
        exit 1
    fi
    echo "   Esperando backend... ($ELAPSED/$TIMEOUT segundos)"
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done

echo "✅ Backend levantado correctamente con perfil activo"
echo "⏳ Esperando 5 segundos adicionales para asegurar creación de tablas..."
sleep 5

# Ejecutar seed de datos base
echo "📊 Ejecutando seed de datos base..."
if [ -f /tmp/iw3_2025/docs/db/seed_base.sql ]; then
    docker exec -i mysql_iw3 mysql -uuser -p'1u4rootiw3' iw3_2025_prod < /tmp/iw3_2025/docs/db/seed_base.sql
    
    if [ $? -eq 0 ]; then
        echo "✅ Datos base insertados correctamente (Products, Customers, Drivers, Trucks)"
    else
        echo "⚠️  Error al ejecutar seed. Puede que ya existan datos o falte alguna tabla"
        echo "   Revisa manualmente con: docker exec -it mysql_iw3 mysql -uuser -p'1u4rootiw3' iw3_2025_prod"
    fi
else
    echo "⚠️  Archivo seed_base.sql no encontrado en /tmp/iw3_2025/docs/db/"
fi

echo ""
echo "=== Proceso completado exitosamente! ==="
echo "📌 Backend: https://iwiii25.dev"
echo "📌 Adminer: https://iwiii25.dev:8082"
echo "📌 MailHog: http://IP_VM:8025"
echo "📌 Logs backend: docker logs --tail 100 backend"
