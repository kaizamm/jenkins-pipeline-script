#!/usr/bin/env groovy
@Library('shareMaven') _
node {
  stage('检出源码'){
    gitCodeCheckout{
      gitRepo='http://172.16.4.213/OPS/jenkins_etcd.git'
      gitLocal='jenkins_etcd'
      gitCredentialsId='f7f592e0-8748-4c06-ac5d-14c1c98f46b4'
    }
  }
}
