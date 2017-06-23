#!/usr/bin/env groovy
@Library('shareMaven') _
@NonCPS
def mapToList(depmap) {
    def dlist = []
    for (entry in depmap) {
        dlist.add([entry.key, entry.value])
    }
    dlist
}

node {
  def props = readProperties file: "/data/prepare_dac_tsp.properties"
  def envList = []
  for (it2 in mapToList(props)) {
      def key=it2[0]
      def val=it2[1]
      envList << key+"="+val
  }
  withEnv(envList) {
    stage ('选择动作') {
    def actionInput = input (
      id: 'actionInput', message: 'Choice your action!', parameters: [
      [$class: 'ChoiceParameterDefinition', choices: "deploy\nrollback", description: 'choice your action!', name: 'action']
      ])
    def action = actionInput.trim()
    if (action == 'deploy') {
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
      stage('镜像构建') {
        dockerBuild {
          propertiesPath = '/data/prepare_dac_tsp.properties'
        }
      }
      stage('部署生产') {
        deployContainer {
          propertiesPath = '/data/prepare_dac_tsp.properties'
        }
      }
    } else {
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
