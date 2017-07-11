#!/usr/bin/env groovy
@Library('shareMaven') _
node {
  stage('检出源码'){
    gitCodeCheckout{
      gitRepo='ssh://git@172.16.4.213:222/OPS/jenkins-pipeline-script.git'
      gitLocal='jenkins_etcd'
      gitCredentialsId='f7f592e0-8748-4c06-ac5d-14c1c98f46b4'
    }
  }
}
