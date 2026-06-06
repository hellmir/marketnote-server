import com.cloudbees.groovy.cps.NonCPS

@NonCPS
def resolveServiceMapping(svc, credentials) {
    def mappings = [
        'user-service'       : [ecr: credentials.USER_SERVICE_ECR_REPOSITORY,        ecs: credentials.USER_SERVICE_ECS_SERVICE_NAME,        tg: credentials.USER_SERVICE_TARGET_GROUP_ARN,        origin: credentials.USER_SERVICE_SERVER_ORIGIN,        dbUrl: credentials.USER_SERVICE_DB_URL,        dbPw: credentials.USER_SERVICE_DB_PASSWORD],
        'product-service'    : [ecr: credentials.PRODUCT_SERVICE_ECR_REPOSITORY,     ecs: credentials.PRODUCT_SERVICE_ECS_SERVICE_NAME,     tg: credentials.PRODUCT_SERVICE_TARGET_GROUP_ARN,     origin: credentials.PRODUCT_SERVICE_SERVER_ORIGIN,     dbUrl: credentials.PRODUCT_SERVICE_DB_URL,     dbPw: credentials.PRODUCT_SERVICE_DB_PASSWORD],
        'commerce-service'   : [ecr: credentials.COMMERCE_SERVICE_ECR_REPOSITORY,    ecs: credentials.COMMERCE_SERVICE_ECS_SERVICE_NAME,    tg: credentials.COMMERCE_SERVICE_TARGET_GROUP_ARN,    origin: credentials.COMMERCE_SERVICE_SERVER_ORIGIN,    dbUrl: credentials.COMMERCE_SERVICE_DB_URL,    dbPw: credentials.COMMERCE_SERVICE_DB_PASSWORD],
        'community-service'  : [ecr: credentials.COMMUNITY_SERVICE_ECR_REPOSITORY,   ecs: credentials.COMMUNITY_SERVICE_ECS_SERVICE_NAME,   tg: credentials.COMMUNITY_SERVICE_TARGET_GROUP_ARN,   origin: credentials.COMMUNITY_SERVICE_SERVER_ORIGIN,   dbUrl: credentials.COMMUNITY_SERVICE_DB_URL,   dbPw: credentials.COMMUNITY_SERVICE_DB_PASSWORD],
        'reward-service'     : [ecr: credentials.REWARD_SERVICE_ECR_REPOSITORY,      ecs: credentials.REWARD_SERVICE_ECS_SERVICE_NAME,      tg: credentials.REWARD_SERVICE_TARGET_GROUP_ARN,      origin: credentials.REWARD_SERVICE_SERVER_ORIGIN,      dbUrl: credentials.REWARD_SERVICE_DB_URL,      dbPw: credentials.REWARD_SERVICE_DB_PASSWORD],
        'fulfillment-service': [ecr: credentials.FULFILLMENT_SERVICE_ECR_REPOSITORY, ecs: credentials.FULFILLMENT_SERVICE_ECS_SERVICE_NAME, tg: credentials.FULFILLMENT_SERVICE_TARGET_GROUP_ARN, origin: credentials.FULFILLMENT_SERVICE_SERVER_ORIGIN, dbUrl: credentials.FULFILLMENT_SERVICE_DB_URL, dbPw: credentials.FULFILLMENT_SERVICE_DB_PASSWORD],
        'file-service'           : [ecr: credentials.FILE_SERVICE_ECR_REPOSITORY,            ecs: credentials.FILE_SERVICE_ECS_SERVICE_NAME,            tg: credentials.FILE_SERVICE_TARGET_GROUP_ARN,            origin: credentials.FILE_SERVICE_SERVER_ORIGIN,            dbUrl: credentials.FILE_SERVICE_DB_URL,            dbPw: credentials.FILE_SERVICE_DB_PASSWORD],
        'notification-service'   : [ecr: credentials.NOTIFICATION_SERVICE_ECR_REPOSITORY,    ecs: credentials.NOTIFICATION_SERVICE_ECS_SERVICE_NAME,    tg: credentials.NOTIFICATION_SERVICE_TARGET_GROUP_ARN,    origin: credentials.NOTIFICATION_SERVICE_SERVER_ORIGIN,    dbUrl: credentials.NOTIFICATION_SERVICE_DB_URL,    dbPw: credentials.NOTIFICATION_SERVICE_DB_PASSWORD],
    ]
    mappings[svc]
}

