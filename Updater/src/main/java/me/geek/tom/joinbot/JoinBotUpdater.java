package me.geek.tom.joinbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

public class JoinBotUpdater {

    private static final URL VERSION_URL;
    private static final Gson GSON = new GsonBuilder().create();

    static {
        URL VERSION_URL1 = null;
        try {
            VERSION_URL1 = new URL("http://maven.tomthegeek.ml/me/geek/tom/JoinBot/maven-metadata.xml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        VERSION_URL = VERSION_URL1;
    }

    public static void main(String[] args) throws ParserConfigurationException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try {
            System.out.println("Checking for bot updates...");
            Document doc = builder.parse(VERSION_URL.openStream());
            Element root = doc.getDocumentElement();
            NodeList versioning = root.getElementsByTagName("versioning");
            if (versioning.getLength() != 1) {
                System.err.println("Invalid maven-metadata!");
                return;
            }
            Element version = (Element) versioning.item(0);
            String latestVersion = version.getElementsByTagName("latest").item(0).getTextContent();

            File botJar = new File("JoinBot.jar");
            URL latestVersionUrl = new URL(String.format("http://maven.tomthegeek.ml/me/geek/tom/JoinBot/%s/JoinBot-%s-all.jar", latestVersion, latestVersion));
            if (!botJar.exists()) {
                System.out.println("Bot JAR does not exist, downloading latest version...");
                FileUtils.copyURLToFile(latestVersionUrl, botJar);
            } else {
                System.out.println("Checking if we already have the latest version...");
                JarFile jar = new JarFile(botJar);
                InputStream in = jar.getInputStream(jar.getJarEntry("version.json"));
                JsonObject obj = GSON.fromJson(new InputStreamReader(in), JsonObject.class);
                String currentVersion = obj.get("version").getAsString();
                if (!currentVersion.equals(latestVersion)) {
                    System.out.println("Downloading update...");
                    FileUtils.copyURLToFile(latestVersionUrl, botJar);
                }
            }
            System.out.println("Update check complete, creating custom ClassLoader...");
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{ botJar.toURI().toURL() }, JoinBotUpdater.class.getClassLoader());
            System.out.println("Loading main class...");
            Class<?> main = Class.forName("me.geek.tom.joinbot.JoinBot", true, loader);
            Method mainMethod = main.getDeclaredMethod("main", String[].class);
            mainMethod.setAccessible(true);
            System.out.println("Launching main...");
            mainMethod.invoke(null, (Object) new String[0]);
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            System.err.println("Failed to load main!");
            e.printStackTrace();
        }
    }
}
