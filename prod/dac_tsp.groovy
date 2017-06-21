#!/usr/bin/env groovy
@Library('shareMaven') _
node {
  stage('Docker Build') {
    docker.image('172.30.33.31:5000/service/maven:3.5.0-8u74').inside {
      stage("CheckoutCode") {
        codeCheckout{
          svnRepo="https://qf-project-01.quark.com:8443/svn/DAC/CodeLib/dac/branches/DAC_MOBILE_20170401"
          svnCredentialsId="c9baf728-2463-4d59-8643-2181a681fdd4"
          svnLocal="."
        }
      }
    }
  }
}
