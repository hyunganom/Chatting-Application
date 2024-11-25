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
                    // Docker 이미지를 직접 빌드
                    sh 'docker build -t rheonik/chat-eureka-server:1.0 chatapp-eureka-server/'
                    sh 'docker build -t rheonik/chat-apigateway-server:1.0 chatapp-apigateway-server/'
                    sh 'docker build -t rheonik/chat-user-service:1.0 chatapp-user-server/'
                    sh 'docker build -t rheonik/chat-websocket-service:1.0 chatapp-websocket-server/'
                    sh 'docker build -t rheonik/chat-message-service:1.0 chatapp-message-server/'

                    // 이미지 존재 여부 확인
                    sh 'docker images'

                    // Docker 레지스트리에 로그인하고 이미지 푸시
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials-id') {
                        sh 'docker push rheonik/chat-eureka-server:1.0'
                        sh 'docker push rheonik/chat-apigateway-server:1.0'
                        sh 'docker push rheonik/chat-user-service:1.0'
                        sh 'docker push rheonik/chat-websocket-service:1.0'
                        sh 'docker push rheonik/chat-message-service:1.0'
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
