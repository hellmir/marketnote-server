FROM quay.io/prometheus/alertmanager:v0.28.1
RUN apk add --no-cache gettext
COPY alertmanager.yml /etc/alertmanager/alertmanager.template.yml
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
EXPOSE 9093
ENTRYPOINT ["/entrypoint.sh"]
