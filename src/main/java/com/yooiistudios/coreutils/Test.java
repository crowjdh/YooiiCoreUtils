package com.yooiistudios.coreutils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Dongheyon Jeong in CoreUtils from Yooii Studios Co., LTD. on 15. 8. 19.
 *
 * Test
 * description
 */
public class Test {
    public static void showToast(Context context) {
        Toast.makeText(context, "This is core.", Toast.LENGTH_LONG).show();
    }
}
