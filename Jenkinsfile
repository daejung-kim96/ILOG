pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        gitLabConnection('gitlab-connection')
    }

    environment {
        EC2_HOST = "ip-172-26-5-67"
        EC2_PATH = "/home/ubuntu/app"
    }

    stages {

        /* -------------------------------------------------------
           1) Checkout (fetch 제거)
        ------------------------------------------------------- */
        stage('Checkout') {
            steps {
                cleanWs()

                checkout([$class: 'GitSCM',
                    branches: scm.branches,
                    userRemoteConfigs: scm.userRemoteConfigs,
                    extensions: [
                        [$class: 'WipeWorkspace'],
                        [$class: 'CleanBeforeCheckout'],
                        [$class: 'CloneOption', shallow: true, depth: 1, noTags: true]
                    ]
                ])
            }
        }

        /* -------------------------------------------------------
           2) 브랜치 감지 (실패해도 배포)
        ------------------------------------------------------- */
        stage('Detect-Branch') {
            steps {
                script {
                    try {
                        env.GIT_BRANCH = sh(
                            script: "git rev-parse --abbrev-ref HEAD",
                            returnStdout: true
                        ).trim()
                    } catch (err) {
                        env.GIT_BRANCH = "unknown"
                    }

                    echo "🔥 DETECTED GIT_BRANCH => ${env.GIT_BRANCH}"
                }
            }
        }

        /* -------------------------------------------------------
           3) Debug 출력
        ------------------------------------------------------- */
        stage('DEBUG-BRANCH') {
            steps {
                echo "-------------------------------"
                echo "GIT_BRANCH   = ${env.GIT_BRANCH}"
                echo "-------------------------------"
            }
        }

        /* -------------------------------------------------------
           4) Frontend Build (항상 실행)
        ------------------------------------------------------- */
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh """
                        npm ci
                        npm run build
                    """
                }
            }
        }

        /* -------------------------------------------------------
           5) Backend Build (Parallel, 항상 실행)
        ------------------------------------------------------- */
        stage('Build Backend Services') {
            parallel {
                stage('Auth')   { steps { dir('auth')   { sh "chmod +x ./gradlew && ./gradlew clean bootJar -x test" }}}
                stage('Backend'){ steps { dir('backend'){ sh "chmod +x ./gradlew && ./gradlew clean bootJar -x test" }}}
                stage('Gateway'){ steps { dir('gateway'){ sh "chmod +x ./gradlew && ./gradlew clean bootJar -x test" }}}
                stage('User')   { steps { dir('user')   { sh "chmod +x ./gradlew && ./gradlew clean bootJar -x test" }}}
                stage('Group')  { steps { dir('group')  { sh "chmod +x ./gradlew && ./gradlew clean bootJar -x test" }}}
            }
        }

        /* -------------------------------------------------------
           6) EC2 업로드 (항상 실행)
        ------------------------------------------------------- */
        stage('Upload Compose + Build Files to EC2') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    sh """
                        ssh ubuntu@${EC2_HOST} "
                            mkdir -p ${EC2_PATH}/frontend &&
                            rm -rf ${EC2_PATH}/frontend/*
                        "

                        scp docker-compose.yml docker-compose.prod.yml ubuntu@${EC2_HOST}:${EC2_PATH}/

                        scp -r frontend/dist ubuntu@${EC2_HOST}:${EC2_PATH}/frontend/
                        scp -r auth backend gateway user group database infra ubuntu@${EC2_HOST}:${EC2_PATH}/
                    """
                }
            }
        }

        /* -------------------------------------------------------
           7) Docker Compose Deploy (항상 실행)
        ------------------------------------------------------- */
        stage('Deploy on EC2') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    sh """
                        ssh ubuntu@${EC2_HOST} "
                            cd ${EC2_PATH} &&
                            docker compose -f docker-compose.yml -f docker-compose.prod.yml down --remove-orphans || true &&
                            docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
                        "
                    """
                }
            }
        }

    }

    post {
        success { echo '🎉 Deployment Successful!' }
        failure { echo '❌ Deployment Failed!' }
    }
}
