#!/bin/sh
set -e

sed \
  -e "s/\${FULFILLMENT_PRIVATE_IP}/${FULFILLMENT_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_1_PRIVATE_IP}/${KAFKA_BROKER_1_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_2_PRIVATE_IP}/${KAFKA_BROKER_2_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_3_PRIVATE_IP}/${KAFKA_BROKER_3_PRIVATE_IP}/g" \
  /etc/prometheus/prometheus.yml.tmpl > /tmp/prometheus.yml

exec /bin/prometheus "$@"
