def call(Map config) {
    pipeline {
        agent any

        environment {
            DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
            KUBECONFIG_CREDENTIALS_ID = 'kubeconfig'
        }

        stages {
            stage('Checkout') {
                steps {
                    git url: config.gitRepo
                }
            }
            stage('Build') {
                steps {
                    sh 'mvn clean package'
                }
            }
            stage('Docker Build & Push') {
                steps {
                    withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh '''
                        docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
                        docker build -t ${config.dockerImage}:${BUILD_NUMBER} .
                        docker push ${config.dockerImage}:${BUILD_NUMBER}
                        '''
                    }
                }
            }
            stage('Deploy to EKS') {
                steps {
                    withCredentials([file(credentialsId: "${KUBECONFIG_CREDENTIALS_ID}", variable: 'KUBECONFIG_FILE')]) {
                        sh '''
                        export KUBECONFIG=$KUBECONFIG_FILE
                        kubectl set image deployment/${config.k8sDeploymentName} ${config.k8sDeploymentName}=${config.dockerImage}:${BUILD_NUMBER} -n ${config.k8sNamespace}
                        '''
                    }
                }
            }
        }
    }
}
