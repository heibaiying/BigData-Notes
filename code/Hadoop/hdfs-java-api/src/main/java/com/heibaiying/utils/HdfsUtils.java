package com.heibaiying.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HDFS 工具类
 */
public class HdfsUtils {


    private static final String HDFS_PATH = "hdfs://192.168.0.107:8020";
    private static final String HDFS_USER = "root";
    private static FileSystem fileSystem;

    static {

        try {
            Configuration configuration = new Configuration();
            configuration.set("dfs.replication", "1");
            fileSystem = FileSystem.get(new URI(HDFS_PATH), configuration, HDFS_USER);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public static FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * 创建目录 支持递归创建
     *
     * @param path 路径地址
     * @return 创建是否成功
     */
    public static boolean mkdir(String path) throws Exception {
        return fileSystem.mkdirs(new Path(path));
    }

    /**
     * 查看文件内容
     *
     * @param path 路径地址
     * @return 返回文件内容字符串
     */
    public static String text(String path, String encode) throws Exception {
        FSDataInputStream inputStream = fileSystem.open(new Path(path));
        return inputStreamToString(inputStream, encode);
    }


    /**
     * 创建文件并写入内容
     *
     * @param path 路径地址
     * @param context    文件内容
     */
    public void createAndWrite(String path, String context) throws Exception {
        FSDataOutputStream out = fileSystem.create(new Path(path));
        out.write(context.getBytes());
        out.flush();
        out.close();
    }

    /**
     * 文件重命名
     *
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @return 重命名是否成功
     */
    public boolean rename(String oldPath, String newPath) throws Exception {
        return fileSystem.rename(new Path(oldPath), new Path(newPath));

    }


    /**
     * 拷贝文件到HDFS
     *
     * @param localPath 本地文件路径
     * @param hdfsPath  存储到hdfs上的路径
     */
    public void copyFromLocalFile(String localPath, String hdfsPath) throws Exception {
        fileSystem.copyFromLocalFile(new Path(localPath), new Path(hdfsPath));
    }


    /**
     * 从HDFS下载文件
     *
     * @param hdfsPath  文件在hdfs上的路径
     * @param localPath 存储到本地的路径
     */
    public void copyToLocalFile(String hdfsPath, String localPath) throws Exception {
        fileSystem.copyToLocalFile(new Path(hdfsPath), new Path(localPath));
    }


    /**
     * 查询给定路径中文件/目录的状态
     *
     * @param path 目录路径
     * @return 文件信息的数组
     */
    public FileStatus[] listFiles(String path) throws Exception {
        return fileSystem.listStatus(new Path(path));
    }


    /**
     * 查询给定路径中文件的状态和块位置
     *
     * @param path 路径可以是目录路径也可以是文件路径
     * @return 文件信息的数组
     */
    public RemoteIterator<LocatedFileStatus> listFilesRecursive(String path, boolean recursive) throws Exception {
        return fileSystem.listFiles(new Path(path), recursive);
    }


    /**
     * 查看文件块信息
     *
     * @param path 文件路径
     * @return 块信息数组
     */
    public BlockLocation[] getFileBlockLocations(String path) throws Exception {
        FileStatus fileStatus = fileSystem.getFileStatus(new Path(path));
        return fileSystem.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return 删除是否成功
     */
    public boolean delete(String path) throws Exception {
        return fileSystem.delete(new Path(path), true);
    }


    /**
     * 把输入流转换为指定字符
     *
     * @param inputStream 输入流
     * @param encode      指定编码类型
     */
    private static String inputStreamToString(InputStream inputStream, String encode) {
        try {
            if (encode == null || ("".equals(encode))) {
                encode = "utf-8";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encode));
            StringBuilder builder = new StringBuilder();
            String str = "";
            while ((str = reader.readLine()) != null) {
                builder.append(str).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
