ARG BASE_IMAGE=quay.io/prometheus/prometheus:v3.7.1
FROM ${BASE_IMAGE}
COPY prometheus.yml /etc/prometheus/prometheus.yml
COPY alert-rules.yml /etc/prometheus/alert-rules.yml
EXPOSE 9090
