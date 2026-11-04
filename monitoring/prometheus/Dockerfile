ARG BASE_IMAGE=quay.io/prometheus/prometheus:v3.7.1
FROM ${BASE_IMAGE}
COPY prometheus-qa.yml.tmpl /etc/prometheus/prometheus-qa.yml.tmpl
COPY prometheus-prod.yml.tmpl /etc/prometheus/prometheus-prod.yml.tmpl
COPY alert-rules.yml /etc/prometheus/alert-rules.yml
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
EXPOSE 9090
