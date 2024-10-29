pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id')
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/hyunganom/Chatting-Application.git', branch: 'main'
            }
        }

        stage('Build') {
            steps {
                dir('eureka-server') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('eureka-server') {
                    script {
                        sh '''
                        docker buildx create --use --name mybuilder || docker buildx use mybuilder
                        docker buildx build --platform linux/amd64,linux/arm64 \
                          -t rheonik/eureka-server:latest --push .
                        '''
                    }
                }
            }
        }

        // Push Docker Image 단계는 이미 --push 옵션으로 푸시되므로 생략

        stage('Deploy') {
            steps {
                sh '''
                cd /Users/hyunjae/desktop
                docker-compose pull eureka-server
                docker-compose up -d eureka-server
                '''
            }
        }
    }
}
