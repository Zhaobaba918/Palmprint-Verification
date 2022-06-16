package top.sun1999;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddPalmActicity extends AppCompatActivity {
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ImageView resultImageView;
    private TextView thresholdTextview;
    private TextView tvInfo;
    private Button backButton;


    private final double threshold = 0.35;
    private final double nms_threshold = 0.7;
    private float[] myVec;
    private Module module;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, PERMISSIONS, PackageManager.PERMISSION_GRANTED);
        while ((ContextCompat.checkSelfPermission(this.getApplicationContext(), PERMISSIONS[0]) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this.getApplicationContext(), PERMISSIONS[1]) == PackageManager.PERMISSION_DENIED)) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        setContentView(R.layout.activity_add_palm_acticity);
        YOLOv4.init(getAssets());

        resultImageView = findViewById(R.id.imageView);
        thresholdTextview = findViewById(R.id.valTxtView);
        tvInfo = findViewById(R.id.tv_info);
        backButton = findViewById(R.id.button);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("文件创建失败", ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "top.sun1999.fileProvider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }

        backButton.setOnClickListener(view -> {
            finish();
        });

        try {
            module = Module.load(assetFilePath(this, "best.pt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap image = byteToBitmap(getPhotoByUrl(currentPhotoPath));

        long startTime = System.currentTimeMillis();
        Box[] result = YOLOv4.detect(image, threshold, nms_threshold);
        Bitmap tmpimage = Util.extractROI(image, result);
        if (tmpimage == null) {
            tvInfo.setText("Yolo 识别失败");
            resultImageView.setImageBitmap(image);
            return;
        }
        resultImageView.setImageBitmap(tmpimage);

        // preparing input tensor
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(tmpimage,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);

        // running the model
        final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();


        long endTime;
        myVec = outputTensor.getDataAsFloatArray();

        float sum = 0;
        for (float tmp : myVec) {
            sum += tmp * tmp;
        }
        sum = (float) Math.sqrt(sum);
        for (int i = 0; i < myVec.length; i++) {
            myVec[i] /= sum;
        }
        endTime = System.currentTimeMillis();
        Log.e("myVec", myVec.toString());

        inputName();



        tvInfo.setText(String.format(Locale.CHINESE,
                "ImgSize: %dx%d\nUseTime: %d ms\n特征向量已保存",
                image.getHeight(), image.getWidth(), endTime - startTime));
    }

    String name;

    public void inputName()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置名称");
        final EditText et = new EditText(this);
        et.setHint("请输入指纹名称");
        et.setSingleLine(true);
        builder.setView(et);
//        builder.setNegativeButton("取消",null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String palmName = et.getText().toString();
                thresholdTextview.setText(palmName);
                name = palmName;
                Util.vecs.put(name,myVec);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    public static byte[] getPhotoByUrl(String photoUrl) {
        File file = new File(photoUrl);
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] temp = new byte[1024];
            int size = 0;
            while ((size = fis.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
            fis.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap byteToBitmap(byte[] imgByte) {
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        input = new ByteArrayInputStream(imgByte);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));  //软引用防止OOM
        bitmap = (Bitmap) softRef.get();
        if (imgByte != null) {
            imgByte = null;
        }

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // 异常捕获
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        int rotate = readPictureDegree(picturePath);
        return rotateBitmapByDegree(bitmap, rotate);
    }


    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (name!=null){
            JSONObject jsonObject = new JSONObject(Util.vecs);
            Log.e("palms",jsonObject.toString());
        }

    }

}