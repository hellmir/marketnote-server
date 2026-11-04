#!/bin/sh
set -e

case "${ENVIRONMENT}" in
  qa|prod) ;;
  *)
    echo "ERROR: ENVIRONMENT must be 'qa' or 'prod' (got: '${ENVIRONMENT}')" >&2
    exit 1
    ;;
esac

TEMPLATE="/etc/prometheus/prometheus-${ENVIRONMENT}.yml.tmpl"

sed \
  -e "s/\${FULFILLMENT_PRIVATE_IP}/${FULFILLMENT_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_1_PRIVATE_IP}/${KAFKA_BROKER_1_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_2_PRIVATE_IP}/${KAFKA_BROKER_2_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_3_PRIVATE_IP}/${KAFKA_BROKER_3_PRIVATE_IP}/g" \
  "${TEMPLATE}" > /tmp/prometheus.yml

exec /bin/prometheus "$@"
