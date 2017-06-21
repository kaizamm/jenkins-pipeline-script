#!/usr/bin/env groovy
@Library('shareMaven') _
pipeline {
  agent none
  stages {
    stage('Build') {
      agent {
        docker {
          image '172.30.33.31:5000/service/maven:3.5.0-8u74'
          args '--add-host qf-javadev-01:172.16.1.39 -v /data/maven_repo:/home/qkuser/.m2'
        }
      }
      steps {
        codeCheckout{
          svnRepo='https://qf-project-01.quark.com:8443/svn/DAC/CodeLib/dac/branches/DAC_MOBILE_20170401'
        }
      }
    }

    stage('Generate Dockerfile') {
      agent {
        label 'jenkins-slave-01'
      }
      steps {
        echo "ok"
      }
    }
  }
}
