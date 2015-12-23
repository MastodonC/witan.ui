#!/bin/sh

SERVER_ADDR=${NGINX_SERVER_ADDR:?not set}
SERVER_PORT=${NGINX_SERVER_PORT:-3000}
DEPLOY_ADDR=${DEPLOY_ADDR:?not set}
DEPLOY_PORT=${DEPLOY_PORT:-9501}

PROXY_CONFIG_FILE=/etc/nginx/sites-available/witan-ui

echo "SERVER_ADDR is ${SERVER_ADDR}:${SERVER_PORT}"

cat > ${PROXY_CONFIG_FILE} <<EOF
server {

        listen 80 default_server;

        error_log /var/log/nginx/error.log;

        server_name witan-ui;

        location /api {
            access_log /var/log/nginx/access.log;

            # Assumes we are already behind a reverse proxy (e.g. ELB)
            real_ip_header X-Forwarded-For;
            set_real_ip_from 0.0.0.0/0;

            proxy_pass http://${SERVER_ADDR}:${SERVER_PORT};

        }

        location /monitoring/_elb_status {
            access_log /var/log/nginx/elb_status_access.log;
            proxy_pass http://${SERVER_ADDR}:${SERVER_PORT};
        }

        location ~* /api-docs/(.+) {
            real_ip_header X-Forwarded-For;
            set_real_ip_from 0.0.0.0/0;

            rewrite ^/api-docs/(.+)$ /\$1 break;
            proxy_pass http://${SERVER_ADDR}:${SERVER_PORT};
        }

        location ~* /deploy/(.+) {
            real_ip_header X-Forwarded-For;
            set_real_ip_from 0.0.0.0/0;

            rewrite ^/deploy/(.+)$ /\$1 break;
            proxy_pass http://${DEPLOY_ADDR}:${DEPLOY_PORT};
        }

        location /api-docs/ {
            real_ip_header X-Forwarded-For;
            set_real_ip_from 0.0.0.0/0;
            proxy_pass http://${SERVER_ADDR}:${SERVER_PORT}/index.html;
        }

        location /swagger.json {
            real_ip_header X-Forwarded-For;
            set_real_ip_from 0.0.0.0/0;
            proxy_pass http://${SERVER_ADDR}:${SERVER_PORT};
        }

        location / {
            root /var/www/witan-ui;
        }
}
EOF

rm /etc/nginx/sites-enabled/*

ln -sf ${PROXY_CONFIG_FILE} /etc/nginx/sites-enabled/default

nginx
