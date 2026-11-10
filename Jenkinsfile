import com.cloudbees.groovy.cps.NonCPS

@NonCPS
def buildMarketnoteTaskDefinition(env, dbUrl, dbPassword) {
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
                [name: "SERVICE_NAME",                              value: env.ECS_SERVICE_NAME],
                [name: "JAVA_TOOL_OPTIONS",                         value: "-Duser.timezone=Asia/Seoul"],
                [name: "DB_URL",                                    value: dbUrl],
                [name: "DB_USERNAME",                               value: env.DB_USERNAME],
                [name: "DB_PASSWORD",                               value: dbPassword],
                [name: "JWT_SECRET_KEY",                            value: env.JWT_SECRET_KEY],
                [name: "ACCESS_TOKEN_EXPIRATION_TIME",              value: env.ACCESS_TOKEN_EXPIRATION_TIME],
                [name: "REFRESH_TOKEN_EXPIRATION_TIME",             value: env.REFRESH_TOKEN_EXPIRATION_TIME],
                [name: "CLIENT_ORIGIN",                             value: env.CLIENT_ORIGIN],
                [name: "COOKIE_DOMAIN",                             value: env.COOKIE_DOMAIN],
                [name: "ACCESS_CONTROL_ALLOWED_ORIGINS",            value: env.ACCESS_CONTROL_ALLOWED_ORIGINS],
                [name: "SERVER_ORIGIN",                             value: env.SERVER_ORIGIN],
                [name: "SPRING_PROFILES_ACTIVE",                    value: env.SPRING_PROFILE],
                [name: "GOOGLE_CLIENT_ID",                          value: env.GOOGLE_CLIENT_ID],
                [name: "GOOGLE_CLIENT_SECRET",                      value: env.GOOGLE_CLIENT_SECRET],
                [name: "KAKAO_CLIENT_ID",                           value: env.KAKAO_CLIENT_ID],
                [name: "KAKAO_CLIENT_SECRET",                       value: env.KAKAO_CLIENT_SECRET],
                [name: "KAKAO_ADMIN_KEY",                           value: env.KAKAO_ADMIN_KEY],
                [name: "KAKAO_ALLOWED_AUDIENCES",                   value: env.KAKAO_ALLOWED_AUDIENCES],
                [name: "GOOGLE_ALLOWED_AUDIENCES",                  value: env.GOOGLE_ALLOWED_AUDIENCES],
                [name: "APPLE_ALLOWED_AUDIENCES",                   value: env.APPLE_ALLOWED_AUDIENCES],
                [name: "S3_ACCESS_KEY",                             value: env.S3_ACCESS_KEY],
                [name: "S3_SECRET_KEY",                             value: env.S3_SECRET_KEY],
                [name: "S3_BUCKET_NAME",                            value: env.S3_BUCKET_NAME],
                [name: "SES_SMTP_USERNAME",                         value: env.SES_SMTP_USERNAME],
                [name: "SES_SMTP_PASSWORD",                         value: env.SES_SMTP_PASSWORD],
                [name: "MAIL_FROM",                                 value: env.MAIL_FROM],
                [name: "MAIL_SENDER_NAME",                          value: env.MAIL_SENDER_NAME],
                [name: "MAIL_VERIFICATION_TTL_MINUTES",             value: env.MAIL_VERIFICATION_TTL_MINUTES],
                [name: "REDIS_HOST_NAME",                           value: env.REDIS_HOST_NAME],
                [name: "REDIS_PASSWORD",                            value: env.REDIS_PASSWORD],
                [name: "REDIS_EMAIL_VERIFICATION_PREFIX",           value: env.REDIS_EMAIL_VERIFICATION_PREFIX],
                [name: "FILE_SERVICE_SERVER_ORIGIN",                value: env.FILE_SERVICE_SERVER_ORIGIN],
                [name: "PRODUCT_SERVICE_SERVER_ORIGIN",             value: env.PRODUCT_SERVICE_SERVER_ORIGIN],
                [name: "COMMERCE_SERVICE_SERVER_ORIGIN",            value: env.COMMERCE_SERVICE_SERVER_ORIGIN],
                [name: "COMMUNITY_SERVICE_SERVER_ORIGIN",           value: env.COMMUNITY_SERVICE_SERVER_ORIGIN],
                [name: "REWARD_SERVICE_SERVER_ORIGIN",              value: env.REWARD_SERVICE_SERVER_ORIGIN],
                [name: "FULFILLMENT_SERVICE_SERVER_ORIGIN",         value: env.FULFILLMENT_SERVICE_SERVER_ORIGIN],
                [name: "NOTIFICATION_SERVICE_SERVER_ORIGIN",        value: env.NOTIFICATION_SERVICE_SERVER_ORIGIN],
                [name: "JWT_ADMIN_ACCESS_TOKEN",                    value: env.JWT_ADMIN_ACCESS_TOKEN],
                [name: "ADPOPCORN_ANDROID_HASH_KEY",                value: env.ADPOPCORN_ANDROID_HASH_KEY],
                [name: "ADPOPCORN_IOS_HASH_KEY",                    value: env.ADPOPCORN_IOS_HASH_KEY],
                [name: "TNK_ANDROID_HASH_KEY",                      value: env.TNK_ANDROID_HASH_KEY],
                [name: "TNK_IOS_HASH_KEY",                          value: env.TNK_IOS_HASH_KEY],
                [name: "ADISCOPE_ANDROID_HASH_KEY",                 value: env.ADISCOPE_ANDROID_HASH_KEY],
                [name: "ADISCOPE_IOS_HASH_KEY",                     value: env.ADISCOPE_IOS_HASH_KEY],
                [name: "FASSTO_BASE_URL",                           value: env.FASSTO_BASE_URL],
                [name: "FASSTO_API_CD",                             value: env.FASSTO_API_CD],
                [name: "FASSTO_API_KEY",                            value: env.FASSTO_API_KEY],
                [name: "FASSTO_CUSTOMER_CODE",                      value: env.FASSTO_CUSTOMER_CODE],
                [name: "KAFKA_BOOTSTRAP_SERVERS",                   value: env.KAFKA_BOOTSTRAP_SERVERS],
                [name: "KAFKA_SLACK_WEBHOOK_URL",                   value: env.KAFKA_SLACK_WEBHOOK_URL],
                [name: "KAFKA_SASL_ENABLED",                        value: env.KAFKA_SASL_ENABLED],
                [name: "KAFKA_SASL_MECHANISM",                      value: env.KAFKA_SASL_MECHANISM],
                [name: "KAFKA_SASL_PROTOCOL",                       value: env.KAFKA_SASL_PROTOCOL],
                [name: "KAFKA_SASL_USERNAME",                       value: env.KAFKA_SASL_USERNAME],
                [name: "KAFKA_SASL_PASSWORD",                       value: env.KAFKA_SASL_PASSWORD],
                [name: "KAFKA_SSL_TRUSTSTORE_PASSWORD",             value: env.KAFKA_SSL_TRUSTSTORE_PASSWORD],
                [name: "HMAC_SECRET_KEY",                           value: env.HMAC_SECRET_KEY],
                [name: "GIFTICON_PIN_ENCRYPT_KEY",                  value: env.GIFTICON_PIN_ENCRYPT_KEY],
                [name: "GIFTICON_SYNC_SCHEDULER_ENABLED",           value: env.GIFTICON_SYNC_SCHEDULER_ENABLED],
                [name: "GIFTICON_SYNC_SCHEDULER_CRON",              value: env.GIFTICON_SYNC_SCHEDULER_CRON],
                [name: "GIFTICON_COUPON_SYNC_SCHEDULER_ENABLED",    value: env.GIFTICON_COUPON_SYNC_SCHEDULER_ENABLED],
                [name: "GIFTICON_COUPON_SYNC_SCHEDULER_CRON",       value: env.GIFTICON_COUPON_SYNC_SCHEDULER_CRON],
                [name: "FIREBASE_SERVICE_ACCOUNT_JSON",             value: env.FIREBASE_SERVICE_ACCOUNT_JSON],
                [name: "SPRING_FLYWAY_ENABLED",                     value: env.SPRING_FLYWAY_ENABLED ?: "true"],
            ],
            logConfiguration: [
                logDriver: "awslogs",
                options: [
                    "awslogs-group":  env.CLOUDWATCH_LOG_GROUP,
                    "awslogs-region": env.AWS_DEFAULT_REGION,
                    "awslogs-stream-prefix": env.PROJECT_NAME
                ]
            ],
            healthCheck: [
                command: ["CMD-SHELL",
                    "(curl -fsS http://127.0.0.1:8080/actuator/health/liveness || wget -qO- http://127.0.0.1:8080/actuator/health/liveness) | grep '\"status\":\"UP\"'"],
                interval: 15,
                timeout: 5,
                retries: 3,
                startPeriod: 210
            ]
        ]]
    ]
}

