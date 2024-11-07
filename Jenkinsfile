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
                git url: 'https://github.com/hyunganom/Chatting-Application.git', branch: 'main'
                sh 'ls -l ${WORKSPACE}'
            }
        }

        stage('Build Modules') {
            steps {
                dir('Chatting-Application') { // 프로젝트 루트 디렉토리로 이동
                    parallel (
                        "Build Eureka Server": {
                            dir('chatapp-eureka-server') {
                                sh 'mvn clean package -DskipTests'
                            }
                        },
                        "Build API Gateway": {
                            dir('chatapp-apigateway-server') {
                                sh 'mvn clean package -DskipTests'
                            }
                        },
                        "Build User Server": {
                            dir('chatapp-user-server') {
                                sh 'mvn clean package -DskipTests'
                            }
                        },
                        "Build Chat Server": {
                            dir('chatapp-chat-server') {
                                sh 'mvn clean package -DskipTests'
                            }
                        },
                        "Build Websocket Server": {
                            dir('chatapp-websocket-server') {
                                sh 'mvn clean package -DskipTests'
                            }
                        },
                        "Build Message Server": {
                            dir('chatapp-message-server') {
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    )
                }
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                dir('Chatting-Application') { // 프로젝트 루트 디렉토리로 이동
                    script {
                        def eurekaImage = docker.build("rheonik/chat-eureka-server:1.0", "chatapp-eureka-server/")
                        def apiGatewayImage = docker.build("rheonik/chat-apigateway-server:1.0", "chatapp-apigateway-server/")
                        def userImage = docker.build("rheonik/chat-user-service:1.0", "chatapp-user-server/")
                        def chatImage = docker.build("rheonik/chat-chat-service:1.0", "chatapp-chat-server/")
                        def websocketImage = docker.build("rheonik/chat-websocket-service:1.0", "chatapp-websocket-server/")
                        def messageImage = docker.build("rheonik/chat-message-service:1.0", "chatapp-message-server/")

                        docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials-id') {
                            eurekaImage.push()
                            apiGatewayImage.push()
                            userImage.push()
                            chatImage.push()
                            websocketImage.push()
                            messageImage.push()
                        }
                    }
                }
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                dir('Chatting-Application') { // 프로젝트 루트 디렉토리로 이동
                    script {
                        sh '''
                            docker-compose down
                            docker-compose pull
                            docker-compose -p chatting_application up -d
                        '''
                    }
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
