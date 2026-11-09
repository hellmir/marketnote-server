import com.cloudbees.groovy.cps.NonCPS

@NonCPS
def resolveServiceMapping(svc, credentials) {
    def mappings = [
        'user-service'       : [ecr: credentials.USER_SERVICE_ECR_REPOSITORY,        ecs: credentials.USER_SERVICE_ECS_SERVICE_NAME,        tg: credentials.USER_SERVICE_TARGET_GROUP_ARN,        origin: credentials.USER_SERVICE_SERVER_ORIGIN],
        'product-service'    : [ecr: credentials.PRODUCT_SERVICE_ECR_REPOSITORY,     ecs: credentials.PRODUCT_SERVICE_ECS_SERVICE_NAME,     tg: credentials.PRODUCT_SERVICE_TARGET_GROUP_ARN,     origin: credentials.PRODUCT_SERVICE_SERVER_ORIGIN],
        'commerce-service'   : [ecr: credentials.COMMERCE_SERVICE_ECR_REPOSITORY,    ecs: credentials.COMMERCE_SERVICE_ECS_SERVICE_NAME,    tg: credentials.COMMERCE_SERVICE_TARGET_GROUP_ARN,    origin: credentials.COMMERCE_SERVICE_SERVER_ORIGIN],
        'community-service'  : [ecr: credentials.COMMUNITY_SERVICE_ECR_REPOSITORY,   ecs: credentials.COMMUNITY_SERVICE_ECS_SERVICE_NAME,   tg: credentials.COMMUNITY_SERVICE_TARGET_GROUP_ARN,   origin: credentials.COMMUNITY_SERVICE_SERVER_ORIGIN],
        'reward-service'     : [ecr: credentials.REWARD_SERVICE_ECR_REPOSITORY,      ecs: credentials.REWARD_SERVICE_ECS_SERVICE_NAME,      tg: credentials.REWARD_SERVICE_TARGET_GROUP_ARN,      origin: credentials.REWARD_SERVICE_SERVER_ORIGIN],
        'fulfillment-service': [ecr: credentials.FULFILLMENT_SERVICE_ECR_REPOSITORY, ecs: credentials.FULFILLMENT_SERVICE_ECS_SERVICE_NAME, tg: credentials.FULFILLMENT_SERVICE_TARGET_GROUP_ARN, origin: credentials.FULFILLMENT_SERVICE_SERVER_ORIGIN],
        'file-service'           : [ecr: credentials.FILE_SERVICE_ECR_REPOSITORY,            ecs: credentials.FILE_SERVICE_ECS_SERVICE_NAME,            tg: credentials.FILE_SERVICE_TARGET_GROUP_ARN,            origin: credentials.FILE_SERVICE_SERVER_ORIGIN],
        'notification-service'   : [ecr: credentials.NOTIFICATION_SERVICE_ECR_REPOSITORY,    ecs: credentials.NOTIFICATION_SERVICE_ECS_SERVICE_NAME,    tg: credentials.NOTIFICATION_SERVICE_TARGET_GROUP_ARN,    origin: credentials.NOTIFICATION_SERVICE_SERVER_ORIGIN],
    ]
    mappings[svc]
}