@NonCPS
def buildRedisTaskDefinition(env) {
    [
        family: "redis",
        networkMode: "awsvpc",
        requiresCompatibilities: ["FARGATE"],
        cpu: "256",
        memory: "512",
        executionRoleArn: "${env.ECS_TASK_EXECUTION_ROLE_ARN}",
        taskRoleArn: "${env.ECS_TASK_ROLE_ARN}",
        containerDefinitions: [[
            name: "redis",
            image: "redis:7.2",
            portMappings: [[containerPort: 6379, protocol: "tcp"]],
            essential: true,
            command: [
                "sh",
                "-c",
                "exec redis-server --appendonly yes --requirepass \"\$REDIS_PASSWORD\""
            ],
            environment: [
                [name: "REDIS_SERVICE_NAME", value: "${env.REDIS_SERVICE_NAME}"],
                [name: "REDIS_PASSWORD",     value: "${env.REDIS_PASSWORD}"]
            ],
            logConfiguration: [
                logDriver: "awslogs",
                options: [
                    "awslogs-group": "${env.CLOUDWATCH_LOG_GROUP_REDIS}",
                    "awslogs-region": "${env.AWS_DEFAULT_REGION}",
                    "awslogs-stream-prefix": "redis"
                ]
            ],
            healthCheck: [
                command: ["CMD-SHELL",
                    "redis-cli -a \"\$REDIS_PASSWORD\" --no-auth-warning ping | grep -q PONG"],
                interval: 15,
                timeout: 5,
                retries: 3,
                startPeriod: 30
            ]
        ]]
    ]
}

@NonCPS
def buildPrometheusTaskDefinition(env) {
    [
        family: "prometheus",
        networkMode: "awsvpc",
        requiresCompatibilities: ["FARGATE"],
        cpu: "256",
        memory: "512",
        executionRoleArn: "${env.ECS_TASK_EXECUTION_ROLE_ARN}",
        taskRoleArn: "${env.ECS_TASK_ROLE_ARN}",
        containerDefinitions: [[
            name: "prometheus",
            image: "${env.PROMETHEUS_IMAGE_URI}",
            portMappings: [[containerPort: 9090, protocol: "tcp"]],
            essential: true,
            command: [
                "--config.file=/tmp/prometheus.yml",
                "--storage.tsdb.path=/prometheus",
                "--storage.tsdb.retention.time=1d",
                "--storage.tsdb.retention.size=512MB",
                "--web.console.libraries=/usr/share/prometheus/console_libraries",
                "--web.console.templates=/usr/share/prometheus/consoles"
            ],
            environment: [
                [name: "ENVIRONMENT",               value: "${env.ENVIRONMENT_UPPER.toLowerCase()}"],
                [name: "MARKETNOTE_SERVER_DOMAIN",        value: "${env.MARKETNOTE_SERVER_DOMAIN}"],
                [name: "FULFILLMENT_PRIVATE_IP",    value: "${env.FULFILLMENT_PRIVATE_IP}"],
                [name: "KAFKA_BROKER_1_PRIVATE_IP", value: "${env.KAFKA_BROKER_1_PRIVATE_IP}"],
                [name: "KAFKA_BROKER_2_PRIVATE_IP", value: "${env.KAFKA_BROKER_2_PRIVATE_IP}"],
                [name: "KAFKA_BROKER_3_PRIVATE_IP", value: "${env.KAFKA_BROKER_3_PRIVATE_IP}"]
            ],
            logConfiguration: [
                logDriver: "awslogs",
                options: [
                    "awslogs-group": "${env.CLOUDWATCH_LOG_GROUP_PROMETHEUS}",
                    "awslogs-region": "${env.AWS_DEFAULT_REGION}",
                    "awslogs-stream-prefix": "prometheus"
                ]
            ],
            healthCheck: [
                command: ["CMD-SHELL",
                    "wget -qO- http://localhost:9090/-/healthy || exit 1"],
                interval: 15,
                timeout: 5,
                retries: 3,
                startPeriod: 60
            ]
        ]]
    ]
}

