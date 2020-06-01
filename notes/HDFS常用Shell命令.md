# HDFS 常用 shell 命令

**1. 显示当前目录结构**

```shell
# 显示当前目录结构
hadoop fs -ls  <path>
# 递归显示当前目录结构
hadoop fs -ls  -R  <path>
# 显示根目录下内容
hadoop fs -ls  /
```

**2. 创建目录**

```shell
# 创建目录
hadoop fs -mkdir  <path> 
# 递归创建目录
hadoop fs -mkdir -p  <path>  
```

**3. 删除操作**

```shell
# 删除文件
hadoop fs -rm  <path>
# 递归删除目录和文件
hadoop fs -rm -R  <path> 
```

**4. 从本地加载文件到 HDFS**

```shell
# 二选一执行即可
hadoop fs -put  [localsrc] [dst] 
hadoop fs - copyFromLocal [localsrc] [dst] 
```


**5. 从 HDFS 导出文件到本地**

```shell
# 二选一执行即可
hadoop fs -get  [dst] [localsrc] 
hadoop fs -copyToLocal [dst] [localsrc] 
```

**6. 查看文件内容**

```shell
# 二选一执行即可
hadoop fs -text  <path> 
hadoop fs -cat  <path>  
```

**7. 显示文件的最后一千字节**

```shell
hadoop fs -tail  <path> 
# 和Linux下一样，会持续监听文件内容变化 并显示文件的最后一千字节
hadoop fs -tail -f  <path> 
```

**8. 拷贝文件**

```shell
hadoop fs -cp [src] [dst]
```

**9. 移动文件**

```shell
hadoop fs -mv [src] [dst] 
```


**10. 统计当前目录下各文件大小**  
+ 默认单位字节  
+ -s : 显示所有文件大小总和，
+ -h : 将以更友好的方式显示文件大小（例如 64.0m 而不是 67108864）
```shell
hadoop fs -du  <path>  
```

**11. 合并下载多个文件**
+ -nl  在每个文件的末尾添加换行符（LF）
+ -skip-empty-file 跳过空文件

```shell
hadoop fs -getmerge
# 示例 将HDFS上的hbase-policy.xml和hbase-site.xml文件合并后下载到本地的/usr/test.xml
hadoop fs -getmerge -nl  /test/hbase-policy.xml /test/hbase-site.xml /usr/test.xml
```

**12. 统计文件系统的可用空间信息**

```shell
hadoop fs -df -h /
```

**13. 更改文件复制因子**
```shell
hadoop fs -setrep [-R] [-w] <numReplicas> <path>
```
+ 更改文件的复制因子。如果 path 是目录，则更改其下所有文件的复制因子
+ -w : 请求命令是否等待复制完成

```shell
# 示例
hadoop fs -setrep -w 3 /user/hadoop/dir1
```

**14. 权限控制**  
```shell
# 权限控制和Linux上使用方式一致
# 变更文件或目录的所属群组。 用户必须是文件的所有者或超级用户。
hadoop fs -chgrp [-R] GROUP URI [URI ...]
# 修改文件或目录的访问权限  用户必须是文件的所有者或超级用户。
hadoop fs -chmod [-R] <MODE[,MODE]... | OCTALMODE> URI [URI ...]
# 修改文件的拥有者  用户必须是超级用户。
hadoop fs -chown [-R] [OWNER][:[GROUP]] URI [URI ]
```

**15. 文件检测**
```shell
hadoop fs -test - [defsz]  URI
```
可选选项：
+ -d：如果路径是目录，返回 0。
+ -e：如果路径存在，则返回 0。
+ -f：如果路径是文件，则返回 0。
+ -s：如果路径不为空，则返回 0。
+ -r：如果路径存在且授予读权限，则返回 0。
+ -w：如果路径存在且授予写入权限，则返回 0。
+ -z：如果文件长度为零，则返回 0。

```shell
# 示例
hadoop fs -test -e filename
```


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>