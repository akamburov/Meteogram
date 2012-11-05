package net.kamburov.meteogram;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class MeteogramDownloadTask extends AsyncTask<String, Integer, Bitmap> {

    private ProgressDialog dialog;

    private Context context;

    private ImageView imageView;

    public MeteogramDownloadTask(Context context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "", context.getString(R.string.loading), true, false);
    }

    protected Bitmap doInBackground(final String... args) {
        try {
            String url = String.format(context.getString(R.string.location_url), args[0]);
            URLConnection connection = new URL(url).openConnection();
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        dialog.dismiss();

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_menu_close_clear_cancel);
        }

        super.onPostExecute(bitmap);
    }
}
