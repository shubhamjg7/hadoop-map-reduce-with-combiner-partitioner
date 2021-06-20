package com.rmit.student.s3806186.bigdata.Assignment1;

import java.io.IOException;
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

public class WordByVowelOrConsonant {
	
	// Logger
	private static final Logger LOG = Logger.getLogger(WordByVowelOrConsonant.class);
	
	// Mapper class
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			LOG.info("The mapper task of Shubham Gupta, s3806186");
			
			String strValue = value.toString();
			
			// Remove punctuation and convert to lower case
			strValue = strValue.replaceAll("\\p{Punct}|\\d", "").toLowerCase(); 
			
			StringTokenizer itr = new StringTokenizer(strValue);
			
			while (itr.hasMoreTokens()) {
				String inpWord = itr.nextToken();
				
				char firstc = inpWord.charAt(0); // First character of the word in consideration
				
				if (Config.VOWELS.indexOf(firstc) > -1 ) {
					// Word is vowel
					word.set(Config.VOWELKEY);
				} else if (Config.CONSONANTS.indexOf(firstc) > -1 ) {
					// Word is consonant
					word.set(Config.CONSONANTKEY);
				} else {
					continue;
				}
				
				context.write(word, one);
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
		Job job = Job.getInstance(conf, "Word count by vowel or consonant as first character");
		job.setJarByClass(WordByVowelOrConsonant.class);
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