前端  
Project Setup
```
npm install
```
Compile and Hot-Reload for Development
```
npm run dev
```
Compile and Minify for Production
```
npm run build
```
后端  
1. 执行resouces目录下的sql脚本，创建表  
2. 将application.yml中的数据库配置改为自己的数据库配置  
3. 如果要实现注册功能需要再application.yml中配置自己的邮箱配置，还需要启动RocketMQ服务
  
在启动前需要启动redis服务，否则会报错