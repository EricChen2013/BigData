package cn.neu.hadoop.bigdata.LPA;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


@Component
@Slf4j
public class LPACompute {
    private static String input_path = "/test/input";
    private static String output_path = "/test/output4";
    private static String tmp_output_path = "/test/tmp/pagerank/";
    private static int tmp_count = 0;

    public static class LPAIterMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] key_value = value.toString().split("\t");
            String[] name_and_label = key_value[0].split("#");
            String[] PR_and_name_list = key_value[1].split("#");
            String[] chain_name_and_weight_list = PR_and_name_list[1].split(";");

            // PR值和人物关系权重不作改变，使用$做不同map的区分
            context.write(new Text(key_value[0]), new Text('$' + key_value[1]));

            // 输出 <链出人物名，人物名#人物标签#关系边权重>
            for (String i : chain_name_and_weight_list) {
                String[] name_and_weight = i.split(":");
                context.write(new Text(name_and_weight[0]), new Text(name_and_label[0] + '#' +
                        name_and_label[1] + '#' + name_and_weight[1]));
            }
        }
    }

    public static class LPAIterReduce extends Reducer<Text, Text, Text, Text> {
        static HashMap<String, String> name_label = new HashMap<>();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String PR_name_list = "";
            Integer max_weight = 0;
            List<String> max_weight_label = new LinkedList<>();
            HashMap<String, Integer> label_weight_sum = new HashMap<>();
            for (Text i : values) {
                String for_i_str = i.toString();
                if (for_i_str.startsWith("$")) {
                    PR_name_list = for_i_str.substring(1);
                } else {
                    String[] name_label_weight = for_i_str.split("#");
                    if (name_label.containsKey(name_label_weight[0])) {
                        name_label_weight[1] = name_label.get(name_label_weight[0]);
                    }
                    Integer weight = 0;
                    if (label_weight_sum.containsKey(name_label_weight[1])) {
                        weight = label_weight_sum.get(name_label_weight[1]);
                    }
                    weight += Integer.valueOf(name_label_weight[2]);
                    label_weight_sum.put(name_label_weight[1], weight);

                    if (weight > max_weight) {
                        max_weight = weight;
                        max_weight_label.clear();
                        max_weight_label.add(name_label_weight[1]);
                    } else if (weight.equals(max_weight)) {
                        max_weight_label.add(name_label_weight[1]);
                    }
                }
            }

            String update_label = max_weight_label.get((int) (Math.random() * max_weight_label.size()));
            name_label.put(key.toString(), update_label);
            context.write(new Text(key.toString() + '#' + update_label), new Text(PR_name_list));
        }
    }

    public static void main(int repeat_time, String name_node) throws InterruptedException, IOException, ClassNotFoundException {
        LPAInit.main(name_node + input_path, name_node + tmp_output_path + tmp_count);
        tmp_count++;

        while (repeat_time > 0) {
            Job job = Job.getInstance();
            job.setJarByClass(LPACompute.class);
            job.setMapperClass(LPAIterMapper.class);
            job.setReducerClass(LPAIterReduce.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setNumReduceTasks(1);//设置reduce的个数
            FileInputFormat.addInputPath(job, new Path(name_node + tmp_output_path + (tmp_count - 1)));
            FileOutputFormat.setOutputPath(job, new Path(name_node + tmp_output_path + tmp_count));
            job.waitForCompletion(true);
            repeat_time--;
            tmp_count++;
        }
        LPAReorganize.main(name_node + tmp_output_path + (tmp_count - 1), name_node + output_path);
    }

    public static void main(String in_path, String out_path, int repeat_time, String name_node) throws IOException, InterruptedException, ClassNotFoundException {
        input_path = in_path;
        output_path = out_path;
        main(repeat_time, name_node);
    }
}