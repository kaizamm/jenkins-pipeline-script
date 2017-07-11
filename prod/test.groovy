#!/usr/bin/env groovy
@Library('shareMaven') _
node {
  stage('检出源码'){
    gitCodeCheckout{
      gitRepo='http://172.16.4.213/OPS/jenkins_etcd.git'
      gitLocal='jenkins_etcd'
      // gitCredentialsId
    }
  }
}
