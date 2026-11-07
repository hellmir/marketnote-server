#!/bin/sh
set -e

case "${ENVIRONMENT}" in
  qa|prod) ;;
  *)
    echo "ERROR: ENVIRONMENT must be 'qa' or 'prod' (got: '${ENVIRONMENT}')" >&2
    exit 1
    ;;
esac

if [ -z "${MARKETNOTE_SERVER_DOMAIN}" ]; then
  echo "ERROR: MARKETNOTE_SERVER_DOMAIN must be set" >&2
  exit 1
fi

TEMPLATE="/etc/prometheus/prometheus-${ENVIRONMENT}.yml.tmpl"

sed \
  -e "s/\${MARKETNOTE_SERVER_DOMAIN}/${MARKETNOTE_SERVER_DOMAIN}/g" \
  -e "s/\${FULFILLMENT_PRIVATE_IP}/${FULFILLMENT_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_1_PRIVATE_IP}/${KAFKA_BROKER_1_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_2_PRIVATE_IP}/${KAFKA_BROKER_2_PRIVATE_IP}/g" \
  -e "s/\${KAFKA_BROKER_3_PRIVATE_IP}/${KAFKA_BROKER_3_PRIVATE_IP}/g" \
  "${TEMPLATE}" > /tmp/prometheus.yml

exec /bin/prometheus "$@"
