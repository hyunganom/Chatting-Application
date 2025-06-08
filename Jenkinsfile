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

        stage('Build User Server') {
            steps {
                dir('chatapp-user-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Chat Server') {
            steps {
                dir('chatapp-chat-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Websocket Server') {
            steps {
                dir('chatapp-websocket-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Message Server') {
            steps {
                dir('chatapp-message-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def eurekaImage     = docker.build("rheonik/chat-eureka-server:1.0",     "chatapp-eureka-server")
                    def apiGatewayImage = docker.build("rheonik/chat-apigateway-server:1.0", "chatapp-apigateway-server")
                    def userImage       = docker.build("rheonik/chat-user-service:1.0",     "chatapp-user-server")
                    def chatImage       = docker.build("rheonik/chat-chat-service:1.0",     "chatapp-chat-server")
                    def websocketImage  = docker.build("rheonik/chat-websocket-service:1.0","chatapp-websocket-server")
                    def messageImage    = docker.build("rheonik/chat-message-service:1.0",  "chatapp-message-server")

                    sh 'docker images'

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

        stage('Deploy Core Stack') {
            steps {
                script {
                    sh '''
                      cd ${WORKSPACE}
                      docker-compose -f docker-compose.base.yml -p chatting_application_core down
                      docker-compose -f docker-compose.base.yml -p chatting_application_core pull
                      docker-compose -f docker-compose.base.yml -p chatting_application_core up -d
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
