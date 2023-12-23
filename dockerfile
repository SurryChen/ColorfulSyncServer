# 安装环境：JDK11
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 传入项目Jar包路径
ARG JAR_FILE=target/*.jar
# 复制Jar到镜像内
COPY ${JAR_FILE} app.jar

# 暴露端口
ARG PORT=8002
EXPOSE ${PORT}

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]