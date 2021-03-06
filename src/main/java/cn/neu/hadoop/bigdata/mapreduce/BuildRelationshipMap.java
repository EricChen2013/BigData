package cn.neu.hadoop.bigdata.mapreduce;

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
import java.util.Map;

@Component
@Slf4j
public class BuildRelationshipMap {
    public static class NameMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] name_and_count = value.toString().split("\t");
            String[] name = name_and_count[0].split(",");
            context.write(new Text(name[0]), new Text(name[1] + ':' + name_and_count[1]));
            context.write(new Text(name[1]), new Text(name[0] + ':' + name_and_count[1]));
        }
    }

    public static class CountRelationshipReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            HashMap<String, Integer> relationship_count = new HashMap<>();
            int sum = 0;
            for (Text i : values) {
                String[] name_count = i.toString().split(":");
                int count = Integer.valueOf(name_count[1]);
                relationship_count.put(name_count[0], count);
                sum += count;
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> i : relationship_count.entrySet()) {
                sb.append(i.getKey()).append(':').append((float) i.getValue() / sum);
                sb.append(';');
            }
            String result = sb.toString();
            context.write(key, new Text(result.substring(0, result.length() - 1)));
        }
    }

    public static void main(String in_path, String out_path, String name_node) throws IOException, InterruptedException, ClassNotFoundException {
        Job job = Job.getInstance();
        job.setJarByClass(BuildRelationshipMap.class);
        job.setMapperClass(NameMapper.class);
        job.setReducerClass(CountRelationshipReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(name_node + in_path));
        FileOutputFormat.setOutputPath(job, new Path(name_node + out_path));
        job.waitForCompletion(true);
    }
}