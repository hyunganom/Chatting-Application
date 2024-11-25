pipeline {
    agent any
    tools {
        jdk 'JDK11'
        maven 'MAVEN_HOME'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id')
        DOCKER_BUILDKIT = '0'  // BuildKit 비활성화
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

        // 필요한 경우 Chat Server 단계 활성화
        // stage('Build Chat Server') {
        //     steps {
        //         dir('chatapp-chat-server') {
        //             sh 'mvn clean package -DskipTests'
        //         }
        //     }
        // }

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
                    // BuildKit 비활성화로 인해 이미지를 로컬에 로드할 수 있게 됩니다.
                    def eurekaImage = docker.build("rheonik/chat-eureka-server:1.0", "chatapp-eureka-server/")
                    def apiGatewayImage = docker.build("rheonik/chat-apigateway-server:1.0", "chatapp-apigateway-server/")
                    def userImage = docker.build("rheonik/chat-user-service:1.0", "chatapp-user-server/")
                    def websocketImage = docker.build("rheonik/chat-websocket-service:1.0", "chatapp-websocket-server/")
                    def messageImage = docker.build("rheonik/chat-message-service:1.0", "chatapp-message-server/")

                    // 빌드된 이미지 확인
                    sh 'docker images'

                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials-id') {
                        eurekaImage.push()
                        apiGatewayImage.push()
                        userImage.push()
                        websocketImage.push()
                        messageImage.push()
                    }
                }
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                script {
                    sh '''
                        cd ${WORKSPACE}
                        docker-compose down  # 기존 컨테이너를 중지하고 제거
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
