package com.github.randerzander.perf;

import java.sql.*;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WriteThread implements Runnable {
  private int threadId;
  private HashMap<String, String> props;

  private Connection connection;
  private int count;
  private int repetitions;
  private int commitInterval;
  private PreparedStatement writeStatement;
  private PreparedStatement perfStatement;
  private ResultSetMetaData columns;
  private String verb;
  private boolean useCommits;

  boolean recordPerf;
  private String testName;
  private String metric;

  private int maxVal;

  public WriteThread(int threadId, HashMap<String, String> props){
    this.threadId = threadId;
    this.props = props;
    this.verb = (props.get("jdbc.url").contains("phoenix")) ? "upsert" : "insert";
    this.useCommits = !props.get("jdbc.url").contains("hive2");

    repetitions = Integer.parseInt(props.get("repetitions"));
    count = Integer.parseInt(props.get("writeCount"));
    commitInterval = Integer.parseInt(props.get("commitInterval"));

    recordPerf = props.get("recordPerf").equalsIgnoreCase("true");
    testName = props.get("testName");
    metric = "writes/sec";

    maxVal = Integer.parseInt(props.get("maxVal"));

    try{
      //connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      connection = DriverManager.getConnection(props.get("jdbc.url"));
      connection.setAutoCommit(props.get("autocommit").equals("true"));
    } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }

    String writeTable = props.get("writeTable");
    String writeColumns = props.get("writeColumns");
    String columnNames = "(";
    String values = "(";
    try {
      String prefix = (writeColumns == null) ? "select *" : "select " + columns;
      columns = connection.createStatement().executeQuery(prefix + " from " + writeTable + " limit 0").getMetaData();
      for (int i = 1; i <= columns.getColumnCount(); i++){
        columnNames += columns.getColumnName(i).split("\\.")[1] + ",";
        values += "?,";
      }
      columnNames = columnNames.substring(0, columnNames.length() - 1) + ")";
      values = values.substring(0, values.length() - 1) + ")";
      System.out.println(verb + " into " + writeTable + " " + columnNames + " values " + values);
      writeStatement = connection.prepareStatement(verb + " into " + writeTable + " " + columnNames + " values " + values);
      perfStatement = connection.prepareStatement("upsert into perf values (?, ?, ?, ?, ?, ?)");
    } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }
  }

  public void run(){
    System.out.println("WRITE THREAD " + threadId + " starting " + repetitions + " rounds of " + count + " writes = " + repetitions * count + " upserts, committing every " + commitInterval);

    long start = System.nanoTime();
    for (int i = 0; i < repetitions; i++){
      long t1 = System.nanoTime();
      for (int j = 0; j < count; j++) write(useCommits && (j % commitInterval == 0));
      perf(threadId, i, t1);
    }

    double seconds = (double)(System.nanoTime() - start) / 1000000000.0;
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    System.out.println("WRITE DONE: " + threadId + ": " + repetitions * count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(repetitions*count/seconds)) + " upserts/sec");
  }

  private static String CharSet = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ234567890!@#$";
  private String randString(int len) {
    String str = "";
    for (int a = 1; a <= Math.min(maxVal, randInt()); a++) {
      str += CharSet.charAt(new java.util.Random().nextInt(CharSet.length()));
    }
    return str;
  }

  private int randInt(){
    return ThreadLocalRandom.current().nextInt(0, 10000 + 1);
  }

  private void fillStatement(){
    try{
      for (int i = 1; i <= columns.getColumnCount(); i++){
        Object tmp = null;
        switch (columns.getColumnType(i)){
          case Types.BINARY:
          case Types.VARBINARY: tmp = randString(randInt()).getBytes(); break;

          case Types.CHAR:
          case Types.VARCHAR: tmp = randString(randInt()); break;

          case Types.DOUBLE:
          case Types.FLOAT:
          case Types.REAL:
          case Types.TINYINT:
          case Types.SMALLINT:
          case Types.INTEGER:
          case Types.BIGINT: tmp = randInt(); break;

          case Types.DATE:
          case Types.TIME:
          case Types.TIMESTAMP: now(); break;
          default: break;
        }
        writeStatement.setObject(i, tmp);
      }
    } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }
  }

  private void write(boolean commit){
    try{
      fillStatement();
      writeStatement.executeUpdate();
      if (commit) connection.commit();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  private Timestamp now(){
    long timeInMillis = System.currentTimeMillis();
    long timeInNanos = System.nanoTime();

    Timestamp ts = new Timestamp(timeInMillis);
    ts.setNanos((int) (timeInNanos % 1000000000));
    return ts;
  }

  private void perf(int threadId, int rep, long t1){
    double seconds = (double)(System.nanoTime() - t1) / 1000000000.0;
    //double seconds = (System.nanoTime() - t1) / 1000000000.0;
    if (recordPerf){
      try{
        perfStatement.setString(1, testName);
        perfStatement.setString(2, metric);
        perfStatement.setInt(3, threadId);
        perfStatement.setInt(4, rep);
        perfStatement.setObject(5, now());
        perfStatement.setObject(6, count/seconds);
        perfStatement.executeUpdate();
        connection.commit();
      } catch (SQLException e) { e.printStackTrace(); }
    }

    System.out.println("WRITE THREAD " + threadId + " rep " + rep + " of " + repetitions + ": " + count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(count/seconds)) + " upserts/sec");
  }

}