@NonCPS
def buildGrafanaTaskDefinition(env) {
    [
        family: "grafana",
        networkMode: "awsvpc",
        requiresCompatibilities: ["FARGATE"],
        cpu: "256",
        memory: "512",
        executionRoleArn: "${env.ECS_TASK_EXECUTION_ROLE_ARN}",
        taskRoleArn: "${env.ECS_TASK_ROLE_ARN}",
        containerDefinitions: [[
            name: "grafana",
            image: "${env.GRAFANA_IMAGE_URI}",
            portMappings: [[containerPort: 3000, protocol: "tcp"]],
            essential: true,
            environment: [
                [name: "GF_SECURITY_ADMIN_PASSWORD",      value: "${env.GRAFANA_ADMIN_PASSWORD}"],
                [name: "GF_SERVER_DOMAIN",                value: "${env.GRAFANA_DOMAIN}"],
                [name: "GF_SERVER_ROOT_URL",              value: "${env.GRAFANA_ROOT_URL}"],
                [name: "GF_SERVER_SERVE_FROM_SUB_PATH",   value: "${env.GRAFANA_SERVE_FROM_SUB_PATH?:'false'}"],
                [name: "PROMETHEUS_URL",                  value: "${env.PROMETHEUS_URL}"]
            ],
            logConfiguration: [
                logDriver: "awslogs",
                options: [
                    "awslogs-group": "${env.CLOUDWATCH_LOG_GROUP_GRAFANA}",
                    "awslogs-region": "${env.AWS_DEFAULT_REGION}",
                    "awslogs-stream-prefix": "grafana"
                ]
            ],
            healthCheck: [
                command: ["CMD-SHELL",
                    "wget -qO- http://localhost:3000/api/health || exit 1"],
                interval: 15,
                timeout: 5,
                retries: 3,
                startPeriod: 90
            ]
        ]]
    ]
}

// Flyway 마이그레이션이 분리된 서비스 (Jenkins stage가 마이그레이션 담당, ECS는 SPRING_FLYWAY_ENABLED=false)
def FLYWAY_MIGRATED_SERVICES = [
    'notification-service', 'commerce-service', 'community-service',
    'user-service', 'fulfillment-service', 'reward-service',
    'product-service', 'file-service'
]

// QA 잡 한정 매일 새벽 자동 배포 cron spec.
// 공용 Jenkinsfile이 qa/stage/prod 잡에 모두 사용되므로 JOB_NAME 프로젝트 세그먼트 접미사로 QA만 분기한다.
// 판정 로직은 Environment Setup stage의 기존 선례와 동일(jobName.split('/')[0] 매칭)하여 이중 방어를 확보한다.
// 비-QA 환경에서는 '#'로 시작하는 주석 라인만 반환하여 parameterizedCron parser가 무시하게 한다.
// 스케줄: 02:00~05:30 KST, 30분 간격 8개 서비스 (기반→상위→cross-cutting 의존성 순서)
def buildQaCronSpec() {
    def jobName = env.JOB_NAME ?: ''
    def projectName = jobName.split('/')[0]
    def matcher = (projectName =~ /-(qa|stage|prod)$/)
    if (!matcher.find() || matcher.group(1) != 'qa') {
        return '# marketnote-qa 한정. 현재 JOB_NAME은 QA 잡이 아니므로 cron 미발화'
    }
    // TZ=Asia/Seoul 미지정 시 Jenkins controller JVM 기본 TZ(UTC) 로 cron 이 해석됨.
    // 첫 줄 TZ 선언으로 KST 의도(02:00~05:30)를 spec 자체에 표현 → 주석/동작 정합 + 실수 회귀 차단.
    return '''\
TZ=Asia/Seoul
0 2 * * * %SERVICE=user-service
30 2 * * * %SERVICE=file-service
0 3 * * * %SERVICE=product-service
30 3 * * * %SERVICE=reward-service
0 4 * * * %SERVICE=commerce-service
30 4 * * * %SERVICE=community-service
0 5 * * * %SERVICE=fulfillment-service
30 5 * * * %SERVICE=notification-service
'''
}

