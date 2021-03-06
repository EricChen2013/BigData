package cn.neu.hadoop.bigdata.mapreduce.pagerank;

import cn.neu.hadoop.bigdata.mapreduce.mydatatypes.DesFloatWritable;
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


@Component
@Slf4j
public class PageRankViewer {
    public static class PageRankViewerMapper extends Mapper<Object, Text, DesFloatWritable, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] name_and_point_relationship_list = value.toString().split("\t");
            String[] point_and_relationship_list = name_and_point_relationship_list[1].split("#");
            float point = Float.valueOf(point_and_relationship_list[0]);

            context.write(new DesFloatWritable(point), new Text(name_and_point_relationship_list[0]));
        }
    }

    public static class PageRankViewerReduce extends Reducer<DesFloatWritable, Text, DesFloatWritable, Text> {
        public void reduce(DesFloatWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text i : values) {
                context.write(key, i);
            }
        }
    }

    public static void main(String input_path, String output_path) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = Job.getInstance();
        job.setJarByClass(PageRankViewer.class);
        job.setMapperClass(PageRankViewerMapper.class);
        job.setReducerClass(PageRankViewerReduce.class);
        job.setOutputKeyClass(DesFloatWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(input_path));
        FileOutputFormat.setOutputPath(job, new Path(output_path));
        job.waitForCompletion(true);
    }
}