def resolveServiceMappings(parentScript, String envPrefix) {
    withCredentials([
        string(credentialsId: "${envPrefix}_USER_SERVICE_ECR_REPOSITORY",        variable: 'USER_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_PRODUCT_SERVICE_ECR_REPOSITORY",     variable: 'PRODUCT_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_COMMERCE_SERVICE_ECR_REPOSITORY",    variable: 'COMMERCE_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_COMMUNITY_SERVICE_ECR_REPOSITORY",   variable: 'COMMUNITY_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_REWARD_SERVICE_ECR_REPOSITORY",      variable: 'REWARD_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_FULFILLMENT_SERVICE_ECR_REPOSITORY", variable: 'FULFILLMENT_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_FILE_SERVICE_ECR_REPOSITORY",            variable: 'FILE_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_NOTIFICATION_SERVICE_ECR_REPOSITORY",    variable: 'NOTIFICATION_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: "${envPrefix}_USER_SERVICE_ECS_SERVICE_NAME",          variable: 'USER_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_PRODUCT_SERVICE_ECS_SERVICE_NAME",       variable: 'PRODUCT_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_COMMERCE_SERVICE_ECS_SERVICE_NAME",      variable: 'COMMERCE_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_COMMUNITY_SERVICE_ECS_SERVICE_NAME",     variable: 'COMMUNITY_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_REWARD_SERVICE_ECS_SERVICE_NAME",        variable: 'REWARD_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_FULFILLMENT_SERVICE_ECS_SERVICE_NAME",   variable: 'FULFILLMENT_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_FILE_SERVICE_ECS_SERVICE_NAME",              variable: 'FILE_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_NOTIFICATION_SERVICE_ECS_SERVICE_NAME",  variable: 'NOTIFICATION_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: "${envPrefix}_USER_SERVICE_TARGET_GROUP_ARN",          variable: 'USER_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_PRODUCT_SERVICE_TARGET_GROUP_ARN",       variable: 'PRODUCT_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_COMMERCE_SERVICE_TARGET_GROUP_ARN",      variable: 'COMMERCE_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_COMMUNITY_SERVICE_TARGET_GROUP_ARN",     variable: 'COMMUNITY_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_REWARD_SERVICE_TARGET_GROUP_ARN",        variable: 'REWARD_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_FULFILLMENT_SERVICE_TARGET_GROUP_ARN",   variable: 'FULFILLMENT_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_FILE_SERVICE_TARGET_GROUP_ARN",              variable: 'FILE_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_NOTIFICATION_SERVICE_TARGET_GROUP_ARN",  variable: 'NOTIFICATION_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: "${envPrefix}_USER_SERVICE_SERVER_ORIGIN",         variable: 'USER_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_PRODUCT_SERVICE_SERVER_ORIGIN",      variable: 'PRODUCT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_COMMERCE_SERVICE_SERVER_ORIGIN",     variable: 'COMMERCE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_COMMUNITY_SERVICE_SERVER_ORIGIN",    variable: 'COMMUNITY_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_REWARD_SERVICE_SERVER_ORIGIN",       variable: 'REWARD_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_FULFILLMENT_SERVICE_SERVER_ORIGIN",  variable: 'FULFILLMENT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_FILE_SERVICE_SERVER_ORIGIN",             variable: 'FILE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_NOTIFICATION_SERVICE_SERVER_ORIGIN", variable: 'NOTIFICATION_SERVICE_SERVER_ORIGIN'),
    ]) {
        def creds = [
            USER_SERVICE_ECR_REPOSITORY: USER_SERVICE_ECR_REPOSITORY, USER_SERVICE_ECS_SERVICE_NAME: USER_SERVICE_ECS_SERVICE_NAME, USER_SERVICE_TARGET_GROUP_ARN: USER_SERVICE_TARGET_GROUP_ARN, USER_SERVICE_SERVER_ORIGIN: USER_SERVICE_SERVER_ORIGIN,
            PRODUCT_SERVICE_ECR_REPOSITORY: PRODUCT_SERVICE_ECR_REPOSITORY, PRODUCT_SERVICE_ECS_SERVICE_NAME: PRODUCT_SERVICE_ECS_SERVICE_NAME, PRODUCT_SERVICE_TARGET_GROUP_ARN: PRODUCT_SERVICE_TARGET_GROUP_ARN, PRODUCT_SERVICE_SERVER_ORIGIN: PRODUCT_SERVICE_SERVER_ORIGIN,
            COMMERCE_SERVICE_ECR_REPOSITORY: COMMERCE_SERVICE_ECR_REPOSITORY, COMMERCE_SERVICE_ECS_SERVICE_NAME: COMMERCE_SERVICE_ECS_SERVICE_NAME, COMMERCE_SERVICE_TARGET_GROUP_ARN: COMMERCE_SERVICE_TARGET_GROUP_ARN, COMMERCE_SERVICE_SERVER_ORIGIN: COMMERCE_SERVICE_SERVER_ORIGIN,
            COMMUNITY_SERVICE_ECR_REPOSITORY: COMMUNITY_SERVICE_ECR_REPOSITORY, COMMUNITY_SERVICE_ECS_SERVICE_NAME: COMMUNITY_SERVICE_ECS_SERVICE_NAME, COMMUNITY_SERVICE_TARGET_GROUP_ARN: COMMUNITY_SERVICE_TARGET_GROUP_ARN, COMMUNITY_SERVICE_SERVER_ORIGIN: COMMUNITY_SERVICE_SERVER_ORIGIN,
            REWARD_SERVICE_ECR_REPOSITORY: REWARD_SERVICE_ECR_REPOSITORY, REWARD_SERVICE_ECS_SERVICE_NAME: REWARD_SERVICE_ECS_SERVICE_NAME, REWARD_SERVICE_TARGET_GROUP_ARN: REWARD_SERVICE_TARGET_GROUP_ARN, REWARD_SERVICE_SERVER_ORIGIN: REWARD_SERVICE_SERVER_ORIGIN,
            FULFILLMENT_SERVICE_ECR_REPOSITORY: FULFILLMENT_SERVICE_ECR_REPOSITORY, FULFILLMENT_SERVICE_ECS_SERVICE_NAME: FULFILLMENT_SERVICE_ECS_SERVICE_NAME, FULFILLMENT_SERVICE_TARGET_GROUP_ARN: FULFILLMENT_SERVICE_TARGET_GROUP_ARN, FULFILLMENT_SERVICE_SERVER_ORIGIN: FULFILLMENT_SERVICE_SERVER_ORIGIN,
            FILE_SERVICE_ECR_REPOSITORY: FILE_SERVICE_ECR_REPOSITORY, FILE_SERVICE_ECS_SERVICE_NAME: FILE_SERVICE_ECS_SERVICE_NAME, FILE_SERVICE_TARGET_GROUP_ARN: FILE_SERVICE_TARGET_GROUP_ARN, FILE_SERVICE_SERVER_ORIGIN: FILE_SERVICE_SERVER_ORIGIN,
            NOTIFICATION_SERVICE_ECR_REPOSITORY: NOTIFICATION_SERVICE_ECR_REPOSITORY, NOTIFICATION_SERVICE_ECS_SERVICE_NAME: NOTIFICATION_SERVICE_ECS_SERVICE_NAME, NOTIFICATION_SERVICE_TARGET_GROUP_ARN: NOTIFICATION_SERVICE_TARGET_GROUP_ARN, NOTIFICATION_SERVICE_SERVER_ORIGIN: NOTIFICATION_SERVICE_SERVER_ORIGIN,
        ]
        def mapping = resolveServiceMapping(env.SERVICE_NAME, creds)
        if (!mapping) {
            error "SERVICE_NAME not mapped: ${env.SERVICE_NAME}"
        }
        env.ECR_REPOSITORY   = mapping.ecr
        env.ECS_SERVICE_NAME = mapping.ecs
        env.TARGET_GROUP_ARN = mapping.tg
        env.SERVER_ORIGIN    = mapping.origin

        if (!env.ECR_REPOSITORY?.trim())   error "ECR_REPOSITORY not resolved for ${env.SERVICE_NAME}"
        if (!env.ECS_SERVICE_NAME?.trim()) error "ECS_SERVICE_NAME not resolved for ${env.SERVICE_NAME}"
        if (!env.TARGET_GROUP_ARN?.trim()) error "TARGET_GROUP_ARN not resolved for ${env.SERVICE_NAME}"

        echo "ECR_REPOSITORY   = ${env.ECR_REPOSITORY}"
        echo "ECS_SERVICE_NAME = ${env.ECS_SERVICE_NAME}"
        echo "TARGET_GROUP_ARN = ${env.TARGET_GROUP_ARN}"
    }
}

def dbCredentialBindings(String envPrefix) {
    return [
        string(credentialsId: "${envPrefix}_USER_SERVICE_DB_URL",              variable: 'USER_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_USER_SERVICE_DB_PASSWORD",         variable: 'USER_SERVICE_DB_PASSWORD'),
        string(credentialsId: "${envPrefix}_PRODUCT_SERVICE_DB_URL",           variable: 'PRODUCT_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_PRODUCT_SERVICE_DB_PASSWORD",      variable: 'PRODUCT_SERVICE_DB_PASSWORD'),
        string(credentialsId: "${envPrefix}_COMMERCE_SERVICE_DB_URL",          variable: 'COMMERCE_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_COMMERCE_SERVICE_DB_PASSWORD",     variable: 'COMMERCE_SERVICE_DB_PASSWORD'),
        string(credentialsId: "${envPrefix}_COMMUNITY_SERVICE_DB_URL",         variable: 'COMMUNITY_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_COMMUNITY_SERVICE_DB_PASSWORD",    variable: 'COMMUNITY_SERVICE_DB_PASSWORD'),
        string(credentialsId: "${envPrefix}_REWARD_SERVICE_DB_URL",            variable: 'REWARD_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_REWARD_SERVICE_DB_PASSWORD",       variable: 'REWARD_SERVICE_DB_PASSWORD'),
        string(credentialsId: "${envPrefix}_FULFILLMENT_SERVICE_DB_URL",       variable: 'FULFILLMENT_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_FULFILLMENT_SERVICE_DB_PASSWORD",  variable: 'FULFILLMENT_SERVICE_DB_PASSWORD'),
        string(credentialsId: "${envPrefix}_FILE_SERVICE_DB_URL",              variable: 'FILE_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_FILE_SERVICE_DB_PASSWORD",         variable: 'FILE_SERVICE_DB_PASSWORD'),
        string(credentialsId: "${envPrefix}_NOTIFICATION_SERVICE_DB_URL",      variable: 'NOTIFICATION_SERVICE_DB_URL'),
        string(credentialsId: "${envPrefix}_NOTIFICATION_SERVICE_DB_PASSWORD", variable: 'NOTIFICATION_SERVICE_DB_PASSWORD'),
    ]
}

def runFlywayMigrationStage(parentScript, String envPrefix) {
    def pending = getPendingMigrationFiles(parentScript, envPrefix)
    if (pending.isEmpty()) {
        echo "No pending Flyway migrations for ${env.SERVICE_NAME}. Skipping migration."
        return
    }
    def fileList = formatPendingMigrationList(pending)
    echo "Pending Flyway migrations (${pending.size()}):\n${fileList}"

    if (envPrefix == 'MARKETNOTE_PROD') {
        confirmFlywayMigrationOnProd(pending.size(), fileList)
    }
    timeout(time: 30, unit: 'MINUTES') {
        runFlywayMigration(parentScript, envPrefix)
    }
    echo 'Flyway migration complete'
}

def confirmFlywayMigrationOnProd(int pendingCount, String fileList) {
    timeout(time: 15, unit: 'MINUTES') {
        input message: "prod Flyway 마이그레이션 ${pendingCount}건을 실행합니다. (서비스: ${env.SERVICE_NAME}, 버전: ${env.PROJECT_VERSION})\n적용 예정:\n${fileList}",
              ok: 'Proceed'
    }
}

@NonCPS
def formatPendingMigrationList(List pending) {
    return pending.collect { "  - ${it.version} ${it.description}".toString() }.join('\n')
}

def buildFlywayDockerCmd(String svcUpper, String migrationDir, String lastArg) {
    def dbUrlVar = "${svcUpper}_DB_URL"
    def dbPwVar = "${svcUpper}_DB_PASSWORD"
    return "docker run --rm -v '${migrationDir}':/flyway/sql " +
        "-e FLYWAY_URL=\"\$${dbUrlVar}\" " +
        '-e FLYWAY_USER="$DB_USERNAME" ' +
        "-e FLYWAY_PASSWORD=\"\$${dbPwVar}\" " +
        '-e FLYWAY_BASELINE_ON_MIGRATE=true ' +
        '-e FLYWAY_BASELINE_VERSION=0 ' +
        '-e FLYWAY_MIXED=true ' +
        "flyway/flyway:11.7 ${lastArg}"
}

def getPendingMigrationFiles(parentScript, String envPrefix) {
    def pending = []
    withCredentials(dbCredentialBindings(envPrefix) + [
        string(credentialsId: "${envPrefix}_DB_USERNAME", variable: 'DB_USERNAME')
    ]) {
        def svcUpper = env.SERVICE_NAME.toUpperCase().replace('-', '_')
        def migrationDir = "${env.WORKSPACE}/${env.SERVICE_DIRECTORY}/src/main/resources/db/migration"
        def dockerCmd = buildFlywayDockerCmd(svcUpper, migrationDir, '-outputType=json info')
        // fail-secure: returnStdout=true는 exit≠0이면 throw → stage abort. returnStatus로 변경 금지(prod input 게이트 우회 회귀 방지).
        def stdout = sh(script: dockerCmd, returnStdout: true).trim()
        try {
            pending = parsePendingMigrations(stdout)
        } catch (Exception e) {
            error "Failed to parse Flyway info JSON output: ${e.message}\nStdout (first 500 chars): ${stdout.take(500)}"
        }
    }
    return pending
}

@NonCPS
def parsePendingMigrations(String jsonOutput) {
    def parsed = new groovy.json.JsonSlurper().parseText(jsonOutput)
    def migrations = parsed?.migrations ?: []
    return migrations
        .findAll { it.state == 'Pending' }
        .collect { [
            version: it.version?.toString(),
            description: it.description?.toString(),
            filename: (it.filepath ?: it.description)?.toString()
        ] }
}

def runFlywayMigration(parentScript, String envPrefix) {
    withCredentials(dbCredentialBindings(envPrefix) + [
        string(credentialsId: "${envPrefix}_DB_USERNAME", variable: 'DB_USERNAME')
    ]) {
        def svcUpper = env.SERVICE_NAME.toUpperCase().replace('-', '_')
        def migrationDir = "${env.WORKSPACE}/${env.SERVICE_DIRECTORY}/src/main/resources/db/migration"
        def dockerCmd = buildFlywayDockerCmd(svcUpper, migrationDir, 'migrate')
        sh dockerCmd
    }
}

def runConcurrentIndexes(parentScript, String envPrefix) {
    withCredentials(dbCredentialBindings(envPrefix) + [
        string(credentialsId: "${envPrefix}_DB_USERNAME", variable: 'DB_USERNAME')
    ]) {
        def svcUpper = env.SERVICE_NAME.toUpperCase().replace('-', '_')
        def indexDir = "${env.WORKSPACE}/${env.SERVICE_DIRECTORY}/src/main/resources/db/concurrent-indexes"
        def scriptPath = "${env.WORKSPACE}/jenkins/run-concurrent-indexes.sh"
        def dbUrlVar = "${svcUpper}_DB_URL"
        def dbPwVar = "${svcUpper}_DB_PASSWORD"
        def dockerCmd = "docker run --rm " +
            "-v '${indexDir}':/concurrent-indexes:ro " +
            "-v '${scriptPath}':/run.sh:ro " +
            "-e DB_URL=\"\$${dbUrlVar}\" " +
            '-e DB_USER="$DB_USERNAME" ' +
            "-e DB_PASSWORD=\"\$${dbPwVar}\" " +
            '--entrypoint bash ' +
            'postgres:17-alpine ' +
            '/run.sh /concurrent-indexes'
        sh dockerCmd
    }
}

def registerTaskDefinition(parentScript, String envPrefix) {
    withCredentials([
        string(credentialsId: "${envPrefix}_DB_USERNAME",                           variable: 'DB_USERNAME'),
        string(credentialsId: "${envPrefix}_JWT_SECRET_KEY",                        variable: 'JWT_SECRET_KEY'),
        string(credentialsId: "${envPrefix}_ACCESS_TOKEN_EXPIRATION_TIME",          variable: 'ACCESS_TOKEN_EXPIRATION_TIME'),
        string(credentialsId: "${envPrefix}_REFRESH_TOKEN_EXPIRATION_TIME",         variable: 'REFRESH_TOKEN_EXPIRATION_TIME'),
        string(credentialsId: "${envPrefix}_CLIENT_ORIGIN",                         variable: 'CLIENT_ORIGIN'),
        string(credentialsId: "${envPrefix}_COOKIE_DOMAIN",                         variable: 'COOKIE_DOMAIN'),
        string(credentialsId: "${envPrefix}_ACCESS_CONTROL_ALLOWED_ORIGINS",        variable: 'ACCESS_CONTROL_ALLOWED_ORIGINS'),
        string(credentialsId: "${envPrefix}_SPRING_PROFILE",                variable: 'SPRING_PROFILE'),
        string(credentialsId: "${envPrefix}_GOOGLE_CLIENT_ID",                      variable: 'GOOGLE_CLIENT_ID'),
        string(credentialsId: "${envPrefix}_GOOGLE_CLIENT_SECRET",                  variable: 'GOOGLE_CLIENT_SECRET'),
        string(credentialsId: "${envPrefix}_KAKAO_CLIENT_ID",                       variable: 'KAKAO_CLIENT_ID'),
        string(credentialsId: "${envPrefix}_KAKAO_CLIENT_SECRET",                   variable: 'KAKAO_CLIENT_SECRET'),
        string(credentialsId: "${envPrefix}_KAKAO_ADMIN_KEY",                       variable: 'KAKAO_ADMIN_KEY'),
        string(credentialsId: "${envPrefix}_KAKAO_ALLOWED_AUDIENCES",       variable: 'KAKAO_ALLOWED_AUDIENCES'),
        string(credentialsId: "${envPrefix}_GOOGLE_ALLOWED_AUDIENCES",      variable: 'GOOGLE_ALLOWED_AUDIENCES'),
        string(credentialsId: "${envPrefix}_APPLE_ALLOWED_AUDIENCES",       variable: 'APPLE_ALLOWED_AUDIENCES'),
        string(credentialsId: "${envPrefix}_S3_ACCESS_KEY",                         variable: 'S3_ACCESS_KEY'),
        string(credentialsId: "${envPrefix}_S3_SECRET_KEY",                         variable: 'S3_SECRET_KEY'),
        string(credentialsId: "${envPrefix}_S3_BUCKET_NAME",                        variable: 'S3_BUCKET_NAME'),
        string(credentialsId: "${envPrefix}_AWS_ACCOUNT_ID",                        variable: 'AWS_ACCOUNT_ID'),
        string(credentialsId: "${envPrefix}_AWS_ACCESS_KEY_ID",                     variable: 'AWS_ACCESS_KEY_ID'),
        string(credentialsId: "${envPrefix}_AWS_SECRET_ACCESS_KEY",                 variable: 'AWS_SECRET_ACCESS_KEY'),
        string(credentialsId: "${envPrefix}_AWS_DEFAULT_REGION",                    variable: 'AWS_DEFAULT_REGION'),
        string(credentialsId: "${envPrefix}_ECS_TASK_EXECUTION_ROLE_ARN",           variable: 'ECS_TASK_EXECUTION_ROLE_ARN'),
        string(credentialsId: "${envPrefix}_ECS_TASK_ROLE_ARN",                     variable: 'ECS_TASK_ROLE_ARN'),
        string(credentialsId: "${envPrefix}_CLOUDWATCH_LOG_GROUP",                  variable: 'CLOUDWATCH_LOG_GROUP'),
        string(credentialsId: "${envPrefix}_SES_SMTP_USERNAME",                     variable: 'SES_SMTP_USERNAME'),
        string(credentialsId: "${envPrefix}_SES_SMTP_PASSWORD",                     variable: 'SES_SMTP_PASSWORD'),
        string(credentialsId: "${envPrefix}_MAIL_FROM",                             variable: 'MAIL_FROM'),
        string(credentialsId: "${envPrefix}_MAIL_SENDER_NAME",                      variable: 'MAIL_SENDER_NAME'),
        string(credentialsId: "${envPrefix}_MAIL_VERIFICATION_TTL_MINUTES",         variable: 'MAIL_VERIFICATION_TTL_MINUTES'),
        string(credentialsId: "${envPrefix}_REDIS_PASSWORD",                        variable: 'REDIS_PASSWORD'),
        string(credentialsId: "${envPrefix}_REDIS_HOST_NAME",                       variable: 'REDIS_HOST_NAME'),
        string(credentialsId: "${envPrefix}_REDIS_EMAIL_VERIFICATION_PREFIX",       variable: 'REDIS_EMAIL_VERIFICATION_PREFIX'),
        string(credentialsId: "${envPrefix}_FILE_SERVICE_SERVER_ORIGIN",         variable: 'FILE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_PRODUCT_SERVICE_SERVER_ORIGIN",      variable: 'PRODUCT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_COMMERCE_SERVICE_SERVER_ORIGIN",     variable: 'COMMERCE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_COMMUNITY_SERVICE_SERVER_ORIGIN",    variable: 'COMMUNITY_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_REWARD_SERVICE_SERVER_ORIGIN",       variable: 'REWARD_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_FULFILLMENT_SERVICE_SERVER_ORIGIN",      variable: 'FULFILLMENT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_NOTIFICATION_SERVICE_SERVER_ORIGIN",  variable: 'NOTIFICATION_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: "${envPrefix}_JWT_ADMIN_ACCESS_TOKEN",             variable: 'JWT_ADMIN_ACCESS_TOKEN'),
        string(credentialsId: "${envPrefix}_ADPOPCORN_ANDROID_HASH_KEY",         variable: 'ADPOPCORN_ANDROID_HASH_KEY'),
        string(credentialsId: "${envPrefix}_ADPOPCORN_IOS_HASH_KEY",             variable: 'ADPOPCORN_IOS_HASH_KEY'),
        string(credentialsId: "${envPrefix}_TNK_ANDROID_HASH_KEY",               variable: 'TNK_ANDROID_HASH_KEY'),
        string(credentialsId: "${envPrefix}_TNK_IOS_HASH_KEY",                   variable: 'TNK_IOS_HASH_KEY'),
        string(credentialsId: "${envPrefix}_ADISCOPE_ANDROID_HASH_KEY",          variable: 'ADISCOPE_ANDROID_HASH_KEY'),
        string(credentialsId: "${envPrefix}_ADISCOPE_IOS_HASH_KEY",              variable: 'ADISCOPE_IOS_HASH_KEY'),
        string(credentialsId: "${envPrefix}_FASSTO_BASE_URL",                    variable: 'FASSTO_BASE_URL'),
        string(credentialsId: "${envPrefix}_FASSTO_API_CD",                      variable: 'FASSTO_API_CD'),
        string(credentialsId: "${envPrefix}_FASSTO_API_KEY",                     variable: 'FASSTO_API_KEY'),
        string(credentialsId: "${envPrefix}_FASSTO_CUSTOMER_CODE",               variable: 'FASSTO_CUSTOMER_CODE'),
        string(credentialsId: "${envPrefix}_KAFKA_BOOTSTRAP_SERVERS",            variable: 'KAFKA_BOOTSTRAP_SERVERS'),
        string(credentialsId: "${envPrefix}_KAFKA_SLACK_WEBHOOK_URL",            variable: 'KAFKA_SLACK_WEBHOOK_URL'),
        string(credentialsId: "${envPrefix}_KAFKA_SASL_ENABLED",                 variable: 'KAFKA_SASL_ENABLED'),
        string(credentialsId: "${envPrefix}_KAFKA_SASL_MECHANISM",               variable: 'KAFKA_SASL_MECHANISM'),
        string(credentialsId: "${envPrefix}_KAFKA_SASL_PROTOCOL",                variable: 'KAFKA_SASL_PROTOCOL'),
        string(credentialsId: "${envPrefix}_KAFKA_SASL_USERNAME",                variable: 'KAFKA_SASL_USERNAME'),
        string(credentialsId: "${envPrefix}_KAFKA_SASL_PASSWORD",                variable: 'KAFKA_SASL_PASSWORD'),
        string(credentialsId: "${envPrefix}_KAFKA_SSL_TRUSTSTORE_PASSWORD",    variable: 'KAFKA_SSL_TRUSTSTORE_PASSWORD'),
        string(credentialsId: "${envPrefix}_HMAC_SECRET_KEY",                    variable: 'HMAC_SECRET_KEY'),
        string(credentialsId: "${envPrefix}_GIFTICON_PIN_ENCRYPT_KEY",           variable: 'GIFTICON_PIN_ENCRYPT_KEY'),
        string(credentialsId: "${envPrefix}_GIFTICON_SYNC_SCHEDULER_ENABLED",   variable: 'GIFTICON_SYNC_SCHEDULER_ENABLED'),
        string(credentialsId: "${envPrefix}_GIFTICON_SYNC_SCHEDULER_CRON",      variable: 'GIFTICON_SYNC_SCHEDULER_CRON'),
        string(credentialsId: "${envPrefix}_GIFTICON_COUPON_SYNC_SCHEDULER_ENABLED", variable: 'GIFTICON_COUPON_SYNC_SCHEDULER_ENABLED'),
        string(credentialsId: "${envPrefix}_GIFTICON_COUPON_SYNC_SCHEDULER_CRON",    variable: 'GIFTICON_COUPON_SYNC_SCHEDULER_CRON'),
        string(credentialsId: "${envPrefix}_FIREBASE_SERVICE_ACCOUNT_JSON",           variable: 'FIREBASE_SERVICE_ACCOUNT_JSON'),
    ] + dbCredentialBindings(envPrefix)) {
        sh '''
          LG="$CLOUDWATCH_LOG_GROUP"
          EXISTS=$(aws logs describe-log-groups --log-group-name-prefix "$LG" --region "$AWS_DEFAULT_REGION" --query "length(logGroups[?logGroupName=='$LG'])" --output text || echo 0)
          if [ "$EXISTS" = "0" ]; then
            aws logs create-log-group --log-group-name "$LG" --region "$AWS_DEFAULT_REGION"
          fi
        '''

        def dbCreds = [
            USER_SERVICE_DB_URL: USER_SERVICE_DB_URL, USER_SERVICE_DB_PASSWORD: USER_SERVICE_DB_PASSWORD,
            PRODUCT_SERVICE_DB_URL: PRODUCT_SERVICE_DB_URL, PRODUCT_SERVICE_DB_PASSWORD: PRODUCT_SERVICE_DB_PASSWORD,
            COMMERCE_SERVICE_DB_URL: COMMERCE_SERVICE_DB_URL, COMMERCE_SERVICE_DB_PASSWORD: COMMERCE_SERVICE_DB_PASSWORD,
            COMMUNITY_SERVICE_DB_URL: COMMUNITY_SERVICE_DB_URL, COMMUNITY_SERVICE_DB_PASSWORD: COMMUNITY_SERVICE_DB_PASSWORD,
            REWARD_SERVICE_DB_URL: REWARD_SERVICE_DB_URL, REWARD_SERVICE_DB_PASSWORD: REWARD_SERVICE_DB_PASSWORD,
            FULFILLMENT_SERVICE_DB_URL: FULFILLMENT_SERVICE_DB_URL, FULFILLMENT_SERVICE_DB_PASSWORD: FULFILLMENT_SERVICE_DB_PASSWORD,
            FILE_SERVICE_DB_URL: FILE_SERVICE_DB_URL, FILE_SERVICE_DB_PASSWORD: FILE_SERVICE_DB_PASSWORD,
            NOTIFICATION_SERVICE_DB_URL: NOTIFICATION_SERVICE_DB_URL, NOTIFICATION_SERVICE_DB_PASSWORD: NOTIFICATION_SERVICE_DB_PASSWORD,
        ]
        def svcUpper = env.SERVICE_NAME.toUpperCase().replace('-', '_')
        def dbUrl = dbCreds["${svcUpper}_DB_URL"]
        def dbPassword = dbCreds["${svcUpper}_DB_PASSWORD"]
        if (!dbUrl?.trim())      error "${svcUpper}_DB_URL credential not resolved for ${env.SERVICE_NAME}"
        if (!dbPassword?.trim()) error "${svcUpper}_DB_PASSWORD credential not resolved for ${env.SERVICE_NAME}"

        def td = parentScript.buildMarketnoteTaskDefinition(env, dbUrl, dbPassword)

        def json = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(td))
        writeFile file: 'taskdef.json', text: json

        def out = sh(
            script: 'set -eu; aws ecs register-task-definition --cli-input-json file://taskdef.json --region $AWS_DEFAULT_REGION --query "taskDefinition.taskDefinitionArn" --output text',
            returnStdout: true
        ).trim()

        if (!out || out == 'None') {
            error 'Failed to register application task definition'
        }

        env.APP_TASK_DEF_ARN = out
        echo "Registered TaskDef ARN = ${env.APP_TASK_DEF_ARN}"
    }
}

def resolveBranchLabel(parentScript) {
    return sh(
        script: '''
          if [ -n "$BRANCH_NAME" ]; then
            echo "$BRANCH_NAME"
            exit 0
          fi
          if [ -n "$CHANGE_ID" ] && [ -n "$CHANGE_BRANCH" ] && [ -n "$CHANGE_TARGET" ]; then
            echo "PR #$CHANGE_ID ($CHANGE_BRANCH -> $CHANGE_TARGET)"
            exit 0
          fi
          if [ -n "$GIT_BRANCH" ]; then
            echo "${GIT_BRANCH#origin/}"
            exit 0
          fi
          bn=$(git rev-parse --abbrev-ref HEAD 2>/dev/null)
          if [ "$bn" != "HEAD" ] && [ -n "$bn" ]; then
            echo "$bn"
            exit 0
          fi
          guess=$(git for-each-ref --format="%(refname:short)" --contains HEAD refs/remotes/origin | sed "s#^origin/##" | head -n1)
          if [ -n "$guess" ]; then
            echo "$guess"
            exit 0
          fi
          tag=$(git describe --tags --exact-match 2>/dev/null || true)
          if [ -n "$tag" ]; then
            echo "tag: $tag"
            exit 0
          fi
          name=$(git name-rev --name-only HEAD 2>/dev/null | sed "s#remotes/origin/##;s#~[0-9]*##")
          if [ -n "$name" ] && [ "$name" != "undefined" ]; then
            echo "$name"
            exit 0
          fi
          git rev-parse --short HEAD
        ''',
        returnStdout: true
    ).trim()
}

// SKIP_REASON 파라미터 화이트리스트 검증. 형식: "#<PR번호> <설명>" (설명 1~200자, 줄바꿈/탭/Unicode 줄바꿈/C1 제어 금지).
// Slack mrkdwn 메시지 주입(줄바꿈 위장으로 AUTHOR/COMMIT 라인 조작) 차단 + 긴급 revert PR 추적성 강제.
// SKIP=true 경로에서만 호출되며 (notifyFlywayImmutabilitySkip 진입 시점) SKIP=false 빌드에는 영향 없음.
// 구분자는 리터럴 공백(`[ ]+`)로 한정 — `\s` 는 \n/\t 를 포함하므로 인젝션 차단 목적에 부적합.
// #4823: C0(\x00-\x1F) + DEL(\x7F) + C1(\x80-\x9F: NEL U+0085 포함) + Unicode 줄바꿈(U+2028 LS, U+2029 PS) 차단.
def validateSkipReason(String reason) {
    if (!reason?.trim()) {
        error "SKIP_REASON 파라미터가 비어있다. SKIP_FLYWAY_IMMUTABILITY_CHECK=true 사용 시 필수. 형식: '#<PR번호> <설명>' (예: '#4806 마이그레이션 V102 revert')"
    }
    def trimmed = reason.trim()
    if (!(trimmed ==~ /^#\d+ +[^\x00-\x1F\x7F-\x9F\u2028\u2029]{1,200}$/)) {
        error "SKIP_REASON 형식 위반: '${reason}'. 허용 형식: '#<PR번호> <설명>' (설명 1~200자, 줄바꿈/탭/Unicode 줄바꿈/C1 제어 금지, 구분자는 공백만)"
    }
}

// Slack mrkdwn 메시지 보간 입력에서 줄바꿈/탭/제어문자 제거 + 길이 상한.
// 강조 문자(* _ < > | `)는 통과 (#4810 SKIP_REASON 검증과 일관, 라인 위장만 차단).
// 핵심 위협: commit message body에 `\nAUTHOR: attacker` 같은 위장 라인 삽입 → AUTHOR/COMMIT 라인 조작.
// #4823: 기존 C0(\x00-\x1F)+DEL(\x7F) 차단에 C1(\x80-\x9F: NEL 포함) + Unicode 줄바꿈(U+2028 LS, U+2029 PS) 추가.
def sanitizeSlackLine(String value, int maxLen) {
    if (value == null) return ''
    def cleaned = value.replaceAll(/[\x00-\x1F\x7F-\x9F\u2028\u2029]/, ' ').trim()
    if (cleaned.length() > maxLen) {
        cleaned = cleaned.substring(0, maxLen) + '...'
    }
    return cleaned
}

// CPS 직렬화 안전성 확보 — 기본 인자 대신 명시 오버로드.
def sanitizeSlackLine(String value) {
    return sanitizeSlackLine(value, 200)
}

// commit message 첫 줄(subject)만 추출 후 정화. body 라인 위장 벡터 원천 차단.
def sanitizeCommitSubject(String commitMessage) {
    if (commitMessage == null) return ''
    def firstLine = commitMessage.split(/\r?\n/, 2)[0]
    return sanitizeSlackLine(firstLine, 200)
}

def notifyFlywayImmutabilitySkip(parentScript) {
    def rawReason = parentScript.params.SKIP_REASON
    validateSkipReason(rawReason)
    def skipReason = rawReason.trim()
    def skipCommitAuthor = sanitizeSlackLine(sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim())
    def branchLabel = sanitizeSlackLine(env.BRANCH_LABEL?.trim() ?: resolveBranchLabel(parentScript))
    def ch = env.SLACK_NOTIFICATION_CHANNEL?.trim()
    if (ch) {
        slackSend(
            channel: "${ch}",
            color:   "${env.SLACK_FAIL_COLOR}",
            message: "[AUDIT] Flyway immutability check SKIPPED\n"
                + "SERVICE: ${parentScript.params.SERVICE}\n"
                + "BUILD_URL: ${env.BUILD_URL}\n"
                + "COMMIT: ${env.GIT_COMMIT}\n"
                + "BRANCH: ${branchLabel}\n"
                + "AUTHOR: ${skipCommitAuthor}\n"
                + "REASON: ${skipReason}"
        )
        return
    }
    echo 'SLACK_NOTIFICATION_CHANNEL unresolved; skipping Slack SKIP audit notification'
}

// cron 재배포 skip 가드 — QA Jenkinsfile 'Skip If Same Commit' stage에서 호출.
// parentScript.currentBuild / parentScript.env 통해 상태 갱신. skip 시 result=NOT_BUILT.
// 이후 stage는 nested stage의 단일 when { not { environment CRON_SKIP } }로 skip 처리.
def performCronSkipGuard(parentScript) {
    def build = parentScript.currentBuild

    if (!isCronTriggered(build)) {
        echo "Not a cron-triggered build. Skip-guard inactive."
        return
    }
    env.CRON_TRIGGERED = 'true'

    if (!env.GIT_COMMIT?.trim()) {
        echo "[QA-CRON] GIT_COMMIT unresolved. Skip-guard bypassed (fail-open)."
        return
    }

    def lastCommit = findLastDeployedCommit(build, env.SERVICE_NAME)
    boolean sameCommit = lastCommit && lastCommit == env.GIT_COMMIT
    if (!sameCommit) {
        echo "[QA-CRON] SERVICE=${env.SERVICE_NAME} 배포 진행. last=${lastCommit ?: 'none'} current=${env.GIT_COMMIT}"
        return
    }

    env.CRON_SKIP = 'true'
    build.result = 'NOT_BUILT'
    echo "[QA-CRON] SERVICE=${env.SERVICE_NAME} COMMIT=${env.GIT_COMMIT} 이미 배포됨. 재배포 skip."
}

// Environment Setup에서 호출 — CRON_TRIGGERED / parameterizedCron spec 로그.
// Jenkinsfile CPS 메서드 크기 축소 목적으로 분리. buildQaCronSpec은 parentScript(Jenkinsfile) 소유.
def logCronInfo(parentScript) {
    echo "CRON_TRIGGERED            = ${env.CRON_TRIGGERED ?: 'false'}"
    echo "parameterizedCron spec:\n" + parentScript.buildQaCronSpec()
}

// JOB_NAME 의 -qa/-stage/-prod 접미사로부터 ENV_PREFIX(MARKETNOTE_QA/MARKETNOTE_STAGE/MARKETNOTE_PROD) 도출.
// Environment Setup stage 의 동일 로직을 dispatcher 분기에서도 재사용 (ENV_PREFIX 미설정 상태에서 credential 로드 필요).
// 잡 이름이 -qa/-stage/-prod 로 끝나지 않으면 fail-fast.
def resolveEnvPrefixFromJobName(parentScript) {
    def jobName = env.JOB_NAME ?: ''
    def projectName = jobName.split('/')[0]
    def matcher = (projectName =~ /-(qa|stage|prod)$/)
    if (!matcher.find()) {
        error "JOB_NAME '${jobName}' (project='${projectName}') must end with one of: -qa, -stage, -prod"
    }
    def environment = matcher.group(1).toUpperCase()
    matcher = null
    return [envPrefix: "MARKETNOTE_${environment}", envUpper: environment]
}

// Select Target Service stage 본문 — 변경 파일 감지 → 후보 서비스 결정 → dispatcher 분기 / 단일 서비스 분기.
// Jenkinsfile inline 분량을 helper 로 이관하여 WorkflowScript CPS 메서드 64KB 한계 회피 (#4725 선례 패턴).
// 부수효과:
//   - dispatcher 분기: env.DISPATCHER_MODE='true', env.CANDIDATE_SERVICES, currentBuild.description, 자식 build N건 트리거
//   - 단일 서비스 분기: env.SERVICE_NAME, env.SERVICE_DIRECTORY, env.CANDIDATE_SERVICES
def selectTargetService(parentScript) {
    def base = sh(script: 'git rev-parse HEAD~1 || git rev-parse HEAD', returnStdout: true).trim()
    def changed = sh(script: "git diff --name-only ${base} HEAD || true", returnStdout: true)
        .trim()
        .split('\n')
        .findAll { it?.trim() } as List

    def targetAdapters = [
        'user-service'         : 'user-adapters',
        'product-service'      : 'product-adapters',
        'commerce-service'     : 'commerce-adapters',
        'community-service'    : 'community-adapters',
        'reward-service'       : 'reward-adapters',
        'fulfillment-service'  : 'fulfillment-adapters',
        'file-service'         : 'file-adapters',
        'notification-service' : 'notification-adapters',
    ]

    def serviceTouched = { svc, adp ->
        def prefixes = [
            "${svc}/${adp}/",
            "${svc}/${svc.replace('-service','')}-application/",
            "${svc}/${svc.replace('-service','')}-domain/"
        ]
        changed.any { p -> prefixes.any { pre -> p.startsWith(pre) } }
    }

    // 전역 영향 파일/디렉토리 — 변경 시 8개 서비스 전체 재트리거.
    // 정확 매칭: 루트 build/설정 파일, Gradle wrapper 런처 (서비스 하위 동명 파일은 serviceTouched 가 별도 감지).
    // Prefix 매칭: common 모듈, jenkins 헬퍼 디렉토리, gradle wrapper, 공용 scripts
    //             — 전부 모든 서비스 빌드가 직접 의존하는 경로.
    def globalExactPaths = [
        'build.gradle.kts',
        'settings.gradle.kts',
        'gradle.properties',
        'Jenkinsfile',
        'gradlew',
        'gradlew.bat',
    ]
    def globalPrefixes = [
        'common/',
        'jenkins/',
        'gradle/',
        'scripts/',
    ]

    def commonOrRootChanged = changed.any { p ->
        globalExactPaths.contains(p) || globalPrefixes.any { pre -> p.startsWith(pre) }
    }

    def candidates = commonOrRootChanged
        ? (targetAdapters.keySet() as List)
        : (targetAdapters.findAll { svc, adp -> serviceTouched(svc, adp) }.keySet() as List)
    candidates = candidates.sort()

    // auto 실행은 dispatcher 역할: 감지된 서비스 각각을 SERVICE=<svc> 로 재트리거 후 종료.
    // disableConcurrentBuilds() 가 동일 잡의 동시 실행을 막아 자식 빌드 N건이 큐에서 직렬 소비됨.
    // 재트리거된 빌드는 params.SERVICE != 'auto' 분기로 진입하여 단일 서비스 배포 흐름을 그대로 수행.
    // NOTE: dispatcher 빌드는 env.DISPATCHER_MODE='true' 가 설정된 채 이후 stage 를 모두 스킵한다.
    //       Skip If Same Commit / Deploy Pipeline 외 top-level stage 를 추가하는 경우 반드시
    //       when { not { environment name: 'DISPATCHER_MODE', value: 'true' } } 가드를 추가할 것.
    if (parentScript.params.SERVICE == 'auto') {
        // 긴급 revert 우회(SKIP_FLYWAY_IMMUTABILITY_CHECK)는 단일 서비스 지정 전제 — auto 광역 우회 차단.
        if (parentScript.params.SKIP_FLYWAY_IMMUTABILITY_CHECK) {
            error "SKIP_FLYWAY_IMMUTABILITY_CHECK=true 는 SERVICE 를 구체 서비스로 지정한 경우에만 허용된다. (긴급 revert PR 한정 정책)"
        }

        env.DISPATCHER_MODE = 'true'
        env.CANDIDATE_SERVICES = candidates.join(',')

        echo "[DISPATCHER] changed files       = ${changed.size()}"
        echo "[DISPATCHER] common/root changed = ${commonOrRootChanged}"
        echo "[DISPATCHER] candidates          = ${candidates}"

        if (!candidates) {
            parentScript.currentBuild.description = '[DISPATCHER] no deploy target'
            echo '[DISPATCHER] no candidates detected; exiting without deploy'
            sendDispatcherSummaryNotification(parentScript, [], commonOrRootChanged)
            return
        }

        candidates.each { svc ->
            echo "[DISPATCHER] re-triggering ${env.JOB_NAME} with SERVICE=${svc}"
            parentScript.build(
                job: env.JOB_NAME,
                wait: false,
                propagate: false,
                parameters: [
                    parentScript.string(name: 'SERVICE', value: svc),
                    parentScript.booleanParam(name: 'SKIP_FLYWAY_IMMUTABILITY_CHECK', value: false),
                    parentScript.string(name: 'SKIP_REASON', value: parentScript.params.SKIP_REASON ?: ''),
                    parentScript.string(name: 'DISPATCHER_BUILD_NUMBER', value: env.BUILD_NUMBER)
                ]
            )
        }

        parentScript.currentBuild.description = "[DISPATCHER] re-triggered ${candidates.size()}: ${candidates.join(',')}"
        sendDispatcherSummaryNotification(parentScript, candidates, commonOrRootChanged)
        triggerDispatcherMonitor(parentScript, candidates)
        return
    }

    def targetService = parentScript.params.SERVICE

    if (!targetAdapters.containsKey(targetService)) {
        error "Unknown SERVICE '${targetService}'. Allowed: ${targetAdapters.keySet()}"
    }

    env.SERVICE_NAME = targetService
    env.SERVICE_DIRECTORY = "${targetService}/${targetAdapters[targetService]}"
    env.CANDIDATE_SERVICES = candidates.join(',')

    sh "test -d '${env.SERVICE_DIRECTORY}'"

    // #4828: 자식 description 기록을 Select Target Service stage 로 앞당김.
    // Notify Start 시점은 Environment Setup 다음이라 30초 monitor 첫 폴링과 race 가능.
    // 여기서 prefix 를 즉시 기록하여 첫 폴링 시점 매칭 확률을 높인다 (handleBuildSuccess/Failure 갱신 패턴은 유지).
    parentScript.currentBuild.description = composeChildDescription(parentScript, "SERVICE=${targetService}")

    echo "Changed fileDomain count       = ${changed.size()}"
    echo "COMMON/ROOT changed?      = ${commonOrRootChanged}"
}

// dispatcher 빌드(SERVICE=auto) 가 자식 N건을 큐잉한 직후 1회 발송하는 요약 Slack 알림.
// disableConcurrentBuilds 와 wait:false 로 인해 자식 결과 집계는 본 함수 책임 외 (#4811 후속 분리).
// SLACK_NOTIFICATION_CHANNEL credential 미해결 시 fail-open (echo 후 return).
def sendDispatcherSummaryNotification(parentScript, candidates, boolean commonOrRootChanged) {
    def envInfo = resolveEnvPrefixFromJobName(parentScript)
    withCredentials([
        string(credentialsId: "${envInfo.envPrefix}_SLACK_NOTIFICATION_CHANNEL", variable: 'SLACK_NOTIFICATION_CHANNEL')
    ]) {
        def ch = SLACK_NOTIFICATION_CHANNEL?.trim()
        if (!ch) {
            echo '[DISPATCHER] SLACK_NOTIFICATION_CHANNEL unresolved; skipping dispatcher summary notification'
            return
        }
        def branchLabel = sanitizeSlackLine(resolveBranchLabel(parentScript))
        def commitAuthor  = sanitizeSlackLine(sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim())
        def commitMessage = sanitizeCommitSubject(sh(script: 'git log -1 --pretty=%B',  returnStdout: true).trim())
        def cronPrefix = env.CRON_TRIGGERED == 'true' ? '[QA-CRON] ' : ''
        def candidateList = candidates ?: []
        def servicesText = candidateList ? candidateList.join(', ') : '(none)'
        slackSend(
            channel: ch,
            color:   "${env.SLACK_GENERAL_COLOR}",
            message: "${cronPrefix}[DISPATCHER][${envInfo.envUpper}] re-triggered ${candidateList.size()} services\n"
                + "SERVICES: ${servicesText}\n"
                + "COMMON_OR_ROOT_CHANGED: ${commonOrRootChanged}\n"
                + "JOB_NAME: ${env.JOB_NAME}\n"
                + "BUILD_NUMBER: ${env.BUILD_NUMBER}\n"
                + "BUILD_URL: ${env.BUILD_URL}\n"
                + "BRANCH: ${branchLabel}\n"
                + "AUTHOR: ${commitAuthor}\n"
                + "COMMIT: ${env.GIT_COMMIT}\n"
                + "MESSAGE: ${commitMessage}"
        )
    }
}

// Notify Start stage 본문 — withCredentials / branchLabel / slackSend 전체.
// #4825: description 설정은 credential 의존 없음. credential 미해결 케이스에서도 dispatcher 추적 prefix 보존하도록 withCredentials 외부로 이동.
// #4828: description 기록은 selectTargetService 로 앞당김 (monitor race 완화). 본 함수에서는 중복 기록하지 않음.
def sendStartNotification(parentScript) {
    withCredentials([
        string(credentialsId: "${env.ENV_PREFIX}_SLACK_NOTIFICATION_CHANNEL", variable: 'SLACK_NOTIFICATION_CHANNEL')
    ]) {
        env.SLACK_NOTIFICATION_CHANNEL = SLACK_NOTIFICATION_CHANNEL
        def branchLabel = sanitizeSlackLine(resolveBranchLabel(parentScript))
        env.BRANCH_LABEL = branchLabel
        def commitAuthor  = sanitizeSlackLine(sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim())
        def commitMessage = sanitizeCommitSubject(sh(script: 'git log -1 --pretty=%B',  returnStdout: true).trim())
        def cronPrefix = env.CRON_TRIGGERED == 'true' ? '[QA-CRON] ' : ''
        slackSend(
            channel: "${env.SLACK_NOTIFICATION_CHANNEL}",
            color:   "${env.SLACK_GENERAL_COLOR}",
            message: "${cronPrefix}[${env.ENVIRONMENT_UPPER}] Start a build\n"
                + "SERVICE: ${env.SERVICE_NAME}\n"
                + "PROJECT_NAME: ${env.PROJECT_NAME}\n"
                + "VERSION: ${env.PROJECT_VERSION}\n"
                + "JOB_NAME: ${env.JOB_NAME}\n"
                + "BUILD_NUMBER: ${env.BUILD_NUMBER}\n"
                + "BUILD_URL: ${env.BUILD_URL}\n"
                + "BRANCH: ${branchLabel}\n"
                + "AUTHOR: ${commitAuthor}\n"
                + "COMMIT: ${env.GIT_COMMIT}\n"
                + "MESSAGE: ${commitMessage}"
        )
    }
}

// DISPATCHER_BUILD_NUMBER 화이트리스트 검증. 자동 dispatcher 경로(env.BUILD_NUMBER)는 정수만 전달되나,
// 수동/REST 트리거에서 임의 문자열이 description prefix 에 보간되는 표면 차단.
// #4810 SKIP_REASON 정규식 검증 선례와 일관. 1~9자리 숫자만 허용 (Jenkins 빌드 번호 상한 999_999_999).
// 빈값/null 은 통과 (수동/cron 트리거 정상 경로).
@NonCPS
def validateDispatcherBuildNumber(String num) {
    if (!num) return
    if (!(num ==~ /^[0-9]{1,9}$/)) {
        error "DISPATCHER_BUILD_NUMBER 형식 위반: '${num}'. 1~9자리 숫자만 허용 (자동 dispatcher 가 env.BUILD_NUMBER 를 전달)."
    }
}

// 자식 빌드 description 에 dispatcher BUILD_NUMBER prefix 합성. 수동/cron 트리거 시 prefix 없이 suffix 그대로 반환.
// #4816: build(wait: false) 가 자식 BUILD_NUMBER 미반환 → 자식이 자기 description 에 부모 역기록으로 양방향 추적.
// findLastDeployedCommit 의 SERVICE=...COMMIT=... substring 매칭과 정합 — prefix 추가는 회귀 없음.
// #4824: 동일 파일 findLastDeployedCommit/isCronTriggered 와 일관성. 순수 문자열 보간만 수행하므로 CPS 직렬화 우회 명시.
// #4826: prefix 보간 직전 형식 검증으로 임의 문자열 표면 차단.
@NonCPS
def composeChildDescription(parentScript, String coreSuffix) {
    def num = parentScript.params.DISPATCHER_BUILD_NUMBER?.trim()
    validateDispatcherBuildNumber(num)
    if (num) {
        return "dispatcher: #${num} ${coreSuffix}"
    }
    return coreSuffix
}

// post.success 본문 — description 기록 + Slack 성공 알림.
// #4822: post.success Slack 보간 변수(SERVICE_NAME, ECS_SERVICE_NAME, IMAGE_URI)를 sanitizeSlackLine 으로 정화하여
// Notify Start (#4817) 와 sink 일관성 확보. params.SERVICE 에서 흘러 온 값의 표면 잔존 차단.
def handleBuildSuccess(parentScript) {
    if (env.DISPATCHER_MODE == 'true') {
        echo "[DISPATCHER] skipping success notification/description override"
        return
    }
    if (env.SERVICE_NAME?.trim() && env.GIT_COMMIT?.trim()) {
        parentScript.currentBuild.description = composeChildDescription(parentScript, "SERVICE=${env.SERVICE_NAME} COMMIT=${env.GIT_COMMIT}")
    }
    def ch = env.SLACK_NOTIFICATION_CHANNEL?.trim()
    if (!ch) {
        echo "SLACK_NOTIFICATION_CHANNEL unresolved; skipping Slack success notification"
        return
    }
    def cronPrefix = env.CRON_TRIGGERED == 'true' ? '[QA-CRON] ' : ''
    def serviceName    = sanitizeSlackLine(env.SERVICE_NAME)
    def ecsServiceName = sanitizeSlackLine(env.ECS_SERVICE_NAME)
    def imageUri       = sanitizeSlackLine(env.IMAGE_URI ?: 'N/A')
    slackSend(
        channel: ch,
        color:   "${env.SLACK_SUCCESS_COLOR}",
        message: "${cronPrefix}Build Success!!\n"
            + "SERVICE: ${serviceName}\n"
            + "ECS_SERVICE: ${ecsServiceName}\n"
            + "IMAGE: ${imageUri}\n"
            + "BUILD_NUMBER: ${env.BUILD_NUMBER}"
    )
}

// post.failure 본문 — Slack 실패 알림 ([QA-CRON] prefix 조건부).
// #4822: post.failure 도 동일 정화 적용 (BUILD_NUMBER 만 보간되어 위장 표면 작으나 sink 일관성).
def handleBuildFailure(parentScript) {
    if (env.DISPATCHER_MODE == 'true') {
        echo "[DISPATCHER] skipping failure notification"
        return
    }
    if (env.SERVICE_NAME?.trim()) {
        def failedSuffix = env.GIT_COMMIT?.trim() ? "SERVICE=${env.SERVICE_NAME} COMMIT=${env.GIT_COMMIT} FAILED" : "SERVICE=${env.SERVICE_NAME} FAILED"
        parentScript.currentBuild.description = composeChildDescription(parentScript, failedSuffix)
    }
    def ch = env.SLACK_NOTIFICATION_CHANNEL?.trim()
    if (!ch) {
        echo "SLACK_NOTIFICATION_CHANNEL unresolved; skipping Slack failure notification"
        return
    }
    def cronPrefix = env.CRON_TRIGGERED == 'true' ? '[QA-CRON] ' : ''
    def buildNumber = sanitizeSlackLine(env.BUILD_NUMBER)
    slackSend(
        channel: ch,
        color:   "${env.SLACK_FAIL_COLOR}",
        message: "${cronPrefix}Build Failed...\nBUILD_NUMBER: ${buildNumber}"
    )
}

// TimerTrigger 또는 ParameterizedTimerTrigger cause 존재 여부로 cron 발화 빌드 판정.
@NonCPS
def isCronTriggered(build) {
    def causes = build.getBuildCauses() ?: []
    return causes.any { c ->
        def cls = (c._class ?: '').toString()
        return cls.contains('TimerTrigger') || cls.contains('ParameterizedTimerTrigger')
    }
}

// ===== #4815 dispatcher monitor 지원 helper =====

// dispatcher 분기에서 monitor job 트리거. fail-open: 미존재/실패 시 echo 만 (monitor 미존재해도 dispatcher 흐름 유지).
// monitor job 명: dispatcher project + '-dispatcher-monitor'. 동일 branch path 유지.
// 예: dispatcher = shop-qa/main → monitor = shop-qa-dispatcher-monitor/main
def triggerDispatcherMonitor(parentScript, candidates) {
    if (!candidates) {
        echo '[DISPATCHER] no candidates; skipping monitor trigger'
        return
    }
    def jobName = env.JOB_NAME ?: ''
    def parts = jobName.split('/') as List
    if (parts.size() < 1 || !parts[0]) {
        echo "[DISPATCHER] JOB_NAME='${jobName}' invalid; skipping monitor trigger"
        return
    }
    def project = parts[0]
    def branchPath = parts.size() > 1 ? parts.drop(1).join('/') : ''
    def monitorProject = "${project}-dispatcher-monitor"
    def monitorJobName = branchPath ? "${monitorProject}/${branchPath}" : monitorProject
    def envInfo = resolveEnvPrefixFromJobName(parentScript)
    try {
        parentScript.build(
                job: monitorJobName,
                wait: false,
                propagate: false,
                parameters: [
                    parentScript.string(name: 'DISPATCHER_BUILD_NUMBER', value: env.BUILD_NUMBER),
                    parentScript.string(name: 'DISPATCHER_BUILD_URL', value: env.BUILD_URL),
                    parentScript.string(name: 'PARENT_JOB_NAME', value: env.JOB_NAME),
                    parentScript.string(name: 'SERVICES', value: candidates.join(',')),
                    parentScript.string(name: 'ENV_PREFIX', value: envInfo.envPrefix)
                ]
        )
        echo "[DISPATCHER] monitor job triggered: ${monitorJobName}"
    } catch (Exception e) {
        echo "[DISPATCHER] monitor trigger failed (fail-open): ${e.message}. Job '${monitorJobName}' may not exist yet — operations task pending."
    }
}

// dispatcher description 끝에 ' MONITOR=<url>' append (REST POST submitDescription).
// 기존 description 보존 + monitor URL 역기록. 실패 시 fail-open (echo 만, monitor 진행 계속).
def appendDispatcherMonitorLink(parentScript) {
    def envPrefix = parentScript.params.ENV_PREFIX
    def parentJobName = parentScript.params.PARENT_JOB_NAME
    def dispatcherNum = parentScript.params.DISPATCHER_BUILD_NUMBER
    def monitorUrl = env.BUILD_URL ?: ''
    if (!monitorUrl) {
        echo '[MONITOR] BUILD_URL unresolved; skipping dispatcher reverse link'
        return
    }
    try {
        withCredentials([
            usernamePassword(credentialsId: "${envPrefix}_JENKINS_API", usernameVariable: 'J_USER', passwordVariable: 'J_TOKEN')
        ]) {
            def jobPath = parentJobName.split('/').collect { "job/${it}" }.join('/')
            def baseUrl = (env.JENKINS_URL ?: '').replaceAll(/\/$/, '')
            def descUrl = "${baseUrl}/${jobPath}/${dispatcherNum}/submitDescription"
            def getUrl = "${baseUrl}/${jobPath}/${dispatcherNum}/api/json?tree=description"
            // sh 보간 인젝션 방지 — URL/description 을 withEnv 로 위임 후 single-quote sh 본문에서 $VAR 참조.
            // #4831: -u "$J_USER:$J_TOKEN" 은 ps -ef / /proc/<pid>/cmdline 로 동시 빌드 프로세스에서 관찰 가능.
            //        --netrc-file 사용 시 인증 정보가 argv 가 아닌 파일에서 로드 → 프로세스 목록 노출 차단.
            //        netrc 파일은 mktemp + chmod 600 으로 격리, trap 으로 sh 종료 시 즉시 삭제.
            String raw = ''
            parentScript.withEnv(["GET_URL=${getUrl}"]) {
                raw = sh(
                    script: '''
                        NETRC="$(mktemp)"
                        chmod 600 "$NETRC"
                        trap 'rm -f "$NETRC"' EXIT
                        HOST=$(printf '%s' "$GET_URL" | sed 's|^[a-z]*://||;s|/.*$||;s|:.*$||')
                        {
                            printf 'machine %s\\nlogin %s\\npassword %s\\n' "$HOST" "$J_USER" "$J_TOKEN"
                        } > "$NETRC"
                        curl -sf --netrc-file "$NETRC" "$GET_URL"
                    ''',
                    returnStdout: true
                ).trim()
            }
            def parsed = parentScript.readJSON(text: raw)
            def existing = (parsed.description ?: '').toString()
            if (existing.contains('MONITOR=')) {
                echo "[MONITOR] dispatcher #${dispatcherNum} already has MONITOR link; skipping"
                return
            }
            def newDesc = "${existing} MONITOR=${monitorUrl}".trim()
            parentScript.withEnv(["NEW_DESC=${newDesc}", "DESC_URL=${descUrl}"]) {
                sh '''
                    NETRC="$(mktemp)"
                    chmod 600 "$NETRC"
                    trap 'rm -f "$NETRC"' EXIT
                    HOST=$(printf '%s' "$DESC_URL" | sed 's|^[a-z]*://||;s|/.*$||;s|:.*$||')
                    {
                        printf 'machine %s\\nlogin %s\\npassword %s\\n' "$HOST" "$J_USER" "$J_TOKEN"
                    } > "$NETRC"
                    curl -sf --netrc-file "$NETRC" -X POST \
                         --data-urlencode "description=$NEW_DESC" \
                         "$DESC_URL" -o /dev/null
                '''
            }
            echo "[MONITOR] dispatcher #${dispatcherNum} description updated with monitor link"
        }
    } catch (Exception e) {
        echo "[MONITOR] dispatcher reverse link failed (fail-open): ${e.message}"
    }
}

// 자식 빌드 결과 폴링. 30초 간격, 60분 타임아웃 (env.POLL_INTERVAL_SECONDS / POLL_TIMEOUT_MINUTES).
// description prefix 'dispatcher: #N SERVICE=<svc>' 매칭으로 자식 식별 (#4816 join key).
// 모든 SERVICE 종결 시 조기 break. 결과는 env.MONITOR_RESULTS_JSON 직렬화 저장.
def pollDispatcherChildren(parentScript) {
    def envPrefix = parentScript.params.ENV_PREFIX
    def parentJobName = parentScript.params.PARENT_JOB_NAME
    def dispatcherNum = parentScript.params.DISPATCHER_BUILD_NUMBER
    def services = (parentScript.params.SERVICES ?: '').split(',').collect { it.trim() }.findAll { it }
    int intervalSec = (env.POLL_INTERVAL_SECONDS ?: '30') as Integer
    int timeoutMin = (env.POLL_TIMEOUT_MINUTES ?: '60') as Integer
    int maxIterations = ((timeoutMin * 60) / intervalSec) as int

    def results = [:]
    services.each { svc -> results[svc] = [result: null, url: null, number: null, duration: null] }

    long startTime = System.currentTimeMillis()
    boolean timedOut = false

    // credential 미존재 시 CredentialNotFoundException → fail-open: 모든 서비스 PENDING + timedOut=true.
    // monitor SUCCESS 유지 정책 일관성 (Fix 4 — code MEDIUM-1).
    try {
        withCredentials([
            usernamePassword(credentialsId: "${envPrefix}_JENKINS_API", usernameVariable: 'J_USER', passwordVariable: 'J_TOKEN')
        ]) {
            def jobPath = parentJobName.split('/').collect { "job/${it}" }.join('/')
            def baseUrl = (env.JENKINS_URL ?: '').replaceAll(/\/$/, '')
            def listUrl = "${baseUrl}/${jobPath}/api/json?tree=builds[number,description,result,timestamp,duration,url]{0,50}"

            // sh 보간 인젝션 방지 — listUrl 을 withEnv 로 위임 후 single-quote sh 본문에서 $LIST_URL 참조.
            // withEnv 블록은 폴링 루프 전체를 감싸 매 반복마다 환경변수 재설정 비용을 회피.
            // #4831: -u argv 노출 차단 — netrc 파일 사용 (appendDispatcherMonitorLink 와 동일 패턴).
            parentScript.withEnv(["LIST_URL=${listUrl}"]) {
                for (int i = 0; i < maxIterations; i++) {
                    def remainingServices = services.findAll { results[it].result == null }
                    if (!remainingServices) {
                        echo "[MONITOR] all ${services.size()} services completed at iteration ${i}"
                        break
                    }
                    echo "[MONITOR] poll ${i + 1}/${maxIterations}, pending=${remainingServices}"
                    try {
                        def raw = sh(
                                script: '''
                                    NETRC="$(mktemp)"
                                    chmod 600 "$NETRC"
                                    trap 'rm -f "$NETRC"' EXIT
                                    HOST=$(printf '%s' "$LIST_URL" | sed 's|^[a-z]*://||;s|/.*$||;s|:.*$||')
                                    {
                                        printf 'machine %s\\nlogin %s\\npassword %s\\n' "$HOST" "$J_USER" "$J_TOKEN"
                                    } > "$NETRC"
                                    curl -sf --netrc-file "$NETRC" "$LIST_URL"
                                ''',
                                returnStdout: true
                        ).trim()
                        def json = parentScript.readJSON(text: raw)
                        def builds = json.builds ?: []
                        builds.each { b ->
                            def desc = (b.description ?: '').toString()
                            def matcher = (desc =~ /dispatcher: #${dispatcherNum} SERVICE=(\S+)/)
                            if (matcher.find()) {
                                def svc = matcher.group(1)
                                if (results.containsKey(svc) && results[svc].result == null && b.result != null) {
                                    results[svc].result = b.result
                                    results[svc].url = b.url
                                    results[svc].number = b.number
                                    results[svc].duration = b.duration
                                    echo "[MONITOR] ${svc} completed: ${b.result} #${b.number}"
                                }
                            }
                            matcher = null
                        }
                    } catch (Exception e) {
                        echo "[MONITOR] poll iteration ${i + 1} failed (fail-open): ${e.message}"
                    }
                    if (i < maxIterations - 1) {
                        sleep time: intervalSec, unit: 'SECONDS'
                    }
                }
            }
            if (services.any { results[it].result == null }) {
                timedOut = true
                echo "[MONITOR] timeout after ${timeoutMin}min, pending services not completed"
            }
        }
    } catch (Exception e) {
        echo "[MONITOR] credential/poll failure (fail-open): ${e.message}. All services marked PENDING."
        timedOut = true
    }

    long elapsedMs = System.currentTimeMillis() - startTime
    env.MONITOR_RESULTS_JSON = parentScript.writeJSON(
            returnText: true,
            json: [results: results, timedOut: timedOut, elapsedMs: elapsedMs, services: services]
    )
}

// 폴링 결과 집계 + Slack 통합 알림.
// fail-open: timeout 시 Slack 경고 발송 후 monitor SUCCESS 유지 (미종결은 운영자 직접 확인).
def sendDispatcherChildrenSummary(parentScript) {
    def envPrefix = parentScript.params.ENV_PREFIX
    def dispatcherNum = parentScript.params.DISPATCHER_BUILD_NUMBER
    def dispatcherUrl = parentScript.params.DISPATCHER_BUILD_URL
    // monitor JOB_NAME 은 'shop-qa-dispatcher-monitor/...' 형식으로 dispatcher 의 '-qa/-stage/-prod' 접미사 패턴과 다름.
    // resolveEnvPrefixFromJobName 호출 시 정규식 미매치로 error throw → Send Summary stage 영구 실패 (Fix 1 — code CRITICAL).
    // params.ENV_PREFIX(MARKETNOTE_QA/MARKETNOTE_STAGE/MARKETNOTE_PROD)에서 'MARKETNOTE_' 접두사만 제거하여 envUpper 도출.
    def envUpper = (envPrefix ?: '').replaceFirst(/^MARKETNOTE_/, '')

    if (!env.MONITOR_RESULTS_JSON) {
        echo '[MONITOR] no results to summarize'
        return
    }
    def parsed = parentScript.readJSON(text: env.MONITOR_RESULTS_JSON)
    def results = parsed.results
    boolean timedOut = parsed.timedOut as boolean
    long elapsedMs = (parsed.elapsedMs ?: 0L) as long
    def services = parsed.services ?: []

    int successCount = services.count { results[it]?.result == 'SUCCESS' }
    def failed = services.findAll { results[it]?.result == 'FAILURE' || results[it]?.result == 'ABORTED' || results[it]?.result == 'UNSTABLE' }
    def pending = services.findAll { results[it]?.result == null }

    int elapsedMin = (elapsedMs / 60000) as int
    int elapsedSec = ((elapsedMs % 60000) / 1000) as int

    def color = (failed || pending) ? env.SLACK_FAIL_COLOR : env.SLACK_SUCCESS_COLOR
    def header = (failed || pending) ? '[DISPATCHER-MONITOR] partial/failed' : '[DISPATCHER-MONITOR] all success'

    def lines = []
    lines << "${header} dispatcher: #${dispatcherNum} (${envUpper})"
    lines << "DISPATCHER: ${dispatcherUrl}"
    lines << "TOTAL: ${services.size()}, SUCCESS: ${successCount}, FAILED: ${failed.size()}, PENDING(timeout): ${pending.size()}"
    lines << "ELAPSED: ${elapsedMin}m ${elapsedSec}s"
    if (failed) {
        lines << 'FAILED_SERVICES:'
        failed.each { svc -> lines << "  - ${svc} ${results[svc].result}: ${results[svc].url ?: '(no url)'}" }
    }
    if (pending) {
        lines << 'PENDING_SERVICES (60min timeout):'
        pending.each { svc -> lines << "  - ${svc}" }
    }
    // monitor job 자체는 항상 SUCCESS — 컬러/헤더는 자식 결과 반영하지만 빌드 결과는 fail-open (Fix 5 — code MEDIUM-4).
    // 운영자 혼란(Slack RED + monitor SUCCESS) 방지용 명시적 안내.
    if (timedOut || failed || pending) {
        lines << '[NOTE] monitor job 자체는 SUCCESS — 자식 빌드 실패/미종결은 위 BUILD_URL 직접 확인'
    }

    withCredentials([
        string(credentialsId: "${envPrefix}_SLACK_NOTIFICATION_CHANNEL", variable: 'SLACK_NOTIFICATION_CHANNEL')
    ]) {
        def ch = SLACK_NOTIFICATION_CHANNEL?.trim()
        if (!ch) {
            echo '[MONITOR] SLACK_NOTIFICATION_CHANNEL unresolved; printing summary only'
            echo lines.join('\n')
            return
        }
        slackSend(channel: ch, color: color, message: lines.join('\n'))
    }
}

// monitor 자체 실패 (post.failure) Slack 경고. 시스템 오류 알림용.
def notifyMonitorFailure(parentScript) {
    def envPrefix = (parentScript.params.ENV_PREFIX ?: '').toString()
    if (!envPrefix) {
        echo '[MONITOR] ENV_PREFIX unset; skipping failure notification'
        return
    }
    try {
        withCredentials([
            string(credentialsId: "${envPrefix}_SLACK_NOTIFICATION_CHANNEL", variable: 'SLACK_NOTIFICATION_CHANNEL')
        ]) {
            def ch = SLACK_NOTIFICATION_CHANNEL?.trim()
            if (!ch) return
            slackSend(
                    channel: ch,
                    color: env.SLACK_FAIL_COLOR,
                    message: "[DISPATCHER-MONITOR][SYSTEM-ERROR] dispatcher: #${parentScript.params.DISPATCHER_BUILD_NUMBER}\nMONITOR_BUILD: ${env.BUILD_URL}"
            )
        }
    } catch (Exception e) {
        echo "[MONITOR] notifyMonitorFailure failed: ${e.message}"
    }
}

// 이전 성공 빌드 description에서 동일 SERVICE의 마지막 배포 COMMIT 조회.
// description 포맷: "SERVICE=<name> COMMIT=<sha>" (post.success에서 기록)
// 순회 한도 50건 — 8 서비스 × 평균 일 3회 성공 = 약 2일 커버.
// 미발견 시 null 반환 → fail-open(재배포). 장기간 미배포 서비스 드리프트 방지.
@NonCPS
def findLastDeployedCommit(build, String serviceName) {
    def prev = build.previousSuccessfulBuild
    int scanned = 0
    while (prev != null && scanned < 50) {
        def desc = prev.description ?: ''
        def m = desc =~ /SERVICE=(\S+) COMMIT=(\S+)/
        if (m.find() && m.group(1) == serviceName) {
            return m.group(2)
        }
        prev = prev.previousSuccessfulBuild
        scanned++
    }
    return null
}

return this
