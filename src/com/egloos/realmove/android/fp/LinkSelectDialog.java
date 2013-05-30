
package com.egloos.realmove.android.fp;

import com.egloos.realmove.android.fp.model.Link;

import android.app.Dialog;
import android.content.Context;

public class LinkSelectDialog extends Dialog {

    private Link mLink;

    private LinkSelectDialog(Context context, Link link) {
        super(context);
        this.mLink = link;
    }

    public static void show(Context context, Link link) {
        new LinkSelectDialog(context, link).show();
    }

}
