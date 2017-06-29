#!/usr/bin/env groovy
@Library('shareMaven')
node {
  def envList = myLoadProperties ("172.30.33.31",2379,"/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties")
  stage('选择动作') {
    try {
      def action = choiceAction()
      if (action == 'deploy') {
        withEnv(envList) {
          docker.image("${env.dockerMavenImage}").inside("${env.dockerMavenRunOpts}") {
            stage("检出源码") {
              codeCheckout {
                svnRepo = "${this.env.svnRepo}"
              }
            }
            stage("执行测试") {
              mvnTest()
            }
            stage("包构建") {
              mvnPackage("${this.env.mavenPackageOpts}")
            }
          }
        }
        stage("镜像构建") {
          deployContainer {
            propertiesPath = "/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties"
          }
        }
        stage("部署生产") {
          propertiesPath = "/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties"
        }
      } else {
        stage("版本回滚") {
          rollbackContainer {
            propertiesPath = "/quarkfinance.com/instances/"+System.getenv("JOB_BASE_NAME")+"/jenkinspipeline.properties"
            getRegistryTagList= '/data/jenkins_etcd/getRegistryTagList.py'
          }
        }
      }
    } catch (exc) {
      sendEmail {
        emailRecipients= "${this.env.projectRecipientList}"
        error exc
      }
    }
  }
}
