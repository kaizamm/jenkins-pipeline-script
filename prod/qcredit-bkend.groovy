#!/usr/bin/env groovy
@Library('shareMaven') _

node {
  // 读取properties文件
  myLoadProperties ('172.30.33.31',2379,"/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties")
  stage ('选择动作') {
    try {
      // action 选择，有deploy和rollback两种动作
      def action = choiceAction ()
      if (action == 'deploy') {
        // 在docker内部代码检出、执行测试、执行包构建
        docker.image("${env.dockerMavenImage}").inside("${env.dockerMavenRunOpts}") {
          stage("检出源码") {
            // 检出base源码
            codeCheckout{
              svnRepo="${this.env.qfBaseRepo}"
              // svnCredentialsId="${this.env.svnCredentialsId}"
              svnLocal="${this.env.qfBaseLocal}"
            }
            // 检出parent
            codeCheckout {
              svnRepo="${this.env.qfParentRepo}"
              svnLocal="${this.env.qfParentLocal}"
            }
            // 检出qf-redis
            codeCheckout {
              svnRepo="${this.env.qfRedisRepo}"
              svnLocal="${this.env.qfRedisLocal}"
            }
            // 检出qf-core
            codeCheckout {
              svnRepo="${this.env.qfCoreRepo}"
              svnLocal="${this.env.qfCoreLocal}"
            }
            // 检出qf-entities
            codeCheckout {
              svnRepo="${this.env.qfEntitiesRepo}"
              svnLocal="${this.env.qfEntitiesLocal}"
            }
            // 检出qcredit-bkend
            codeCheckout {
              svnRepo="${this.env.qcreditBkendRepo}"
              svnLocal="${this.env.qcreditBkendLocal}"
            }
            // 检出qcredit-frontal
            // codeCheckout {
            //   svnRepo="${this.env.qcreditFrontalRepo}"
            //   svnLocal="${this.env.qcreditFrontalLocal}"
            // }
          }
          stage("mvntest") {
            // 如果代码是取到当前"."目录下，则直接用下面即可
            // mvnTest()
            // 否则使用properties里面的内容
            mvnTest("${this.env.mavenTestOpts}")
            // mvnTest()
          }
          stage("包构建") {
            mvnPackage("${this.env.mavenPackageOpts}")
            mvnPackage("${this.env.mavenPackageOpts2}")
          }
        }
        // docker 镜像构建
        stage('镜像构建') {
          dockerBuild {
            projectName = "${this.env.projectName}"
            packageName = "${this.env.appTargetName}"
          }
        }
        // 部署操作
        stage('部署生产') {
          deployContainer {}
        }
      } else {
        // 版本回滚操作，针对镜像的版本回滚，会调用共享库类的几个stage操作
        stage('版本回滚') {
          rollbackContainer {
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
