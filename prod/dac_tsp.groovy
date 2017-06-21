#!/usr/bin/env groovy
@Library('shareMaven') _
node {
  stage('Docker Build') {
    docker.image('172.30.33.31:5000/service/maven:3.5.0-8u74').inside {
      stage("CheckoutCode") {
        CheckoutCode{
          svnRepo="https://qf-project-01.quark.com:8443/svn/DAC/CodeLib/dac/branches/DAC_MOBILE_20170401"
        }
      }
    }
  }
}
