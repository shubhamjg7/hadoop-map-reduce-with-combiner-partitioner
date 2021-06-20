package com.rmit.student.s3806186.bigdata.Assignment1;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class WordByLengthPartitioned {
	
	// Logger
	private static final Logger LOG = Logger.getLogger(WordByLengthPartitioned.class);
	
	// Mapper class
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		
		// Writables
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		// Map method
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			LOG.info("The mapper task of Shubham Gupta, s3806186");
			
			String strValue = value.toString();
			
			// Remove punctuation and convert to lower case
			strValue = strValue.replaceAll("\\p{Punct}|\\d", "").toLowerCase(); 
			
			StringTokenizer itr = new StringTokenizer(strValue);
			
			while (itr.hasMoreTokens()) {
				String inpWord = itr.nextToken();
				
				if(inpWord.length() < 5) { // Word length 1-4
					word.set(Config.SHORTKEY);
				} else if(inpWord.length() < 8) { // Word length 5-7
					word.set(Config.MEDIUMKEY);
				} else if(inpWord.length() < 11) { // Word length 8-10
					word.set(Config.LONGKEY);
				} else { // Word length 11 or more
					word.set(Config.EXTRALONGKEY);
				}
				
				context.write(word, one);
			}
		}
	}
	
	// Partitioner class
	public static class WordLengthPartitioner extends Partitioner <Text, IntWritable>  {
		
		// Partition method
		public int getPartition(Text key, IntWritable value, int numReduceTasks) {
			LOG.info("The partitioner task of Shubham Gupta, s3806186");
			
			// If we have only one reduce task
			if(numReduceTasks == 0) { 
				return 0;
			}
			
			String keyStr = key.toString();
			
			if(keyStr.equals(Config.SHORTKEY) || keyStr.equals(Config.EXTRALONGKEY)) {
				// For short and extra long key use reducer 0
				return 0;
			} else {
				// For short and extra long key use reducer 1
				return 1 % numReduceTasks;
			}
		}
	}
	
	// Reducer class
	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		// Writables
		private IntWritable result = new IntWritable();

		// Reduce method
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			LOG.info("The reducer task of Shubham Gupta, s3806186");
			
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		LOG.setLevel(Config.logLevel); // Set log level
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Word count by length partitioned");
		job.setJarByClass(WordByLengthPartitioned.class);
		job.setMapperClass(TokenizerMapper.class); // Setting mapper
		job.setPartitionerClass(WordLengthPartitioner.class); // Setting partitioner
		job.setNumReduceTasks(2); // Setting number of reduce tasks
		job.setCombinerClass(IntSumReducer.class); // Setting combiner
		job.setReducerClass(IntSumReducer.class); // Setting reducer
		job.setOutputKeyClass(Text.class); // Setting op key type
		job.setOutputValueClass(IntWritable.class); // Setting op value type
		FileInputFormat.addInputPath(job, new Path(args[0])); // Use passed input path 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // Use passsed output path
		LOG.info("INPUT PATH: "+args[0]);
		LOG.info("OUTPUT PATH: "+args[1]);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}