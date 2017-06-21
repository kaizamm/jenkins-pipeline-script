#!/usr/bin/env groovy
@Library('shareMaven') _
node {
  stage('Docker Build') {
    docker.image('172.30.33.31:5000/service/maven:3.5.0-8u74').inside("--add-host qf-javadev-01:172.16.1.39 -v /data/maven_repo:/home/qkuser/.m2") {
      stage("检出源码") {
        codeCheckout{
          svnRepo="https://qf-project-01.quark.com:8443/svn/DAC/CodeLib/dac/branches/DAC_MOBILE_20170401"
        }
        mvnTest()
        mvnPackage()
      }
    }
  }
}
