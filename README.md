# pipeline 使用说明
### 1. 目前所有项目有关的properties文件以及项目所需的配置文件，都放在git上，目录结构如下
```bash
.
├── appCfgs
│   ├── QUARK_PROD_DAC_TSP
│   │   ├── dac_tsp
│   │   │   └── WEB-INF
│   │   │       └── classes
│   │   │           └── test.xml
│   │   └── jenkinspipeline.properties
├── etcdGet.py
├── etcdPush.py
├── etcdRead.py
├── getRegistryTagList.py
├── import.sh

```
目前是配了web hook，一旦git上的文件发生变动，会触发jenkins上的`UPDATE_CONFIG_FROM_GIT`任务，执行配置文件导入。

### 2.properties文件内容类似如下
```bash
svnRepo=https://qf-project-01.quark.com:8443/svn/DAC/CodeLib/dac/branches/DAC_MOBILE_20170401

dockerMavenImage=172.30.33.31:5000/service/maven:3.5.0-8u74
dockerMavenRunOpts=--add-host qf-javadev-01:172.16.1.39 -v /data/maven_repo:/home/qkuser/.m2
mavenPackageOpts=-Pprod clean install -Dmaven.test.skip=true
mavenTestOpts=-Dmaven.test.failure.ignore  test

appOrg=quark
appEnv=prod
appTargetName=dac_tsp
etcdClusterIp=172.30.33.31
fromImage=172.30.33.31:5000/base/tomcat:8.5.15-8u74
toImage=172.30.33.31:5000/${appOrg}/${appTargetName}
appCfgs=
projectRecipientList=ChenglanGuo@quarkfinance.com
dockerHosts=172.30.33.31_10000,10000:8080 172.30.33.31_10001,10001:8080
dockerRunOpts=\
--add-host=idc-hadoopsh-01:10.19.72.1 \
--add-host=idc-hadoopsh-02:10.19.72.2 \
--add-host=idc-hadoopsh-03:10.19.72.3 \
--add-host=idc-hadoopsh-04:10.19.72.4
javaOpts='\
-server \
-Xms12g \
-Xmx12g \
-Xss512k \
-XX:PermSize=512m \
-XX:MaxPermSize=768m \
-XX:+AggressiveOpts \
-XX:+UseBiasedLocking \
-XX:MaxTenuringThreshold=7 \
-XX:+CMSParallelRemarkEnabled \
-XX:+UseCMSCompactAtFullCollection \
-XX:+UseFastAccessorMethods \
-Djava.awt.headless=true \
-XX:+UseConcMarkSweepGC \
-XX:+UseParNewGC'
```
除了极个别特殊的项目(如：审核)，一般正常的项目，都是这个基本的模板，如果有配置文件需要加载，只需要将配置文件填到appCfgs的位置，以逗号隔开即可。

### 3.JOB名注意事项
```bash
  // 读取properties文件
  myLoadProperties ('172.30.33.31',2379,"/quarkfinance.com/instances/${env.JOB_BASE_NAME}/jenkinspipeline.properties")
```
唯一需要注意的是，每个项目的JOB名称，必须是`appOrg_appEnv_appTargetName`的组合，比如审核项目`appOrg=quark`,`appEnv=prod`,`appTargetName=qcredit-bkend`,那么它的job名则必须是`QUARK_PROD_QCREDIT-BKEND`，注意后面的QCREDIT-BKEND，这里的下划线，是appTargetName本身存在的，并不是指定的，一定要注意。比如dac项目中的task项目，它的JOB名就是QUARK_PROD_PBOC_TASK,因为他的appTargetName是pboc_task。

#### 4.每个项目的groovy
```bash
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
          dockerBuild {}
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

```
只要不是审核项目那种耦合性太高的项目，基本上，使用类似dac_tsp和pboc_task这样的模板即可。

### 5.具体使用
可参考现有的审核项目，或者dac项目
