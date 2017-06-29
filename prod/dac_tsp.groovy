#!/usr/bin/env groovy
@Library('shareMaven') _

node {
  // def applicationConfigPath = "/data/jenkins_etcd/appcfgs"
  // 读取properties文件
  // def envList = myLoadProperties "/data/jenkins_etcd/appCfgs/${env.JOB_BASE_NAME}/jenkinspipeline.properties"
  myLoadProperties ('172.30.33.31',2379,"/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties")
  // withEnv(envList) {
    stage ('选择动作') {
      try {
        // action 选择，有deploy和rollback两种动作
        def action = choiceAction ()

        if (action == 'deploy') {
          // 在docker内部代码检出、执行测试、执行包构建
          docker.image("${env.dockerMavenImage}").inside("${env.dockerMavenRunOpts}") {
            stage("检出源码") {
              codeCheckout{
                svnRepo="${this.env.svnRepo}"
                // svnCredentialsId="${this.env.svnCredentialsId}"
                // svnLocal="${this.env.svnLocal}"
              }
            }
            stage("执行测试") {
              // 如果代码是取到当前"."目录下，则直接用下面即可
              // mvnTest()
              // 否则使用properties里面的内容
              // mvnTest("${this.env.mavenTestOpts}")
              mvnTest()
            }
            stage("包构建") {
              mvnPackage("${this.env.mavenPackageOpts}")
            }
          }

          // docker 镜像构建
          stage('镜像构建') {
            dockerBuild {
              // host = "172.30.33.31"
              // port = 2379
              // location = "${location}"
            }
          }

          // 部署操作
          stage('部署生产') {
            deployContainer {
              // host = "172.30.33.31"
              // port = 2379
              // location = "/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties"
            }
          }
        } else {
          // 版本回滚操作，针对镜像的版本回滚，会调用共享库类的几个stage操作
          stage('版本回滚') {
            rollbackContainer {
              // propertiesPath = "/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties"
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
  // }
}
