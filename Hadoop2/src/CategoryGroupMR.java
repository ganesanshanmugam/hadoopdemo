
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import mapper.MapperDemo;
import partitioner.CustomPartitioner;
import reducer.DefaultReducerDemo;

public class CategoryGroupMR {
	
	
	

	private static final String SPLIT_MAX_SIZE_BUFFER_SIZE = "33554432";
	private static final String SPLIT_MIN_SIZE_BUFFER_SIZE = "128";
	private static final String MAPRED_JOB_TRACKER = "mapred.job.tracker";
	private static final String HDFS_LOCALHOST_50001 = "hdfs://localhost:50001";
	private static boolean dsModeenabled = true;

	public static void main(String[] args) throws Exception {
		int numReduceTasks = (args.length >= 1) ? Integer.parseInt(args[0]) : 1;
		System.out.println("numReduceTasks::" + numReduceTasks);

		Configuration conf = new Configuration();

		if (dsModeenabled){
			conf.set(MAPRED_JOB_TRACKER, HDFS_LOCALHOST_50001);
			//Enable Split Logic
			//setSplitSize(conf);
		}

		Job job = new Job(conf, "Drug_Amount_Spent");

		if (dsModeenabled)
			job.setJarByClass(CategoryGroupMR.class);

		setMapOutputKey(job);
		setDefaultReducer(job);
		setDefaultMapper(job);
		

		if (numReduceTasks >= 3)
			setCustomPartitioner(job);

		setReducerCount(numReduceTasks, job);

		// default -- inputkey type -- longwritable: valuetype is text
		defaultInputOutputFormat(job);

		if (dsModeenabled) {
			setDistributedOutputParam(job);
		} else {
			setLocalOutputParam(job);
		}

		job.waitForCompletion(true);

	}

	private static void setSplitSize(Configuration conf) {
		conf.set("mapred.max.split.size",SPLIT_MAX_SIZE_BUFFER_SIZE);
//		conf.set("mapred.min.split.size",SPLIT_MIN_SIZE_BUFFER_SIZE);
	}

	private static void setLocalOutputParam(Job job) throws IOException {
		String input = "/Users/varshika/Ganesan/Hadoop/Workspace/data/data.txt";
		String output = "/Users/varshika/Ganesan/Hadoop/Workspace/data/out_" + System.currentTimeMillis();
		setPath(job, input, output);
	}

	private static void setDistributedOutputParam(Job job) throws IOException {
//		String input = "/data/data.txt";
		String input = "/data/bigdata.txt";
		String output = "/data/out_" + System.currentTimeMillis();
		setPath(job, input, output);
	}

	private static void setPath(Job job, String input, String output) throws IOException {
		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));
	}

	private static void setReducerCount(int numReduceTasks, Job job) {
		job.setNumReduceTasks(numReduceTasks);
	}

	private static void defaultInputOutputFormat(Job job) {
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
	}

	private static void setMapOutputKey(Job job) {
		// output key type in Mapper
		job.setMapOutputKeyClass(Text.class);
		// output value type in Mapper
		job.setMapOutputValueClass(IntWritable.class);

		// output key type in reducer
		job.setOutputKeyClass(Text.class);
		// output value type in reducer
		job.setOutputValueClass(IntWritable.class);
	}

	private static void setCustomPartitioner(Job job) {
		job.setPartitionerClass(CustomPartitioner.class);
	}

	private static void setDefaultReducer(Job job) {
		job.setReducerClass(DefaultReducerDemo.class);
		//Enable Combiner
		job.setCombinerClass(DefaultReducerDemo.class);

	}

	private static void setDefaultMapper(Job job) {
		job.setMapperClass(MapperDemo.class);		
	}

}
