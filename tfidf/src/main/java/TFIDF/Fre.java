package TFIDF;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Fre extends Configured implements Tool {
	
	public static void main(String[] args) throws Exception {
		int result = ToolRunner.run(new Configuration(), new Fre(), args);
		System.exit(result);
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		Job job = new Job(conf, "Word Frequency");

		job.setJarByClass(Fre.class);
		job.setMapperClass(FreMapper.class);
		job.setReducerClass(FreReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		// 직접 입력받음
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class FreMapper extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		public static final IntWritable ONE = new IntWritable(1);
		private static final Pattern PATTERN = Pattern.compile("\\w+");

		/*
		 * @input: (docid, contents)
		 * 
		 * @output: ((term, docid), 1)
		 */
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fileSplit = (FileSplit) context.getInputSplit();
			String docid = fileSplit.getPath().getName();

			Matcher m = PATTERN.matcher(value.toString());
			while (m.find()) {
				String term = m.group().toLowerCase();
				context.write(new Text(term + " " + docid), ONE);
			}
		}

	}

	public static class FreReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable tf = new IntWritable();

		/*
		 * @input: ((term, docid), [1,....])
		 * 
		 * @output: ((term, docid), tf)
		 */
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;

			for (IntWritable val : values)
				sum += val.get();

			tf.set(sum);
			context.write(key, tf);
		}

	}

}
