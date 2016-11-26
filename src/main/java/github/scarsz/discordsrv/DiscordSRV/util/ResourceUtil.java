package github.scarsz.discordsrv.DiscordSRV.util;

import com.google.common.io.Resources;
import github.scarsz.discordsrv.DiscordSRV.Manager;
import org.apache.commons.io.IOUtils;

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
            System.out.println(Resources.getResource(resourceName));
            System.out.println(Manager.class.getResource(resourceName));
            return IOUtils.toString(ResourceUtil.class.getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
