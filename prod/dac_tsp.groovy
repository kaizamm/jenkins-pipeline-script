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
        checkout([$class: 'SubversionSCM',
        additionalCredentials: [],
        excludedCommitMessages: '',
        excludedRegions: '',
        excludedRevprop: '',
        excludedUsers: '',
        filterChangelog: false,
        ignoreDirPropChanges: false,
        includedRegions: '',
        locations: [[credentialsId: 'c9baf728-2463-4d59-8643-2181a681fdd4', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: 'https://qf-project-01.quark.com:8443/svn/DAC/CodeLib/dac/branches/DAC_MOBILE_20170401']],
        workspaceUpdater: [$class: 'UpdateUpdater']])

        sh "${tool 'M3'}/bin/mvn test"

        sh "${tool 'M3'}/bin/mvn mvn -f pom.xml -Pprod clean install -Dmaven.test.skip=true"

        stash includes: '**/target/*.war',name: 'app'
      }
    }

    stage('Generate Dockerfile') {
      agent {
        label 'jenkins-slave-01'
      }
      steps {
        unstash 'app'
      }
    }
  }
}
