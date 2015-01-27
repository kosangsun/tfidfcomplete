package TFIDF;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

public class Tfidf extends Configured implements Tool{

	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		Configuration conf = getConf();
		FileSystem fs = FileSystem.get(conf);
		
		//셰익스피어 밑에 문서개수
		FileStatus[] listStatus = fs.listStatus(new Path(args[0]));
		conf.setInt("N", listStatus.length);
		
		Job job = new Job(conf, "TFIDF");
		
		job.setJarByClass(Tfidf.class);
		job.setMapperClass(TfidfMapper.class);
		job.setReducerClass(TfidfReducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		TextInputFormat.addInputPath(job, new Path(args[1]));
		TextOutputFormat.setOutputPath(job, new Path(args[2]));	
		
		
		return job.waitForCompletion(true) ? 0 : 1;
	}
	
	public static class TfidfMapper extends Mapper<LongWritable, Text, Text, Text>{
		private static int N;

		@Override
		protected void map(LongWritable key, Text value,
				Context context)
				throws IOException, InterruptedException {
			
			//공백으로 구분
			String[] fields = value.toString().split("\\s+");
			String term = fields[0];
			String docid = fields[1];
			int tf = Integer.parseInt(fields[2]);
			int n = Integer.parseInt(fields[3]);

			double idf = Math.log(N / n);
			double tfidf = tf * idf;

			context.write(new Text(term + "@" + docid),
					new Text(String.valueOf(tfidf)));
		}

		@Override
		protected void setup(Context context)
				throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			N = conf.getInt("N", 0);
		}
		
		
		
		
	}
	
	public static class TfidfReducer extends Reducer<Text, Text, Text, Text>{

		@Override
		protected void reduce(Text key, Iterable<Text> values,
				Context context)
				throws IOException, InterruptedException {
			for (Text value : values)
				context.write(key, value);
		}
		
		
	}
}