def resolveServiceMappings(parentScript) {
    withCredentials([
        string(credentialsId: 'MARKETNOTE_QA_USER_SERVICE_ECR_REPOSITORY',        variable: 'USER_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_PRODUCT_SERVICE_ECR_REPOSITORY',     variable: 'PRODUCT_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_COMMERCE_SERVICE_ECR_REPOSITORY',    variable: 'COMMERCE_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_COMMUNITY_SERVICE_ECR_REPOSITORY',   variable: 'COMMUNITY_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_REWARD_SERVICE_ECR_REPOSITORY',      variable: 'REWARD_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_FULFILLMENT_SERVICE_ECR_REPOSITORY', variable: 'FULFILLMENT_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_FILE_SERVICE_ECR_REPOSITORY',            variable: 'FILE_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_NOTIFICATION_SERVICE_ECR_REPOSITORY',    variable: 'NOTIFICATION_SERVICE_ECR_REPOSITORY'),
        string(credentialsId: 'MARKETNOTE_QA_USER_SERVICE_ECS_SERVICE_NAME',          variable: 'USER_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_PRODUCT_SERVICE_ECS_SERVICE_NAME',       variable: 'PRODUCT_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_COMMERCE_SERVICE_ECS_SERVICE_NAME',      variable: 'COMMERCE_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_COMMUNITY_SERVICE_ECS_SERVICE_NAME',     variable: 'COMMUNITY_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_REWARD_SERVICE_ECS_SERVICE_NAME',        variable: 'REWARD_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_FULFILLMENT_SERVICE_ECS_SERVICE_NAME',   variable: 'FULFILLMENT_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_FILE_SERVICE_ECS_SERVICE_NAME',              variable: 'FILE_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_NOTIFICATION_SERVICE_ECS_SERVICE_NAME',  variable: 'NOTIFICATION_SERVICE_ECS_SERVICE_NAME'),
        string(credentialsId: 'MARKETNOTE_QA_USER_SERVICE_TARGET_GROUP_ARN',          variable: 'USER_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_PRODUCT_SERVICE_TARGET_GROUP_ARN',       variable: 'PRODUCT_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_COMMERCE_SERVICE_TARGET_GROUP_ARN',      variable: 'COMMERCE_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_COMMUNITY_SERVICE_TARGET_GROUP_ARN',     variable: 'COMMUNITY_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_REWARD_SERVICE_TARGET_GROUP_ARN',        variable: 'REWARD_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_FULFILLMENT_SERVICE_TARGET_GROUP_ARN',   variable: 'FULFILLMENT_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_FILE_SERVICE_TARGET_GROUP_ARN',              variable: 'FILE_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_NOTIFICATION_SERVICE_TARGET_GROUP_ARN',  variable: 'NOTIFICATION_SERVICE_TARGET_GROUP_ARN'),
        string(credentialsId: 'MARKETNOTE_QA_USER_SERVICE_SERVER_ORIGIN',         variable: 'USER_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_PRODUCT_SERVICE_SERVER_ORIGIN',      variable: 'PRODUCT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_COMMERCE_SERVICE_SERVER_ORIGIN',     variable: 'COMMERCE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_COMMUNITY_SERVICE_SERVER_ORIGIN',    variable: 'COMMUNITY_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_REWARD_SERVICE_SERVER_ORIGIN',       variable: 'REWARD_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_FULFILLMENT_SERVICE_SERVER_ORIGIN',  variable: 'FULFILLMENT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_FILE_SERVICE_SERVER_ORIGIN',             variable: 'FILE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_NOTIFICATION_SERVICE_SERVER_ORIGIN', variable: 'NOTIFICATION_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_USER_SERVICE_DB_URL',              variable: 'USER_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_PRODUCT_SERVICE_DB_URL',           variable: 'PRODUCT_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_COMMERCE_SERVICE_DB_URL',          variable: 'COMMERCE_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_COMMUNITY_SERVICE_DB_URL',         variable: 'COMMUNITY_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_REWARD_SERVICE_DB_URL',            variable: 'REWARD_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_FULFILLMENT_SERVICE_DB_URL',       variable: 'FULFILLMENT_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_FILE_SERVICE_DB_URL',                  variable: 'FILE_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_NOTIFICATION_SERVICE_DB_URL',       variable: 'NOTIFICATION_SERVICE_DB_URL'),
        string(credentialsId: 'MARKETNOTE_QA_USER_SERVICE_DB_PASSWORD',         variable: 'USER_SERVICE_DB_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_PRODUCT_SERVICE_DB_PASSWORD',      variable: 'PRODUCT_SERVICE_DB_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_COMMERCE_SERVICE_DB_PASSWORD',     variable: 'COMMERCE_SERVICE_DB_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_COMMUNITY_SERVICE_DB_PASSWORD',    variable: 'COMMUNITY_SERVICE_DB_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_REWARD_SERVICE_DB_PASSWORD',       variable: 'REWARD_SERVICE_DB_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_FULFILLMENT_SERVICE_DB_PASSWORD',  variable: 'FULFILLMENT_SERVICE_DB_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_FILE_SERVICE_DB_PASSWORD',             variable: 'FILE_SERVICE_DB_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_NOTIFICATION_SERVICE_DB_PASSWORD',  variable: 'NOTIFICATION_SERVICE_DB_PASSWORD'),
    ]) {
        def creds = [
            USER_SERVICE_ECR_REPOSITORY: USER_SERVICE_ECR_REPOSITORY, USER_SERVICE_ECS_SERVICE_NAME: USER_SERVICE_ECS_SERVICE_NAME, USER_SERVICE_TARGET_GROUP_ARN: USER_SERVICE_TARGET_GROUP_ARN, USER_SERVICE_SERVER_ORIGIN: USER_SERVICE_SERVER_ORIGIN, USER_SERVICE_DB_URL: USER_SERVICE_DB_URL, USER_SERVICE_DB_PASSWORD: USER_SERVICE_DB_PASSWORD,
            PRODUCT_SERVICE_ECR_REPOSITORY: PRODUCT_SERVICE_ECR_REPOSITORY, PRODUCT_SERVICE_ECS_SERVICE_NAME: PRODUCT_SERVICE_ECS_SERVICE_NAME, PRODUCT_SERVICE_TARGET_GROUP_ARN: PRODUCT_SERVICE_TARGET_GROUP_ARN, PRODUCT_SERVICE_SERVER_ORIGIN: PRODUCT_SERVICE_SERVER_ORIGIN, PRODUCT_SERVICE_DB_URL: PRODUCT_SERVICE_DB_URL, PRODUCT_SERVICE_DB_PASSWORD: PRODUCT_SERVICE_DB_PASSWORD,
            COMMERCE_SERVICE_ECR_REPOSITORY: COMMERCE_SERVICE_ECR_REPOSITORY, COMMERCE_SERVICE_ECS_SERVICE_NAME: COMMERCE_SERVICE_ECS_SERVICE_NAME, COMMERCE_SERVICE_TARGET_GROUP_ARN: COMMERCE_SERVICE_TARGET_GROUP_ARN, COMMERCE_SERVICE_SERVER_ORIGIN: COMMERCE_SERVICE_SERVER_ORIGIN, COMMERCE_SERVICE_DB_URL: COMMERCE_SERVICE_DB_URL, COMMERCE_SERVICE_DB_PASSWORD: COMMERCE_SERVICE_DB_PASSWORD,
            COMMUNITY_SERVICE_ECR_REPOSITORY: COMMUNITY_SERVICE_ECR_REPOSITORY, COMMUNITY_SERVICE_ECS_SERVICE_NAME: COMMUNITY_SERVICE_ECS_SERVICE_NAME, COMMUNITY_SERVICE_TARGET_GROUP_ARN: COMMUNITY_SERVICE_TARGET_GROUP_ARN, COMMUNITY_SERVICE_SERVER_ORIGIN: COMMUNITY_SERVICE_SERVER_ORIGIN, COMMUNITY_SERVICE_DB_URL: COMMUNITY_SERVICE_DB_URL, COMMUNITY_SERVICE_DB_PASSWORD: COMMUNITY_SERVICE_DB_PASSWORD,
            REWARD_SERVICE_ECR_REPOSITORY: REWARD_SERVICE_ECR_REPOSITORY, REWARD_SERVICE_ECS_SERVICE_NAME: REWARD_SERVICE_ECS_SERVICE_NAME, REWARD_SERVICE_TARGET_GROUP_ARN: REWARD_SERVICE_TARGET_GROUP_ARN, REWARD_SERVICE_SERVER_ORIGIN: REWARD_SERVICE_SERVER_ORIGIN, REWARD_SERVICE_DB_URL: REWARD_SERVICE_DB_URL, REWARD_SERVICE_DB_PASSWORD: REWARD_SERVICE_DB_PASSWORD,
            FULFILLMENT_SERVICE_ECR_REPOSITORY: FULFILLMENT_SERVICE_ECR_REPOSITORY, FULFILLMENT_SERVICE_ECS_SERVICE_NAME: FULFILLMENT_SERVICE_ECS_SERVICE_NAME, FULFILLMENT_SERVICE_TARGET_GROUP_ARN: FULFILLMENT_SERVICE_TARGET_GROUP_ARN, FULFILLMENT_SERVICE_SERVER_ORIGIN: FULFILLMENT_SERVICE_SERVER_ORIGIN, FULFILLMENT_SERVICE_DB_URL: FULFILLMENT_SERVICE_DB_URL, FULFILLMENT_SERVICE_DB_PASSWORD: FULFILLMENT_SERVICE_DB_PASSWORD,
            FILE_SERVICE_ECR_REPOSITORY: FILE_SERVICE_ECR_REPOSITORY, FILE_SERVICE_ECS_SERVICE_NAME: FILE_SERVICE_ECS_SERVICE_NAME, FILE_SERVICE_TARGET_GROUP_ARN: FILE_SERVICE_TARGET_GROUP_ARN, FILE_SERVICE_SERVER_ORIGIN: FILE_SERVICE_SERVER_ORIGIN, FILE_SERVICE_DB_URL: FILE_SERVICE_DB_URL, FILE_SERVICE_DB_PASSWORD: FILE_SERVICE_DB_PASSWORD,
            NOTIFICATION_SERVICE_ECR_REPOSITORY: NOTIFICATION_SERVICE_ECR_REPOSITORY, NOTIFICATION_SERVICE_ECS_SERVICE_NAME: NOTIFICATION_SERVICE_ECS_SERVICE_NAME, NOTIFICATION_SERVICE_TARGET_GROUP_ARN: NOTIFICATION_SERVICE_TARGET_GROUP_ARN, NOTIFICATION_SERVICE_SERVER_ORIGIN: NOTIFICATION_SERVICE_SERVER_ORIGIN, NOTIFICATION_SERVICE_DB_URL: NOTIFICATION_SERVICE_DB_URL, NOTIFICATION_SERVICE_DB_PASSWORD: NOTIFICATION_SERVICE_DB_PASSWORD,
        ]
        def mapping = resolveServiceMapping(env.SERVICE_NAME, creds)
        if (!mapping) {
            error "SERVICE_NAME not mapped: ${env.SERVICE_NAME}"
        }
        env.ECR_REPOSITORY   = mapping.ecr
        env.ECS_SERVICE_NAME = mapping.ecs
        env.TARGET_GROUP_ARN = mapping.tg
        env.SERVER_ORIGIN    = mapping.origin
        env.DB_URL           = mapping.dbUrl
        env.DB_PASSWORD      = mapping.dbPw

        if (!env.ECR_REPOSITORY?.trim())   error "ECR_REPOSITORY not resolved for ${env.SERVICE_NAME}"
        if (!env.ECS_SERVICE_NAME?.trim()) error "ECS_SERVICE_NAME not resolved for ${env.SERVICE_NAME}"
        if (!env.TARGET_GROUP_ARN?.trim()) error "TARGET_GROUP_ARN not resolved for ${env.SERVICE_NAME}"

        echo "ECR_REPOSITORY   = ${env.ECR_REPOSITORY}"
        echo "ECS_SERVICE_NAME = ${env.ECS_SERVICE_NAME}"
        echo "TARGET_GROUP_ARN = ${env.TARGET_GROUP_ARN}"
    }
}

