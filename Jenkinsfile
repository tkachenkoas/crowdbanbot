pipeline {
    agent {
        docker {
            image 'maven:3-alpine' 
            args '-v /root/.m2:/root/.m2' 
        }
    }
    stages {
        stage('Build') { 
            steps {
                configFileProvider(
                    [configFile(fileId: 'tg-ban-bot-dev', targetLocation : 'conf-local.properties')]
                ) {
                }
                sh 'mvn -B -DskipTests clean package' 
            }
        }
    }
}
