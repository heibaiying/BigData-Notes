import javafx.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 * @author : heibaiying
 * @description : 生成导航和图片格式转换
 */
public class Reformatting {

    /**
     * GITHUB 用户名
     **/
    private static final String GITHUB_USERNAME = "heibaiying";
    /**
     * 项目地址
     **/
    private static final String PROJECT_NAME = "BigData-Notes";

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("请输入文件路径");
            return;
        }

        String dir = "D:\\BigData-Notes\\notes\\Hbase协处理器.md";

        String preUrl = "https://github.com/" + GITHUB_USERNAME + "/" + PROJECT_NAME + "/blob/master/pictures/";
        String regex = "(!\\[(\\S*)]\\(\\S:\\\\" + PROJECT_NAME + "\\\\pictures\\\\(\\S*)\\)[^(</br>)]*?)";

        List<String> filesList = getAllFile(dir, new ArrayList<>());
        for (String filePath : filesList) {
            //  获取文件内容
            String content = getContent(filePath);
            // 修改图片
            String newContent = changeImageUrl(content, preUrl, regex);
            // 获取全部标题
            List<Pair<String, String>> allTitle = getAllTitle(newContent);
            // 生成导航
            String nav = genNav(allTitle);
            // 写出并覆盖原文件
            write(filePath, newContent, nav);
        }
        System.out.println("格式转换成功！");
    }


    private static String changeImageUrl(String content, String preUrl, String oldImageUrlRegex) {

        //github 支持的居中方式 <div align="center"> <img src=""/> </div>
        return content.replaceAll(oldImageUrlRegex,
                String.format("<div align=\"center\"> <img  src=\"%s$3\"/> </div>", preUrl));

    }

    private static List<String> getAllFile(String dir, List<String> filesList) {
        File file = new File(dir);
        //如果是文件 则不遍历
        if (file.isFile() && file.getName().endsWith(".md")) {
            filesList.add(file.getAbsolutePath());
        }
        //如果是文件夹 则遍历下面的所有文件
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    getAllFile(f.getAbsolutePath(), filesList);
                } else if (f.getName().endsWith(".md")) {
                    filesList.add(f.getAbsolutePath());
                }
            }
        }
        return filesList;
    }


    private static void write(String filePath, String content, String nav) {
        try {
            String newContent = "";
            if (content.contains("<nav>") && content.contains("</nav>")) {
                // 如果原来有目录则替换
                newContent = content.replaceAll("(?m)(<nav>[\\s\\S]*</nav>)", nav);
            } else {
                StringBuilder stringBuilder = new StringBuilder(content);
                // 如果原来没有目录，则title和正文一个标题间写入
                int index = content.indexOf("## ");
                stringBuilder.insert(index - 1, nav);
                newContent = stringBuilder.toString();
            }
            // 写出覆盖文件
            FileWriter fileWriter = new FileWriter(new File(filePath));
            fileWriter.write(newContent);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String genNav(List<Pair<String, String>> flagAndTitles) {
        StringBuilder builder = new StringBuilder();
        // 目录头
        builder.append("<nav>\n");
        for (Pair<String, String> ft : flagAndTitles) {
            String flag = ft.getKey();
            String title = ft.getValue();
            builder.append(genBlank(flag.length() - 2, 4));
            // Github有效目录格式: <a href="#21-预备">页面锚点</a>  url中不能出现特殊符号
            String formatTitle = title.trim().replaceAll("[.():：（）|、,，@。&/\\\\]", "").replace(" ", "-");
            builder.append(String.format("<a href=\"%s\">%s</a><br/>\n", "#" + formatTitle, title));
        }
        // 目录尾
        builder.append("</nav>\n");
        return builder.toString();
    }

    private static String genBlank(int i, int scale) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < i; j++) {
            for (int k = 0; k < scale; k++) {
                builder.append("&nbsp;");
            }
        }
        return builder.toString();
    }

    private static List<Pair<String, String>> getAllTitle(String content) {
        List<Pair<String, String>> list = new ArrayList<>();
        Pattern pattern = compile("(?m)^(#{2,10})\\s?(.*)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String group2 = matcher.group(2);
            if (!group2.contains("参考资料")) {
                list.add(new Pair<>(matcher.group(1), group2));
            }
        }
        return list;
    }

    private static String getContent(String filePath) {
        StringBuilder builder = new StringBuilder();

        try {
            FileReader reader = new FileReader(filePath);
            char[] chars = new char[1024 * 1024];

            int read;
            while ((read = reader.read(chars)) != -1) {
                builder.append(new String(chars, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

}