def registerTaskDefinition(parentScript) {
    withCredentials([
        string(credentialsId: 'MARKETNOTE_DB_USERNAME',                           variable: 'DB_USERNAME'),
        string(credentialsId: 'MARKETNOTE_JWT_SECRET_KEY',                        variable: 'JWT_SECRET_KEY'),
        string(credentialsId: 'MARKETNOTE_ACCESS_TOKEN_EXPIRATION_TIME',          variable: 'ACCESS_TOKEN_EXPIRATION_TIME'),
        string(credentialsId: 'MARKETNOTE_REFRESH_TOKEN_EXPIRATION_TIME',         variable: 'REFRESH_TOKEN_EXPIRATION_TIME'),
        string(credentialsId: 'MARKETNOTE_CLIENT_ORIGIN',                         variable: 'CLIENT_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_COOKIE_DOMAIN',                         variable: 'COOKIE_DOMAIN'),
        string(credentialsId: 'MARKETNOTE_ACCESS_CONTROL_ALLOWED_ORIGINS',        variable: 'ACCESS_CONTROL_ALLOWED_ORIGINS'),
        string(credentialsId: 'MARKETNOTE_QA_SPRING_PROFILE',                     variable: 'SPRING_PROFILE'),
        string(credentialsId: 'MARKETNOTE_GOOGLE_CLIENT_ID',                      variable: 'GOOGLE_CLIENT_ID'),
        string(credentialsId: 'MARKETNOTE_GOOGLE_CLIENT_SECRET',                  variable: 'GOOGLE_CLIENT_SECRET'),
        string(credentialsId: 'MARKETNOTE_KAKAO_CLIENT_ID',                       variable: 'KAKAO_CLIENT_ID'),
        string(credentialsId: 'MARKETNOTE_KAKAO_CLIENT_SECRET',                   variable: 'KAKAO_CLIENT_SECRET'),
        string(credentialsId: 'MARKETNOTE_KAKAO_ADMIN_KEY',                       variable: 'KAKAO_ADMIN_KEY'),
        string(credentialsId: 'MARKETNOTE_S3_ACCESS_KEY',                         variable: 'S3_ACCESS_KEY'),
        string(credentialsId: 'MARKETNOTE_S3_SECRET_KEY',                         variable: 'S3_SECRET_KEY'),
        string(credentialsId: 'MARKETNOTE_S3_BUCKET_NAME',                        variable: 'S3_BUCKET_NAME'),
        string(credentialsId: 'MARKETNOTE_AWS_ACCOUNT_ID',                        variable: 'AWS_ACCOUNT_ID'),
        string(credentialsId: 'MARKETNOTE_AWS_ACCESS_KEY_ID',                     variable: 'AWS_ACCESS_KEY_ID'),
        string(credentialsId: 'MARKETNOTE_AWS_SECRET_ACCESS_KEY',                 variable: 'AWS_SECRET_ACCESS_KEY'),
        string(credentialsId: 'MARKETNOTE_AWS_DEFAULT_REGION',                    variable: 'AWS_DEFAULT_REGION'),
        string(credentialsId: 'MARKETNOTE_ECS_TASK_EXECUTION_ROLE_ARN',           variable: 'ECS_TASK_EXECUTION_ROLE_ARN'),
        string(credentialsId: 'MARKETNOTE_ECS_TASK_ROLE_ARN',                     variable: 'ECS_TASK_ROLE_ARN'),
        string(credentialsId: 'MARKETNOTE_CLOUDWATCH_LOG_GROUP',                  variable: 'CLOUDWATCH_LOG_GROUP'),
        string(credentialsId: 'MARKETNOTE_SES_SMTP_USERNAME',                     variable: 'SES_SMTP_USERNAME'),
        string(credentialsId: 'MARKETNOTE_SES_SMTP_PASSWORD',                     variable: 'SES_SMTP_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_MAIL_FROM',                             variable: 'MAIL_FROM'),
        string(credentialsId: 'MARKETNOTE_MAIL_SENDER_NAME',                      variable: 'MAIL_SENDER_NAME'),
        string(credentialsId: 'MARKETNOTE_MAIL_VERIFICATION_TTL_MINUTES',         variable: 'MAIL_VERIFICATION_TTL_MINUTES'),
        string(credentialsId: 'MARKETNOTE_REDIS_PASSWORD',                        variable: 'REDIS_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_REDIS_HOST_NAME',                       variable: 'REDIS_HOST_NAME'),
        string(credentialsId: 'MARKETNOTE_REDIS_EMAIL_VERIFICATION_PREFIX',       variable: 'REDIS_EMAIL_VERIFICATION_PREFIX'),
        string(credentialsId: 'MARKETNOTE_QA_FILE_SERVICE_SERVER_ORIGIN',         variable: 'FILE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_PRODUCT_SERVICE_SERVER_ORIGIN',      variable: 'PRODUCT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_COMMERCE_SERVICE_SERVER_ORIGIN',     variable: 'COMMERCE_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_COMMUNITY_SERVICE_SERVER_ORIGIN',    variable: 'COMMUNITY_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_REWARD_SERVICE_SERVER_ORIGIN',       variable: 'REWARD_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_FULFILLMENT_SERVICE_SERVER_ORIGIN',      variable: 'FULFILLMENT_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_NOTIFICATION_SERVICE_SERVER_ORIGIN',  variable: 'NOTIFICATION_SERVICE_SERVER_ORIGIN'),
        string(credentialsId: 'MARKETNOTE_QA_JWT_ADMIN_ACCESS_TOKEN',             variable: 'JWT_ADMIN_ACCESS_TOKEN'),
        string(credentialsId: 'MARKETNOTE_QA_ADPOPCORN_ANDROID_HASH_KEY',         variable: 'ADPOPCORN_ANDROID_HASH_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_ADPOPCORN_IOS_HASH_KEY',             variable: 'ADPOPCORN_IOS_HASH_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_TNK_ANDROID_HASH_KEY',               variable: 'TNK_ANDROID_HASH_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_TNK_IOS_HASH_KEY',                   variable: 'TNK_IOS_HASH_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_ADISCOPE_ANDROID_HASH_KEY',          variable: 'ADISCOPE_ANDROID_HASH_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_ADISCOPE_IOS_HASH_KEY',              variable: 'ADISCOPE_IOS_HASH_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_FASSTO_BASE_URL',                    variable: 'FASSTO_BASE_URL'),
        string(credentialsId: 'MARKETNOTE_QA_FASSTO_API_CD',                      variable: 'FASSTO_API_CD'),
        string(credentialsId: 'MARKETNOTE_QA_FASSTO_API_KEY',                     variable: 'FASSTO_API_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_FASSTO_CUSTOMER_CODE',               variable: 'FASSTO_CUSTOMER_CODE'),
        string(credentialsId: 'MARKETNOTE_QA_KAFKA_BOOTSTRAP_SERVERS',            variable: 'KAFKA_BOOTSTRAP_SERVERS'),
        string(credentialsId: 'MARKETNOTE_QA_KAFKA_SLACK_WEBHOOK_URL',            variable: 'KAFKA_SLACK_WEBHOOK_URL'),
        string(credentialsId: 'MARKETNOTE_QA_KAFKA_SASL_ENABLED',                 variable: 'KAFKA_SASL_ENABLED'),
        string(credentialsId: 'MARKETNOTE_QA_KAFKA_SASL_MECHANISM',               variable: 'KAFKA_SASL_MECHANISM'),
        string(credentialsId: 'MARKETNOTE_QA_KAFKA_SASL_PROTOCOL',                variable: 'KAFKA_SASL_PROTOCOL'),
        string(credentialsId: 'MARKETNOTE_QA_KAFKA_SASL_USERNAME',                variable: 'KAFKA_SASL_USERNAME'),
        string(credentialsId: 'MARKETNOTE_QA_KAFKA_SASL_PASSWORD',                variable: 'KAFKA_SASL_PASSWORD'),
        string(credentialsId: 'MARKETNOTE_QA_HMAC_SECRET_KEY',                    variable: 'HMAC_SECRET_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_GIFTICON_PIN_ENCRYPT_KEY',           variable: 'GIFTICON_PIN_ENCRYPT_KEY'),
        string(credentialsId: 'MARKETNOTE_QA_GIFTICON_SYNC_SCHEDULER_ENABLED',   variable: 'GIFTICON_SYNC_SCHEDULER_ENABLED'),
        string(credentialsId: 'MARKETNOTE_QA_GIFTICON_SYNC_SCHEDULER_CRON',      variable: 'GIFTICON_SYNC_SCHEDULER_CRON'),
        string(credentialsId: 'MARKETNOTE_QA_GIFTICON_COUPON_SYNC_SCHEDULER_ENABLED', variable: 'GIFTICON_COUPON_SYNC_SCHEDULER_ENABLED'),
        string(credentialsId: 'MARKETNOTE_QA_GIFTICON_COUPON_SYNC_SCHEDULER_CRON',    variable: 'GIFTICON_COUPON_SYNC_SCHEDULER_CRON'),
    ]) {
        sh '''
          LG="$CLOUDWATCH_LOG_GROUP"
          EXISTS=$(aws logs describe-log-groups --log-group-name-prefix "$LG" --region "$AWS_DEFAULT_REGION" --query "length(logGroups[?logGroupName=='$LG'])" --output text || echo 0)
          if [ "$EXISTS" = "0" ]; then
            aws logs create-log-group --log-group-name "$LG" --region "$AWS_DEFAULT_REGION"
          fi
        '''

        def td = parentScript.buildMarketnoteTaskDefinition(env)

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

return this
