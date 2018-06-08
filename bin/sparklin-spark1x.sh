#!/usr/bin/env bash

MAVEN_REPO_BASE=$HOME/.m2/repository/org/jetbrains/kotlin
KOTLIN_VERSION=1.2.41
KSHELL_REPL_API_VERSION=0.2.2-1.2.41
SPARKLIN_VERSION=0.2-SNAPSHOT

# Kotlin libraries
KOTLIN_STDLIB=${MAVEN_REPO_BASE}/kotlin-stdlib/${KOTLIN_VERSION}/kotlin-stdlib-${KOTLIN_VERSION}.jar
KOTLIN_COMPILER=${MAVEN_REPO_BASE}/kotlin-compiler/${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.jar
KOTLIN_SCRIPT_RUNTIME=${MAVEN_REPO_BASE}/kotlin-script-runtime/${KOTLIN_VERSION}/kotlin-script-runtime-${KOTLIN_VERSION}.jar
KOTLIN_REFLECT=${MAVEN_REPO_BASE}/kotlin-reflect/${KOTLIN_VERSION}/kotlin-reflect-${KOTLIN_VERSION}.jar

# JLine2 console support
FUSE_JANSI=$HOME/.m2/repository/org/fusesource/jansi/jansi/1.15/jansi-1.15.jar
KSHELL_CONSOLE_JLINE3=$HOME/.m2/repository/sparklin/jline3-shaded/${SPARKLIN_VERSION}/jline3-shaded-${SPARKLIN_VERSION}.jar
KSHELL_REPL_API=$HOME/.m2/repository/com/github/khud/kshell-repl-api/${KSHELL_REPL_API_VERSION}/kshell-repl-api-${KSHELL_REPL_API_VERSION}.jar

# Spark 1.x support
SPARKLIN_PLUGIN=$HOME/.m2/repository/sparklin/sparklin-spark-1.x/${SPARKLIN_VERSION}/sparklin-spark-1.x-${SPARKLIN_VERSION}.jar
HDFS_BROWSER_PLUGIN=$HOME/.m2/repository/sparklin/hdfs-browser/${SPARKLIN_VERSION}/hdfs-browser-${SPARKLIN_VERSION}.jar

# Please specify path to KShell configuration file, note that:
# 1) If property config.path isn't specified Sparklin is trying find config file in path $HOME/.kshell
# 2) If file does not exist Spark support would not work because the plugin is disabled by default
SPARKLIN_CONFIG=$HOME/Documents/research_projects/sparklin/conf/spark1x.properties

# KShell path
KSHELL=$HOME/.m2/repository/sparklin/kshell/${SPARKLIN_VERSION}/kshell-${SPARKLIN_VERSION}.jar

# Apache Spark home directory must point to Spark version 1.x > 1.6
SPARK_HOME=$HOME/Programs/spark-1.6.1-bin-hadoop2.6

JARS=${KOTLIN_COMPILER},${KOTLIN_STDLIB},${KOTLIN_REFLECT},${KOTLIN_SCRIPT_RUNTIME},${KSHELL_CONSOLE_JLINE3},${KSHELL_REPL_API},${SPARKLIN_PLUGIN},${HDFS_BROWSER_PLUGIN}

${SPARK_HOME}/bin/spark-submit --jars=${JARS} --conf "spark.driver.extraJavaOptions=-Dconfig.path=${SPARKLIN_CONFIG}" \
        --master "local[2]" --class "com.github.khud.sparklin.kshell.KotlinShell" \
		${KSHELL} \
		$@