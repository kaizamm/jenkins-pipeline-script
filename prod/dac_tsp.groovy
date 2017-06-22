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
  stage('Docker Build') {
    codeCheckout {
      svnRepo="${env.svnRepoAddress}"
    }
    docker.image("${env.dockerMavenImage}").inside("${env.dockerMavenOpt}") {
      stage("检出源码") {
        // codeCheckout{
        //   svnRepo="${env.svnRepoAddress}"
        // }
      }
      stage("执行测试") {
        mvnTest()
      }
      stage("执行构建") {
        mvnPackage()
      }
    }
  }
  stage('Generate Dockerfile') {
    generateDockerfile {
      propertiesPath = '/data/prepare_dac_tsp.properties'
    }
  }
}
}