pipeline {
	agent any

	options {
		disableConcurrentBuilds()
	}

	triggers {
		parameterizedCron(buildQaCronSpec())
	}

	parameters {
		choice(name: 'SERVICE', choices: ['auto','user-service','product-service','commerce-service','community-service','reward-service','fulfillment-service','file-service','notification-service'], description: '배포 대상 서비스')
		booleanParam(name: 'SKIP_FLYWAY_IMMUTABILITY_CHECK', defaultValue: false, description: '긴급 revert PR 한정 — 적용 완료 V*.sql 변경 차단 가드 우회. 기본값 false 유지 필수')
		string(name: 'SKIP_REASON', defaultValue: '', description: 'SKIP_FLYWAY_IMMUTABILITY_CHECK=true 시 우회 사유. 형식: "#<PR번호> <설명>" (예: "#4806 마이그레이션 V102 revert"). 설명 1~200자, 줄바꿈/탭 금지. SKIP=true 시 필수 — 형식 위반 시 빌드 차단. SKIP=false 시 무시됨')
		string(name: 'DISPATCHER_BUILD_NUMBER', defaultValue: '', description: 'dispatcher (SERVICE=auto) 가 자식 빌드 트리거 시 자동 전달하는 부모 BUILD_NUMBER. 수동/cron 트리거 시 빈값 유지')
	}

	environment {
		SLACK_SUCCESS_COLOR = "#2C953C"
		SLACK_FAIL_COLOR = "#E03131"
		SLACK_GENERAL_COLOR = "#5C7CFA"
	}

	stages {
		stage('Select Target Service') {
			steps {
				script {
					def helper = load 'jenkins/resolve-and-register.groovy'
					helper.selectTargetService(this)
				}
			}
		}

		stage('Skip If Same Commit (Cron Only)') {
			when { not { environment name: 'DISPATCHER_MODE', value: 'true' } }
			steps {
				script {
					def helper = load 'jenkins/resolve-and-register.groovy'
					helper.performCronSkipGuard(this)
				}
			}
		}

		stage('Deploy Pipeline') {
			when {
				allOf {
					not { environment name: 'CRON_SKIP', value: 'true' }
					not { environment name: 'DISPATCHER_MODE', value: 'true' }
				}
			}
			stages {
			stage('Environment Setup') {
				steps {
					script {
						sh 'chmod +x ./gradlew'
						env.PROJECT_NAME = sh(script: "./gradlew -p ${env.SERVICE_DIRECTORY} -q printProjectName", returnStdout: true).trim()
						env.PROJECT_VERSION = sh(script: "./gradlew -p ${env.SERVICE_DIRECTORY} -q printProjectVersion", returnStdout: true).trim()
						env.DOCKERFILE_PATH = "${WORKSPACE}/${env.SERVICE_DIRECTORY}/Dockerfile"
						env.BUILD_CONTEXT = "${WORKSPACE}/${env.SERVICE_DIRECTORY}"
						def jobName = env.JOB_NAME ?: ''
						def projectName = jobName.split('/')[0]
						def matcher = (projectName =~ /-(qa|stage|prod)$/)
						if (!matcher.find()) {
							error "JOB_NAME '${jobName}' (project='${projectName}') must end with one of: -qa, -stage, -prod"
						}
						def environment = matcher.group(1).toUpperCase()
						matcher = null
						env.ENVIRONMENT_UPPER = environment
						env.ENV_PREFIX = "MARKETNOTE_${environment}"

					}

					echo 'Environment variables set'

					echo "CANDIDATE_SERVICES        = ${env.CANDIDATE_SERVICES}"
					echo "SERVICE_NAME              = ${env.SERVICE_NAME}"
					echo "SERVICE_DIRECTORY         = ${env.SERVICE_DIRECTORY}"
					echo "PROJECT_NAME              = ${env.PROJECT_NAME}"
					echo "PROJECT_VERSION           = ${env.PROJECT_VERSION}"
					echo "ENVIRONMENT               = ${env.ENVIRONMENT_UPPER}"
					echo "ENV_PREFIX                = ${env.ENV_PREFIX}"

					script {
						def helper = load 'jenkins/resolve-and-register.groovy'
						helper.logCronInfo(this)
					}
				}
			}

	        stage('Notify Start') {
	            steps {
	                script {
	                    def helper = load 'jenkins/resolve-and-register.groovy'
	                    helper.sendStartNotification(this)
	                }
	            }
	        }

			stage('Check Flyway Immutability') {
				steps {
					script {
						if (params.SKIP_FLYWAY_IMMUTABILITY_CHECK) {
							echo "[WARN] Flyway immutability check SKIPPED by parameter. 긴급 revert PR 한정 용도. SERVICE=${params.SERVICE}, BUILD_URL=${env.BUILD_URL}"
							def helper = load 'jenkins/resolve-and-register.groovy'
							helper.notifyFlywayImmutabilitySkip(this)
							return
						}
						withCredentials([gitUsernamePassword(credentialsId: 'MARKETNOTE_GITHUB_WEBHOOK_ACCESS_TOKEN', gitToolName: 'Default')]) {
							sh 'bash scripts/check-flyway-immutability.sh'
						}
					}
				}
			}

			stage('Test') {
	            steps {
	                sh "chmod u+x ${WORKSPACE}/gradlew"
	                sh "./gradlew -p ${env.SERVICE_NAME} clean test"
	                echo "Tests complete"
	            }
	        }

	        stage('Build') {
	            steps {
	                sh "./gradlew -p ${env.SERVICE_DIRECTORY} build -x test"
	                echo "Build complete"
	            }
	        }

			stage('Resolve Service Mappings') {
				steps {
					script {
						def helper = load 'jenkins/resolve-and-register.groovy'
						helper.resolveServiceMappings(this, env.ENV_PREFIX)
					}
				}
			}

			stage('Detect Redis Changes') {
				steps {
					script {
						def base = sh(script: 'git rev-parse HEAD~1 || git rev-parse HEAD', returnStdout: true).trim()
						def changed = sh(script: "git diff --name-only ${base} HEAD || true", returnStdout: true)
						.trim()
						.split('\n')
						.findAll {
							it?.trim()
						} as List
						def redisChanged = changed.any {
							p ->
							p ==~ /.*user-service\/.*\/src\/.*\/com\/personal\/marketnote\/user\/configuration\/CacheConfig\.java/ ||
							p ==~ /.*user-service\/.*\/src\/.*\/com\/personal\/marketnote\/user\/configuration\/SessionConfig\.java/
						}
						if (!redisChanged && changed.contains('Jenkinsfile')) {
							redisChanged = sh(
								script: "git diff --unified=0 ${base} HEAD -- Jenkinsfile | grep -E '^[+-]' | grep -iq 'redis' && echo match || true",
								returnStdout: true
							).trim() == 'match'
						}
						env.REDIS_CHANGED = redisChanged ? "true" : "false"
						echo "REDIS_CHANGED              = ${env.REDIS_CHANGED}"
					}
				}
			}

			stage('Register Redis Task Definition') {
				steps {
					script {
						if (env.REDIS_CHANGED != "true") {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",           variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY",       variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",          variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_TASK_EXECUTION_ROLE_ARN", variable: 'ECS_TASK_EXECUTION_ROLE_ARN'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_TASK_ROLE_ARN",           variable: 'ECS_TASK_ROLE_ARN'),
							string(credentialsId: "${env.ENV_PREFIX}_CLOUDWATCH_LOG_GROUP_REDIS",  variable: 'CLOUDWATCH_LOG_GROUP_REDIS'),
							string(credentialsId: "${env.ENV_PREFIX}_REDIS_SERVICE_NAME",          variable: 'REDIS_SERVICE_NAME'),
							string(credentialsId: "${env.ENV_PREFIX}_REDIS_PASSWORD",              variable: 'REDIS_PASSWORD')
						]) {
							sh '''
					          LG="$CLOUDWATCH_LOG_GROUP_REDIS"
					          EXISTS=$(aws logs describe-log-groups --log-group-name-prefix "$LG" --region "$AWS_DEFAULT_REGION" --query "length(logGroups[?logGroupName=='$LG'])" --output text || echo 0)
					          if [ "$EXISTS" = "0" ]; then
					            aws logs create-log-group --log-group-name "$LG" --region "$AWS_DEFAULT_REGION"
					          fi
					        '''

							def redisTask = buildRedisTaskDefinition(env)

							def json = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(redisTask))
							writeFile file: 'redis-taskdef.json', text: json

							def out = sh(
								script: 'aws ecs register-task-definition --cli-input-json file://redis-taskdef.json --region $AWS_DEFAULT_REGION --query \'taskDefinition.taskDefinitionArn\' --output text',
								returnStdout: true
							).trim()

							if (!out || out == 'None') {
								error 'Failed to register Redis task definition'
							}
							env.REDIS_TASK_DEF_ARN = out
							echo "Registered Redis TaskDef ARN = ${env.REDIS_TASK_DEF_ARN}"
						}
					}
				}
			}

			stage('Deploy Redis Service') {
				steps {
					script {
						if (env.REDIS_CHANGED != "true") {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",            variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY",        variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",           variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_CLUSTER_NAME",             variable: 'ECS_CLUSTER_NAME'),
							string(credentialsId: "${env.ENV_PREFIX}_REDIS_SERVICE_NAME",           variable: 'REDIS_SERVICE_NAME'),
							string(credentialsId: "${env.ENV_PREFIX}_SUBNET_IDS",                   variable: 'SUBNET_IDS'),
							string(credentialsId: "${env.ENV_PREFIX}_SECURITY_GROUP_IDS",           variable: 'SECURITY_GROUP_IDS'),
							string(credentialsId: "${env.ENV_PREFIX}_REDIS_SERVICE_DISCOVERY_ARN",  variable: 'REDIS_SERVICE_DISCOVERY_ARN')
						]) {
							def latestTask = env.REDIS_TASK_DEF_ARN?.trim()
							if (!latestTask) {
								latestTask = sh(script: 'aws ecs list-task-definitions --family-prefix redis --sort DESC --region $AWS_DEFAULT_REGION --query \'taskDefinitionArns[0]\' --output text', returnStdout: true).trim()
							}
							if (!latestTask || latestTask == 'None') {
								error 'No Redis TaskDefinition found to deploy'
							}

							def exists = sh(script: 'aws ecs describe-services --cluster $ECS_CLUSTER_NAME --services $REDIS_SERVICE_NAME --region $AWS_DEFAULT_REGION --query \'services[0].status\' --output text || true', returnStdout: true).trim()
							def subnets = sh(script: 'printf "%s" "$SUBNET_IDS" | awk -F, \'{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}\'', returnStdout: true).trim()
							def sgs = sh(script: 'printf "%s" "$SECURITY_GROUP_IDS" | awk -F, \'{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}\'', returnStdout: true).trim()

							if (exists == "ACTIVE" || exists == "DRAINING") {
								sh """
	          					  aws ecs update-service \
	          					    --cluster \$ECS_CLUSTER_NAME \
	          					    --service \$REDIS_SERVICE_NAME \
	          					    --task-definition ${latestTask} \
	          					    --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	          					    --desired-count 1 \
	          					    --region \$AWS_DEFAULT_REGION \
	          					    --force-new-deployment
	          					"""
							} else {
								sh """
	          					  aws ecs create-service \
	          					    --cluster \$ECS_CLUSTER_NAME \
	          					    --service-name \$REDIS_SERVICE_NAME \
	          					    --task-definition ${latestTask} \
	          					    --desired-count 1 \
	          					    --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	          					    --network-configuration "awsvpcConfiguration={subnets=[${subnets}],securityGroups=[${sgs}],assignPublicIp=ENABLED}" \
	          					    --service-registries registryArn=\$REDIS_SERVICE_DISCOVERY_ARN \
	          					    --region \$AWS_DEFAULT_REGION
	          					"""
							}
							sh 'aws ecs wait services-stable --cluster $ECS_CLUSTER_NAME --services $REDIS_SERVICE_NAME --region $AWS_DEFAULT_REGION'
						}
					}
				}
			}

			stage('Build Marketnote Service Image') {
				steps {
					script {
						def dockerImageTag = "${env.PROJECT_NAME}:${env.PROJECT_VERSION}"
						env.DOCKER_IMAGE_TAG = dockerImageTag
						sh '''
				          docker build \
				            --build-arg JAR_FILE=build/libs/$PROJECT_NAME-$PROJECT_VERSION.jar \
				            -t $DOCKER_IMAGE_TAG \
				            -f "$DOCKERFILE_PATH" \
				            "$BUILD_CONTEXT"
				        '''
					}
				}
			}

			stage('Push Marketnote Service Image') {
				steps {
					script {
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCOUNT_ID",        variable: 'AWS_ACCOUNT_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",     variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY", variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",    variable: 'AWS_DEFAULT_REGION'),
						]) {
							sh '''
	                        aws --version
	                        aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
	                        aws ecr describe-repositories --repository-names $ECR_REPOSITORY --region $AWS_DEFAULT_REGION || aws ecr create-repository --repository-name $ECR_REPOSITORY --region $AWS_DEFAULT_REGION
	                        docker tag $DOCKER_IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$ECR_REPOSITORY:$PROJECT_VERSION
	                        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$ECR_REPOSITORY:$PROJECT_VERSION
	                        '''
							env.IMAGE_URI = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_DEFAULT_REGION}.amazonaws.com/${env.ECR_REPOSITORY}:${env.PROJECT_VERSION}"
						}
					}
				}
			}

			stage('Run Flyway Migration') {
				when {
					expression { return env.SERVICE_NAME in FLYWAY_MIGRATED_SERVICES }
				}
				steps {
					script {
						def helper = load 'jenkins/resolve-and-register.groovy'
						helper.runFlywayMigrationStage(this, env.ENV_PREFIX)
					}
				}
			}

			stage('Run Concurrent Indexes') {
				steps {
					timeout(time: 30, unit: 'MINUTES') {
						script {
							if (!fileExists("${env.SERVICE_DIRECTORY}/src/main/resources/db/concurrent-indexes")) {
								sleep time: 1, unit: 'SECONDS'; return
							}
							def helper = load 'jenkins/resolve-and-register.groovy'
							helper.runConcurrentIndexes(this, env.ENV_PREFIX)
							echo 'Concurrent indexes applied'
						}
					}
				}
			}

			stage('Override Spring Flyway Enabled=false') {
				when {
					expression { return env.SERVICE_NAME in FLYWAY_MIGRATED_SERVICES }
				}
				steps {
					script {
						env.SPRING_FLYWAY_ENABLED = "false"
						echo "Set SPRING_FLYWAY_ENABLED=false for ${env.SERVICE_NAME}"
					}
				}
			}

			stage('Register Marketnote Service Task Definition') {
	            steps {
	                script {
	                    def helper = load 'jenkins/resolve-and-register.groovy'
	                    helper.registerTaskDefinition(this, env.ENV_PREFIX)
	                }
	            }
	        }

			stage('Deploy Marketnote Service') {
	            steps {
	                script {
	                    withCredentials([
	                        string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCOUNT_ID",        variable: 'AWS_ACCOUNT_ID'),
	                        string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",     variable: 'AWS_ACCESS_KEY_ID'),
	                        string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY", variable: 'AWS_SECRET_ACCESS_KEY'),
	                        string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",    variable: 'AWS_DEFAULT_REGION'),
	                        string(credentialsId: "${env.ENV_PREFIX}_ECS_CLUSTER_NAME",      variable: 'ECS_CLUSTER_NAME'),
	                        string(credentialsId: "${env.ENV_PREFIX}_SUBNET_IDS",            variable: 'SUBNET_IDS'),
	                        string(credentialsId: "${env.ENV_PREFIX}_SECURITY_GROUP_IDS",    variable: 'SECURITY_GROUP_IDS'),
	                    ]) {
	                        if (!env.APP_TASK_DEF_ARN?.trim()) {
	                            error 'No application TaskDefinition ARN (APP_TASK_DEF_ARN) found to deploy'
	                        }

	                        sh '''
	                          EXISTS=$(aws ecs describe-services --cluster "$ECS_CLUSTER_NAME" --services "$ECS_SERVICE_NAME" --region "$AWS_DEFAULT_REGION" --query 'services[0].status' --output text 2>/dev/null || true)
	                          SUBNETS=$(printf "%s" "$SUBNET_IDS" | awk -F, '{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}')
	                          SGS=$(printf "%s" "$SECURITY_GROUP_IDS" | awk -F, '{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}')

	                          if [ "$EXISTS" = "ACTIVE" ] || [ "$EXISTS" = "DRAINING" ]; then
	                            aws ecs update-service \
	                              --cluster "$ECS_CLUSTER_NAME" \
	                              --service "$ECS_SERVICE_NAME" \
	                              --task-definition "$APP_TASK_DEF_ARN" \
	                              --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	                              --desired-count 1 \
	                              --health-check-grace-period-seconds 240 \
	                              --region "$AWS_DEFAULT_REGION" \
	                              --force-new-deployment
	                          else
	                            aws ecs create-service \
	                              --cluster "$ECS_CLUSTER_NAME" \
	                              --service-name "$ECS_SERVICE_NAME" \
	                              --task-definition "$APP_TASK_DEF_ARN" \
	                              --desired-count 1 \
	                              --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	                              --load-balancers targetGroupArn="$TARGET_GROUP_ARN",containerName="$PROJECT_NAME",containerPort=8080 \
	                              --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SGS],assignPublicIp=ENABLED}" \
	                              --region "$AWS_DEFAULT_REGION"
	                          fi

	                          aws ecs update-service --cluster "$ECS_CLUSTER_NAME" --service "$ECS_SERVICE_NAME" --region "$AWS_DEFAULT_REGION" --health-check-grace-period-seconds 240 || true

	                          MAX_WAIT_RETRIES=20
	                          i=1
	                          while [ $i -le $MAX_WAIT_RETRIES ]; do
	                            if aws ecs wait services-stable --cluster "$ECS_CLUSTER_NAME" --services "$ECS_SERVICE_NAME" --region "$AWS_DEFAULT_REGION"; then
	                              echo "ECS services-stable 성공 (attempt $i)"
	                              break
	                            fi
	                            echo "ECS services-stable 타임아웃 (attempt $i) → 재시도"
	                            sleep 20
	                            if [ $i -eq $MAX_WAIT_RETRIES ]; then
	                              exit 1
	                            fi
	                            i=$((i+1))
	                          done
	                        '''
	                    }
	                }
	            }
	        }

			stage('Build Prometheus Image') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/prometheus || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						sh 'test -f monitoring/prometheus/Dockerfile'
						sh 'test -f monitoring/prometheus/prometheus-qa.yml.tmpl'
						sh 'test -f monitoring/prometheus/prometheus-prod.yml.tmpl'
						def prometheusTag = "prometheus:${env.PROJECT_VERSION}"
						env.PROMETHEUS_LOCAL_TAG = prometheusTag
						sh """
	                    docker build \
	                     -t ${prometheusTag} \
	                     -f ${env.WORKSPACE}/monitoring/prometheus/Dockerfile \
	                     ${env.WORKSPACE}/monitoring/prometheus
	                    """
					}
				}
			}

			stage('Push Prometheus Image') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/prometheus || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCOUNT_ID",              variable: 'AWS_ACCOUNT_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",           variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY",       variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",          variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_PROMETHEUS_ECR_REPOSITORY",   variable: 'PROMETHEUS_ECR_REPOSITORY')
						]) {
							sh '''
	                        aws ecr describe-repositories --repository-names $PROMETHEUS_ECR_REPOSITORY --region $AWS_DEFAULT_REGION || aws ecr create-repository --repository-name $PROMETHEUS_ECR_REPOSITORY --region $AWS_DEFAULT_REGION
	                        aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
	                        docker tag $PROMETHEUS_LOCAL_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$PROMETHEUS_ECR_REPOSITORY:$PROJECT_VERSION
	                        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$PROMETHEUS_ECR_REPOSITORY:$PROJECT_VERSION
	                        '''
							env.PROMETHEUS_IMAGE_URI = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_DEFAULT_REGION}.amazonaws.com/${env.PROMETHEUS_ECR_REPOSITORY}:${env.PROJECT_VERSION}"
						}
					}
				}
			}

			stage('Build Grafana Image') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/grafana || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						sh 'test -f monitoring/grafana/Dockerfile'
						def grafanaTag = "grafana:${env.PROJECT_VERSION}"
						env.GRAFANA_LOCAL_TAG = grafanaTag
						sh """
	                    docker build \
	                     -t ${grafanaTag} \
	                     -f ${env.WORKSPACE}/monitoring/grafana/Dockerfile \
	                     ${env.WORKSPACE}/monitoring/grafana
	                    """
					}
				}
			}

			stage('Push Grafana Image') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/grafana || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCOUNT_ID",             variable: 'AWS_ACCOUNT_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",          variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY",      variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",         variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_GRAFANA_ECR_REPOSITORY",     variable: 'GRAFANA_ECR_REPOSITORY')
						]) {
							sh '''
	                        aws ecr describe-repositories --repository-names $GRAFANA_ECR_REPOSITORY --region $AWS_DEFAULT_REGION || aws ecr create-repository --repository-name $GRAFANA_ECR_REPOSITORY --region $AWS_DEFAULT_REGION
	                        aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
	                        docker tag $GRAFANA_LOCAL_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$GRAFANA_ECR_REPOSITORY:$PROJECT_VERSION
	                        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$GRAFANA_ECR_REPOSITORY:$PROJECT_VERSION
	                        '''
							env.GRAFANA_IMAGE_URI = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_DEFAULT_REGION}.amazonaws.com/${env.GRAFANA_ECR_REPOSITORY}:${env.PROJECT_VERSION}"
						}
					}
				}
			}

			stage('Register Prometheus Task Definition') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/prometheus || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",               variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY",           variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",              variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_TASK_EXECUTION_ROLE_ARN",     variable: 'ECS_TASK_EXECUTION_ROLE_ARN'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_TASK_ROLE_ARN",               variable: 'ECS_TASK_ROLE_ARN'),
							string(credentialsId: "${env.ENV_PREFIX}_CLOUDWATCH_LOG_GROUP_PROMETHEUS", variable: 'CLOUDWATCH_LOG_GROUP_PROMETHEUS'),
							string(credentialsId: "${env.ENV_PREFIX}_SERVER_DOMAIN",                   variable: 'MARKETNOTE_SERVER_DOMAIN'),
							string(credentialsId: "${env.ENV_PREFIX}_FULFILLMENT_PRIVATE_IP",          variable: 'FULFILLMENT_PRIVATE_IP'),
							string(credentialsId: "${env.ENV_PREFIX}_KAFKA_BROKER_1_PRIVATE_IP",       variable: 'KAFKA_BROKER_1_PRIVATE_IP'),
							string(credentialsId: "${env.ENV_PREFIX}_KAFKA_BROKER_2_PRIVATE_IP",       variable: 'KAFKA_BROKER_2_PRIVATE_IP'),
							string(credentialsId: "${env.ENV_PREFIX}_KAFKA_BROKER_3_PRIVATE_IP",       variable: 'KAFKA_BROKER_3_PRIVATE_IP')
						]) {
							sh '''
	                        aws logs create-log-group --log-group-name "$CLOUDWATCH_LOG_GROUP_PROMETHEUS" --region "$AWS_DEFAULT_REGION" || true
	                        '''
							def promTask = buildPrometheusTaskDefinition(env)
							def json = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(promTask))
							writeFile file: 'prom-taskdef.json', text: json
							sh 'aws ecs register-task-definition --cli-input-json file://prom-taskdef.json'
						}
					}
				}
			}

			stage('Deploy Prometheus Service') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/prometheus || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",       variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY",   variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",      variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_CLUSTER_NAME",        variable: 'ECS_CLUSTER_NAME'),
							string(credentialsId: "${env.ENV_PREFIX}_PROMETHEUS_SERVICE_NAME", variable: 'PROMETHEUS_SERVICE_NAME'),
							string(credentialsId: "${env.ENV_PREFIX}_SUBNET_IDS",              variable: 'SUBNET_IDS'),
							string(credentialsId: "${env.ENV_PREFIX}_SECURITY_GROUP_IDS",      variable: 'SECURITY_GROUP_IDS')
						]) {
							def latestTask = sh(script: 'aws ecs list-task-definitions --family-prefix prometheus --sort DESC --region $AWS_DEFAULT_REGION --query \'taskDefinitionArns[0]\' --output text', returnStdout: true).trim()
							if (!latestTask || latestTask == 'None') {
								error 'No Prometheus TaskDefinition found to deploy'
							}
							def exists = sh(script: 'aws ecs describe-services --cluster $ECS_CLUSTER_NAME --services $PROMETHEUS_SERVICE_NAME --region $AWS_DEFAULT_REGION --query \'services[0].status\' --output text || true', returnStdout: true).trim()
							def subnets = sh(script: 'printf "%s" "$SUBNET_IDS" | awk -F, \'{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}\'', returnStdout: true).trim()
							def sgs = sh(script: 'printf "%s" "$SECURITY_GROUP_IDS" | awk -F, \'{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}\'', returnStdout: true).trim()

							if (exists == "ACTIVE" || exists == "DRAINING") {
								sh """
	                            aws ecs update-service \
	                              --cluster \$ECS_CLUSTER_NAME \
	                              --service \$PROMETHEUS_SERVICE_NAME \
	                              --task-definition ${latestTask} \
	                              --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	                              --desired-count 1 \
	                              --region \$AWS_DEFAULT_REGION \
	                              --force-new-deployment
	                            """
							} else {
								sh """
	                            aws ecs create-service \
	                              --cluster \$ECS_CLUSTER_NAME \
	                              --service-name \$PROMETHEUS_SERVICE_NAME \
	                              --task-definition ${latestTask} \
	                              --desired-count 1 \
	                              --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	                              --network-configuration "awsvpcConfiguration={subnets=[${subnets}],securityGroups=[${sgs}],assignPublicIp=ENABLED}" \
	                              --region \$AWS_DEFAULT_REGION
	                            """
							}
							sh 'aws ecs wait services-stable --cluster $ECS_CLUSTER_NAME --services $PROMETHEUS_SERVICE_NAME --region $AWS_DEFAULT_REGION'
						}
					}
				}
			}

			stage('Register Grafana Task Definition') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/grafana || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",              variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY",          variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",             variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_TASK_EXECUTION_ROLE_ARN",    variable: 'ECS_TASK_EXECUTION_ROLE_ARN'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_TASK_ROLE_ARN",              variable: 'ECS_TASK_ROLE_ARN'),
							string(credentialsId: "${env.ENV_PREFIX}_CLOUDWATCH_LOG_GROUP_GRAFANA",   variable: 'CLOUDWATCH_LOG_GROUP_GRAFANA'),
							string(credentialsId: "${env.ENV_PREFIX}_GRAFANA_ADMIN_PASSWORD",         variable: 'GRAFANA_ADMIN_PASSWORD'),
							string(credentialsId: "${env.ENV_PREFIX}_GRAFANA_DOMAIN",                 variable: 'GRAFANA_DOMAIN'),
							string(credentialsId: "${env.ENV_PREFIX}_GRAFANA_ROOT_URL",               variable: 'GRAFANA_ROOT_URL'),
							string(credentialsId: "${env.ENV_PREFIX}_GRAFANA_SERVE_FROM_SUB_PATH",    variable: 'GRAFANA_SERVE_FROM_SUB_PATH'),
							string(credentialsId: "${env.ENV_PREFIX}_PROMETHEUS_URL",                 variable: 'PROMETHEUS_URL')
						]) {
							sh '''
	                        aws logs create-log-group --log-group-name "$CLOUDWATCH_LOG_GROUP_GRAFANA" --region "$AWS_DEFAULT_REGION" || true
	                        '''
							def grafanaTask = buildGrafanaTaskDefinition(env)
							def json = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(grafanaTask))
							writeFile file: 'grafana-taskdef.json', text: json
							sh 'aws ecs register-task-definition --cli-input-json file://grafana-taskdef.json'
						}
					}
				}
			}

			stage('Deploy Grafana Service') {
				steps {
					script {
						def changed = sh(script: 'git diff --quiet HEAD~1 HEAD monitoring/grafana || echo "changed"', returnStdout: true).trim() == 'changed'
						if (!changed) {
							sleep time: 1, unit: 'SECONDS'; return
						}
						withCredentials([
							string(credentialsId: "${env.ENV_PREFIX}_AWS_ACCESS_KEY_ID",     variable: 'AWS_ACCESS_KEY_ID'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_SECRET_ACCESS_KEY", variable: 'AWS_SECRET_ACCESS_KEY'),
							string(credentialsId: "${env.ENV_PREFIX}_AWS_DEFAULT_REGION",    variable: 'AWS_DEFAULT_REGION'),
							string(credentialsId: "${env.ENV_PREFIX}_ECS_CLUSTER_NAME",      variable: 'ECS_CLUSTER_NAME'),
							string(credentialsId: "${env.ENV_PREFIX}_GRAFANA_SERVICE_NAME",  variable: 'GRAFANA_SERVICE_NAME'),
							string(credentialsId: "${env.ENV_PREFIX}_SUBNET_IDS",            variable: 'SUBNET_IDS'),
							string(credentialsId: "${env.ENV_PREFIX}_SECURITY_GROUP_IDS",    variable: 'SECURITY_GROUP_IDS')
						]) {
							def latestTask = sh(script: 'aws ecs list-task-definitions --family-prefix grafana --sort DESC --region $AWS_DEFAULT_REGION --query \'taskDefinitionArns[0]\' --output text', returnStdout: true).trim()
							if (!latestTask || latestTask == 'None') {
								error 'No Grafana TaskDefinition found to deploy'
							}
							def exists = sh(script: 'aws ecs describe-services --cluster $ECS_CLUSTER_NAME --services $GRAFANA_SERVICE_NAME --region $AWS_DEFAULT_REGION --query \'services[0].status\' --output text || true', returnStdout: true).trim()
							def subnets = sh(script: 'printf "%s" "$SUBNET_IDS" | awk -F, \'{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}\'', returnStdout: true).trim()
							def sgs = sh(script: 'printf "%s" "$SECURITY_GROUP_IDS" | awk -F, \'{for(i=1;i<=NF;i++){gsub(/^ +| +$/,"",$i);printf "%s%s",$i,(i<NF?",":"")}}\'', returnStdout: true).trim()

							if (exists == "ACTIVE" || exists == "DRAINING") {
								sh """
	                            aws ecs update-service \
	                              --cluster \$ECS_CLUSTER_NAME \
	                              --service \$GRAFANA_SERVICE_NAME \
	                              --task-definition ${latestTask} \
	                              --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	                              --desired-count 1 \
	                              --region \$AWS_DEFAULT_REGION \
	                              --force-new-deployment
	                            """
							} else {
								sh """
	                            aws ecs create-service \
	                              --cluster \$ECS_CLUSTER_NAME \
	                              --service-name \$GRAFANA_SERVICE_NAME \
	                              --task-definition ${latestTask} \
	                              --desired-count 1 \
	                              --capacity-provider-strategy capacityProvider=FARGATE,weight=0 capacityProvider=FARGATE_SPOT,weight=1 \
	                              --network-configuration "awsvpcConfiguration={subnets=[${subnets}],securityGroups=[${sgs}],assignPublicIp=ENABLED}" \
	                              --region \$AWS_DEFAULT_REGION
	                            """
							}
							sh 'aws ecs wait services-stable --cluster $ECS_CLUSTER_NAME --services $GRAFANA_SERVICE_NAME --region $AWS_DEFAULT_REGION'
						}
					}
				}
			}

			stage('Archive Artifacts') {
				steps {
					archiveArtifacts artifacts: "${env.SERVICE_DIRECTORY}/build/libs/*.jar", fingerprint: true
				}
			}
			}
		}
	}

	post {
		success {
			echo '====================================Build Success====================================='
			script {
				def helper = load 'jenkins/resolve-and-register.groovy'
				helper.handleBuildSuccess(this)
			}
		}

		failure {
			echo '====================================Build Failed======================================='
			script {
				def helper = load 'jenkins/resolve-and-register.groovy'
				helper.handleBuildFailure(this)
			}
		}

		cleanup {
			echo '====================================Clean workspace===================================='
			cleanWs()
			sh 'docker system prune -f'
		}
	}
}
