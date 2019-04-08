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
                    [configFile(fileId: "tg-ban-bot-dev", targetLocation : "conf-dev.properties")]
                ) {
                }
                sh 'mvn Dprofile=dev -B -DskipTests clean package'
                sh 'heroku deploy:jar target/crowdbanbot-1.0-SNAPSHOT.jar -app crowdbanbot'
            }
        }
    }
}
