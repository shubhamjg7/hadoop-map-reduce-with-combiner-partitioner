package com.rmit.student.s3806186.bigdata.Assignment1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class WordByCountInMapCombine {
	
	// Logger
	private static final Logger LOG = Logger.getLogger(WordByCountInMapCombine.class);
	
	// Mapper class
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		
		// Map for intermediate key and value pairs before cleanup
		Map<String, Integer> keyValMap = new HashMap<String, Integer>();

		// Map method
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			LOG.info("The mapper task of Shubham Gupta, s3806186");
			
			String strValue = value.toString();
			
			// Remove punctuation and convert to lower case
			strValue = strValue.replaceAll("\\p{Punct}|\\d", "").toLowerCase(); 
			
			StringTokenizer itr = new StringTokenizer(strValue);
			
			while (itr.hasMoreTokens()) {
			
				String inpWord = itr.nextToken();

				if(keyValMap.containsKey(inpWord)) { // Key present value needs to be incremented
					int sum = (int) keyValMap.get(inpWord) + 1;
					keyValMap.put(inpWord, sum);
				} else {
					keyValMap.put(inpWord, 1); // Key encountered for the first time
				}
			}
		}
		
		// Cleanup for in map combined output
		public void cleanup(Context context) throws IOException, InterruptedException {
			LOG.info("The in mapper cleaning task of Shubham Gupta, s3806186");
			
			// Iterate all entries in key value map and write to context
			Iterator<Map.Entry<String, Integer>> iter = keyValMap.entrySet().iterator();
			while(iter.hasNext()) { 
				Map.Entry<String, Integer> keyValPair = iter.next();
				
				String key = keyValPair.getKey();
	            Integer val = keyValPair.getValue();
	            
	            context.write(new Text(key), new IntWritable(val)); // Write key values aggregated by mapper to context
			}
		}
	}
	
	// Reducer
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
		Job job = Job.getInstance(conf, "Word count with in mapper combiner");
		job.setJarByClass(WordByCountInMapCombine.class);
		job.setMapperClass(TokenizerMapper.class); // Setting mapper
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