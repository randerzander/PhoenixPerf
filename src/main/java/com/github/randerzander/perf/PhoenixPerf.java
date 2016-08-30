package com.github.randerzander.perf;

import java.sql.*;
import org.apache.phoenix.jdbc.PhoenixDriver;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PhoenixPerf {

  public static class TestThread implements Runnable {
    public int threadId;
    public HashMap<String, String> props;

    public TestThread(int threadId, HashMap<String, String> props){
      this.threadId = threadId; this.props = props;
    }

    public void run(){
      int repetitions = Integer.parseInt(props.get("repetitions"));
      int records = Integer.parseInt(props.get("records"));
      int commitInterval = Integer.parseInt(props.get("commitInterval"));
      String sql = "upsert into " + props.get("table") + " values (";

      try{
        Connection connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
        connection.setAutoCommit(props.get("autocommit").equals("true"));
        System.out.println("THREAD " + threadId + " starting " + repetitions + " rounds of " + records + " records = " + repetitions * records + " upserts, committing every " + commitInterval);

        long start = System.nanoTime();
        for (int i = 0; i < repetitions; i++){
          long t1 = System.nanoTime();
          for (int j = 0; j < records; j++){
            connection.createStatement().execute(sql + threadId + "," + i + "," + j + "," + t1 + ")");
            if (j % commitInterval == 0) connection.commit();
          }
          double seconds = (double)(System.nanoTime() - t1) / 1000000000.0;
          System.out.println("THREAD " + threadId + " rep " + i + " of " + repetitions + ": " + records + " / " + String.valueOf(seconds) + " = " + String.valueOf(records/seconds));
        }
        double seconds = (double)(System.nanoTime() - start) / 1000000000.0;

        Thread.sleep(5000);
        System.out.println("DONE: " + threadId + ": " + repetitions * records + " / " + String.valueOf(seconds) + " = " + String.valueOf(repetitions*records/seconds));
      } catch (Exception e) { e.printStackTrace(); }
    }
  }

  public static void main(String args[]){
    HashMap<String, String> props = getPropertiesMap(args[0]);

    try {
      //Load and run setup statements
      String preQueries = new Scanner(new File(props.get("preQueryFile"))).useDelimiter("\\Z").next();
      Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
      Connection connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      for (String query : preQueries.split(";")){
        System.out.println("Running PreQuery: " + query);
        connection.createStatement().execute(query);
      }
    } catch (Exception ex) {
      System.out.println("Error: unable to load driver and run preQueries: " + ex.toString());
      System.exit(1);
    }
    for (int i = 0; i < Integer.parseInt(props.get("threads")); i++){
      Runnable r = new TestThread(i, props);
      new Thread(r).start();
    }
  }

  public static HashMap<String, String> getPropertiesMap(String file){
    Properties props = new Properties();
    try{ props.load(new FileReader(file)); }
    catch(Exception e){ e.printStackTrace(); System.exit(-1); }

    HashMap<String, String> map = new HashMap<String, String>();
    for (final String name: props.stringPropertyNames()) map.put(name, (String)props.get(name));
    return map;
  }

}
