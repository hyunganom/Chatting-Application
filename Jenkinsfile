pipeline {
    agent any
    tools {
        jdk 'JDK11'
        maven 'MAVEN_HOME'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id')
    }

    stages {
        stage('Checkout') {
            steps {
                // Git에서 프로젝트 전체를 가져와 docker-compose.yml 파일 포함
                git url: 'https://github.com/hyunganom/Chatting-Application.git', branch: 'main'

                // docker-compose.yml 위치 확인
                sh 'ls -l ${WORKSPACE}'
            }
        }

        stage('Build Eureka Server') {
            steps {
                dir('chatapp-eureka-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build API Gateway') {
            steps {
                dir('chatapp-apigateway-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build API Gateway') {
            steps {
                dir('chatapp-user-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def eurekaImage = docker.build("rheonik/chat-eureka-server:1.0", "chatapp-eureka-server/")
                    def apiGatewayImage = docker.build("rheonik/chat-apigateway-server:1.0", "chatapp-apigateway-server/")
                    def userImage = docker.build("rheonik/chat-user-server:1.0", "chatapp-user-server/")

                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials-id') {
                        eurekaImage.push()
                        apiGatewayImage.push()
                    }
                }
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                script {
                    // docker-compose.yml이 Git에서 가져온 상태로 ${WORKSPACE}에 있으므로 경로를 명시하지 않고 사용 가능
                    sh '''
                        cd ${WORKSPACE}
                        docker-compose pull
                        docker-compose -p chatting_application up -d
                    '''
                }
            }
        }
    }

    post {
        always {
            echo '파이프라인 완료'
        }
        failure {
            echo '파이프라인 실패'
        }
    }
}
