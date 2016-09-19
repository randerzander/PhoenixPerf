package com.github.randerzander.perf;

import com.github.randerzander.perf.WriteThread;
import com.github.randerzander.perf.ReadThread;

import java.sql.*;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PhoenixPerf {

  public static void main(String args[]){
    HashMap<String, String> props = getPropertiesMap(args[0]);

    try {
      System.out.println("Opening connection to: " + props.get("jdbc.url"));
      Connection connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      //Load and run setup statements
      if (props.get("setupQueryDir") != null){
        for (File file : new File(props.get("setupQueryDir")).listFiles()) {
            System.out.println("Running setup file: " + file.getPath());
            try{
              for (String statement : new Scanner(file).useDelimiter("\\Z").next().split(";")){
                System.out.println("Running: \n" + statement);
                connection.createStatement().execute(statement);
              }
            } catch (java.io.FileNotFoundException e) { e.printStackTrace(); System.exit(-1); }
        }
      }
    } catch (Exception ex) {
      System.out.println("Error: unable to load driver and run setup queries: " + ex.toString());
      System.exit(1);
    }
    if (props.get("writeThreads") != null)
      for (int i = 0; i < Integer.parseInt(props.get("writeThreads")); i++){
        Runnable r = new WriteThread(i, props);
        new Thread(r).start();
      }
    if (props.get("readThreads") != null)
      for (int i = 0; i < Integer.parseInt(props.get("readThreads")); i++){
        Runnable r = new ReadThread(i, props);
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
