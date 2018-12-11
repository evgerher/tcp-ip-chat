package server.commands;

import command.Command;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutor {

  private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

  private String commandsPath;

  public CommandExecutor() {
    String path = CommandExecutor.class.getClassLoader().getResource("nani.txt").getPath();
    this.commandsPath = path.substring(0, path.lastIndexOf("nani.txt")) + "jars/";
  }

  private List<Pair<String, String>> getCommandsList() {
    LinkedList<Pair<String, String>> res = new LinkedList<>();

    for (LinkedList<Class> classList : getClassesList()) {
      for (Class someClass : classList) {
        if (classImplementsInterface(someClass)) {
          try {
            Method methodK = someClass.getDeclaredMethod("getKeyword", null);
            String keyword = (String) methodK.invoke(someClass.newInstance(), null);
            Method methodD = someClass.getDeclaredMethod("getShortDescription", null);
            String description = (String) methodD.invoke(someClass.newInstance(), null);

            res.add(new Pair<>(keyword, description));
          } catch (InstantiationException | IllegalAccessException
              | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Error loading interface for /help command: " + e.getMessage());
          }
        }
      }
    }
    return res;
  }

  private String getHelp() {
    List<Pair<String, String>> res = getCommandsList();
    StringBuilder result = new StringBuilder(res.size() * 10);
    for (Pair<String, String> command : res) {
      result.append("\t").append(command.getKey()).append("\n\t").append(command.getValue())
          .append("\n\n");
    }
    return result.toString();
  }

  public String executeCommand(String keyword, String parameters) {
    if (keyword.equals("help")) {
      return getHelp();
    }

    for (LinkedList<Class> classList : getClassesList()) {
      for (Class someClass : classList) {
        if (classImplementsInterface(someClass)) {
          try {
            Method methodK = someClass.getDeclaredMethod("getKeyword", null);
            String currentKeyword =
                (String) methodK.invoke(someClass.newInstance(), null);

            if (currentKeyword.equals(keyword)) {
              Method methodE = someClass.getDeclaredMethod("executeCommand", String.class);
              return (String) methodE.invoke(someClass.newInstance(), parameters);
            }
          } catch (InstantiationException | IllegalAccessException
              | InvocationTargetException | NoSuchMethodException e) {
            logger.error("executing command with keyword: " + keyword
                + "\n error: " + e.getMessage());
          }
        }
      }
    }

    return null;
  }

  private LinkedList<LinkedList<Class>> getClassesList() {
    LinkedList<LinkedList<Class>> res = new LinkedList<>();
    File dir = new File(commandsPath);
    File[] jars = dir.listFiles();

    if (jars == null) {
      return res;
    }

    for (File jar : jars) {
      res.add(new LinkedList<Class>());
      JarFile jarFile = null;
      try {
        jarFile = new JarFile(jar.getAbsolutePath());
        Enumeration<JarEntry> e = jarFile.entries();

        URL[] urls = {new URL("file:" + jar.getAbsolutePath())};
        URLClassLoader cl = new URLClassLoader(urls);

        while (e.hasMoreElements()) {
          JarEntry je = e.nextElement();
          if (je.isDirectory() || !je.getName().endsWith(".class")) {
            continue;
          }
          // -6 because of .class
          String className = je.getName().substring(0, je.getName().length() - 6);
          className = className.replace('/', '.');
          Class c = cl.loadClass(className);
          res.getLast().add(c);
        }
        jarFile.close();
        cl.close();
      } catch (ClassNotFoundException | IOException ex) {
        logger.error("loading classes from jar ({}: {})", jarFile.getName(), ex.getMessage());
      }
    }
    return res;
  }

  private boolean classImplementsInterface(Class c) {
    boolean res = false;
    String name = Command.class.getSimpleName();
    for (Class interf : c.getInterfaces()) {
      if (interf.getSimpleName().equals(name)) {
        res = true;
      }
    }
    return res;
  }
}
