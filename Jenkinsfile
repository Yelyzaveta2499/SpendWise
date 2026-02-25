pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B -DskipTests=true clean install'
            }
        }

        stage('Unit Test') {
             steps {
                bat 'mvn -B clean test'
             }
        }

        stage('Code Coverage') {
                     steps {
                        bat 'mvn -B jacoco:report'
                        bat 'mvn verify'
                     }
                }

        stage('Package') {
            steps {
                bat 'mvn -B -DskipTests=true package'
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: '**/target/*.jar, **/target/*.war', allowEmptyArchive: true

            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Code Coverage'
            ])
        }
    }
}