package cn.neu.hadoop.bigdata.web;

import cn.neu.hadoop.bigdata.HadoopTemplate;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@Controller
@Slf4j
public class WebController {
    @Autowired
    private HadoopTemplate hadoopTemplate;
    private static String tem_file_save_path = "C:/tem/";

    @RequestMapping(value = "/")
    public String index() {
        return "index.html";
    }

    @ResponseBody
    @RequestMapping(value = "/fs")
    public String get_json_from_hdfs(@RequestParam(name = "path") String path) {
        JsonObject response_json = new JsonObject();
        if (path != null) {
            try {
                boolean if_dir = hadoopTemplate.existDir(path, false);

                if (if_dir) {
                    if (!hadoopTemplate.existsFile(path)) throw new Exception("文件不存在");
                    JsonArray dir_filename_list = new JsonArray();
                    FileStatus[] test = hadoopTemplate.list(path);
                    for (FileStatus i : hadoopTemplate.list(path)) {
                        JsonObject tem = new JsonObject();
                        tem.addProperty("dir_or_file", i.isDirectory() ? "Dir" : "file");
                        String tem_path = i.getPath().toString();
                        tem.addProperty("path", tem_path.substring(tem_path.indexOf("/", 7)));
                        dir_filename_list.add(tem);
                    }
                    response_json.add("content", dir_filename_list);
                    response_json.addProperty("status", "dir");
                } else {
                    response_json.addProperty("content", hadoopTemplate.read(true, path));
                    response_json.addProperty("status", "file");
                }
            } catch (Exception e) {
                response_json.addProperty("status", "wrong");
                response_json.addProperty("content", e.toString());
            }
        } else {
            response_json.addProperty("status", "wrong");
            response_json.addProperty("content", "can't get parameter \"path\" or \"dir_or_file\"");
        }
        return response_json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/upload")
    public String upload(@RequestBody JSONObject post_json) {
        try {
            hadoopTemplate.write(post_json.getString("filepath"), post_json.getString("content"));
            return "文件上传成功";
        } catch (Exception e) {
            return "文件保存失败 " + e.toString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/mk")
    public String mk(@RequestParam(name = "dir_name") String path) {
        try {
            hadoopTemplate.existDir(path, true);
            return "文件夹创建成功";
        } catch (Exception e) {
            return "文件夹创建失败 " + e.toString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/rm")
    public String rm(@RequestParam(name = "delete_list") String delete_list) {
        try {
            for (String i : delete_list.split(",")) {
                hadoopTemplate.my_rm(i);
            }
            return "删除成功";
        } catch (Exception e) {
            return "删除失败 " + e.toString();
        }
    }

    @RequestMapping(value = "/download")
    public void download(HttpServletResponse response, @RequestParam(name = "download_filename") String download_filename_path) {
        try {
            String download_filename = download_filename_path.substring(download_filename_path.lastIndexOf('/'));
            File file = new File(tem_file_save_path + download_filename);
            if (!file.exists()) hadoopTemplate.download(download_filename_path, tem_file_save_path);

            response.setContentType("application/force-download");// 设置强制下载不打开
            //response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);// 设置文件名
            //response.setContentType("multipart/form-data;charset=UTF-8");也可以明确的设置一下UTF-8，测试中不设置也可以。
            response.setHeader("Content-Disposition", "attachment;fileName=" + new String(download_filename.getBytes("GB2312"), "ISO-8859-1"));
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}