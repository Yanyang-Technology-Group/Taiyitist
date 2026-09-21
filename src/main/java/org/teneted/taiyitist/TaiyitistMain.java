package org.teneted.taiyitist;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TaiyitistMain {

    public static void main(String[] args) throws Throwable {
        // Apply input switches before Log4j initializes the shared terminal.
        for (String arg : args) {
            if (arg.equals("--nojline") || arg.equals("-nojline")
                    || arg.equals("--noconsole") || arg.equals("-noconsole")) {
                System.setProperty("terminal.jline", "false");
            }
        }
        System.setProperty("fabric.skipMcProvider", "true");
        System.setProperty("taiyitist.alwaysExtract", "true");
        try {
            var install = fabricInstall();
            var ours = TaiyitistMain.class.getProtectionDomain().getCodeSource().getLocation();
            var classloader = new URLClassLoader(Stream.concat(Stream.of(ours), install.getValue().stream().map(it -> {
                try {
                    return it.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            })).toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
            Thread.currentThread().setContextClassLoader(classloader);
            var cl = Class.forName(install.getKey(), false, classloader);
            var handle = MethodHandles.lookup().findStatic(cl, "main", MethodType.methodType(void.class, String[].class));
            handle.invoke((Object) args);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fail to launch Taiyitist.");
            System.exit(-1);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map.Entry<String, List<Path>> fabricInstall() throws Throwable {
        var path = Paths.get(".taiyitist", "gson.jar");
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.copy(Objects.requireNonNull(TaiyitistMain.class.getResourceAsStream("/gson.jar")), path);
        }
        try (var loader = new URLClassLoader(new URL[]{path.toUri().toURL(), TaiyitistMain.class.getProtectionDomain().getCodeSource().getLocation()}, ClassLoader.getPlatformClassLoader())) {
            var cl = loader.loadClass("org.teneted.taiyitist.install.FabricInstaller");
            var handle = MethodHandles.lookup().findStatic(cl, "applicationInstall", MethodType.methodType(Map.Entry.class));
            return (Map.Entry<String, List<Path>>) handle.invoke();
        }
    }
}
