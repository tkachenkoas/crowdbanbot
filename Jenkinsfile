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
                    [configFile(fileId: "${params.config-file-id}", targetLocation : "${params.config-file-name}")]
                ) {
                }
                sh 'mvn Dprofile=dev -B -DskipTests clean package'
            }
        }
    }
}
