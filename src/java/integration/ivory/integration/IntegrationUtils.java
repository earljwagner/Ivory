package ivory.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.hadoop.conf.Configuration;

public class IntegrationUtils {
  public static final String D_JT = "-Dmapred.job.tracker=bespin00.umiacs.umd.edu:8021";
  public static final String D_NN = "-Dfs.default.name=hdfs://bespin00.umiacs.umd.edu:8020";

  public static final String D_JT_LOCAL = "-D mapred.job.tracker=local";
  public static final String D_NN_LOCAL = "-D fs.default.name=file:///";
   
  public static String getJar(String path, final String prefix) {
      File[] arr = new File(path).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(prefix) && !name.contains("javadoc") && !name.contains("sources");
      }
    });

    assertTrue(arr.length == 1);
    return arr[0].getAbsolutePath();
  }

  public static Configuration getBespinConfiguration() {
    Configuration conf = new Configuration();
    conf.set("mapred.job.tracker", "bespin00.umiacs.umd.edu:8021");
    conf.set("fs.default.name", "hdfs://bespin00.umiacs.umd.edu:8020");
    return conf;
  }
}
