package de.vipherian.imagine.loader;

import de.vipherian.imagine.api.Image;
import de.vipherian.imagine.api.ImageInfo;
import de.vipherian.imagine.api.Initialization;
import de.vipherian.imagine.api.InjectField;
import de.vipherian.octulus.commons.Files;
import de.vipherian.octulus.commons.exception.SecureException;
import de.vipherian.octulus.reflection.JarReflection;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public final class ImageLoader {

    private static final File imageFolder = Files.of("./images");

    static {
        if(!imageFolder.exists()){
            Files.createDictionary(imageFolder);
        }
    }

    public static void loadImages(){
        var files = Arrays.stream(Objects.requireNonNull(imageFolder.listFiles())).filter(file -> file.getName().endsWith(".jar")).toList();

        files.forEach(file -> {
            SecureException.secureAndPrint(() -> {
                var classes = JarReflection.listClassesAsList(file);

                for (var clazz : classes) {
                    if(clazz.isAnnotationPresent(Image.class)){
                        var instance = clazz.getConstructor().newInstance();
                        var methods = clazz.getDeclaredMethods();
                        var fields = clazz.getDeclaredFields();

                        var info = new ImageInfo(UUID.randomUUID(), file);

                        for (var field : fields) {
                            if(field.isAnnotationPresent(InjectField.class)){
                                var type = field.getType();

                                if(ImageInfo.class.equals(type)){
                                    field.setAccessible(true);
                                    field.set(info, info);
                                }
                            }
                        }

                        for (var method : methods) {
                            if(method.isAnnotationPresent(Initialization.class)){
                                method.setAccessible(true);
                                method.invoke(instance);
                            }
                        }
                    }
                }
            });
        });
    }

}
