package com.yooiistudios.coreutils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 7. 10.
 *
 * FileUtils
 * description
 */
public class CloseableUtils {
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) { }
        }
    }
}
