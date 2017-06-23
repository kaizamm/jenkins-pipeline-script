#!/usr/bin/env groovy
@Library('shareMaven') _

node {
  // def envList = myLoadProperties "/data/prepare_dac_tsp.properties"
  withEnv(envList) {
    stage ('选择动作') {
      def actionInput = input (
        id: 'actionInput', message: 'Choice your action!', parameters: [
        [$class: 'ChoiceParameterDefinition', choices: "deploy\nrollback", description: 'choice your action!', name: 'action']
        ])
      def action = actionInput.trim()
      if (action == 'deploy') {
        // 在docker内部代码检出、执行测试、执行包构建
        docker.image("${env.dockerMavenImage}").inside("${env.dockerMavenOpt}") {
          stage("检出源码") {
            codeCheckout{
              svnRepo="${this.env.svnRepo}"
              // svnCredentialsId="${this.env.svnCredentialsId}"
              // svnLocal="${this.env.svnLocal}"
            }
          }
          stage("执行测试") {
            mvnTest()
          }
          stage("包构建") {
            mvnPackage()
          }
        }

        // docker 镜像构建
        stage('镜像构建') {
          dockerBuild {
            propertiesPath = '/data/prepare_dac_tsp.properties'
          }
        }

        // 部署操作
        stage('部署生产') {
          deployContainer {
            propertiesPath = '/data/prepare_dac_tsp.properties'
          }
        }
      } else {
        // 版本回滚操作，针对镜像的版本回滚，会调用共享库类的几个stage操作
        stage('版本回滚') {
          rollbackContainer {
            propertiesPath = '/data/prepare_dac_tsp.properties'
            getRegistryTagList= '/data/jenkins_etcd/getRegistryTagList.py'
          }
        }
      }
    }
  }
}
