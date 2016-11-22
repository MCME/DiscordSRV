package github.scarsz.discordsrv.DiscordSRV.util;

import com.google.common.io.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class ResourceUtil {

    public static void copyResourceToFile(String resourceName, File file) {
        try {
            Resources.copy(Resources.getResource(resourceName), new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getResourceAsString(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}