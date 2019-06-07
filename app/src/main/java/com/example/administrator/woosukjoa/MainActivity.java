package com.example.administrator.woosukjoa;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Semaphore;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "woosukjoa";
    private final Semaphore writeLock = new Semaphore(1);

    private Boolean isPermission = true;
    private Boolean isCamera = false;

    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    private File tempFile;

    public native long loadCascade(String cascadeFileName);

    public native long detect(long cascadeClassifier_face, long matAddrInput, long matAddrResult, long matGray);

    public long cascadeClassifier_face = 0;

    private Mat matInput;
    private Mat matResult;
    private Mat matGray;
    private int arrayFace[] = new int[96*96];
    //--Number of Detectiong face--
    public long dfn = 0;

    TextView txtResult;
    TextView tv_result;
    ImageView emotion_view;
    Drawable e_drawable;


    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_get_image);

        txtResult = (TextView) findViewById(R.id.txtResult);

        tedPermission();

        findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if (isPermission) {
                    read_cascade_file();
                    goToAlbum();
                } else
                    Toast.makeText(view.getContext(), "사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btnCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if (isPermission) {
                    read_cascade_file();
                    takePhoto();
                } else
                    Toast.makeText(view.getContext(), "사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
            return;
        }
        if (requestCode == 3000) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                String result = data.getStringExtra("result");
                txtResult.setText(result);
            }
        }
        if (requestCode == 4000) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                String result = data.getStringExtra("result");
                txtResult.setText(result);
            }
        }

        //앨범에서 이미지를 가져오는 경우
        if (requestCode == PICK_FROM_ALBUM) {
            Uri photoUri = data.getData();
            Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

            Cursor cursor = null;
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

                Log.d(TAG, "tempFile Uri : " + Uri.fromFile(tempFile));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            setImage();
        } else if (requestCode == PICK_FROM_CAMERA) {
            setImage();
        }

    }

     //앨범에서 이미지 가져오기
    private void goToAlbum() {
        isCamera = false;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


     //카메라에서 이미지 가져오기
    private void takePhoto() {
        isCamera = true;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "{package name}.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {
                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        }
    }

    //폴더 및 파일 만들기
    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( woosukjoa_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "thefaceshot_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( woosukjoa )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/thefaceshot/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    private void setImage() {
        ImageView imageView = findViewById(R.id.imageView);

        //temp 파일의 크기를 넓이 1280으로 리사이징 합니다.
        ImageResizeUtils.resizeFile(tempFile, tempFile, 1280, isCamera);
        //bimap input 변수에 tempFile을 담습니다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmapInput = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        //bmp32로 저장
        Bitmap bmp32 = bitmapInput.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap bmp32_f = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);

        matInput = new Mat();
        //bmp32 이미지 파일을 Mat(matInput) 타입으로 저장
        Utils.bitmapToMat(bmp32, matInput);
        if (matResult == null) {
            matResult = new Mat(matInput.width(), matInput.height(), matInput.type());
        }
        matGray = new Mat(96, 96, COLOR_RGB2GRAY);
        dfn = detect(cascadeClassifier_face, matInput.getNativeObjAddr(), matResult.getNativeObjAddr(), matGray.getNativeObjAddr());

        tv_result = findViewById(R.id.tv_result);
        emotion_view = (ImageView) findViewById(R.id.imageView2);

        if (dfn == 1) { //dfn = 검출된 얼굴의 갯수. 얼굴 하나를 올바르게 인식 한 경우

            Utils.matToBitmap(matGray, bmp32_f);

            final int lnth=bmp32_f.getByteCount();
            ByteBuffer dst= ByteBuffer.allocate(lnth);
            bmp32_f.copyPixelsToBuffer(dst);
            byte[] barray=dst.array();

            float[] fArray = new float[96*96];

            for(int i = 0; i<9216; i++){
                fArray[i] = (float)((barray[4*i])&0xff);
            }

            float[] input = fArray;
            int[][] output = new int[][]{{-1}};

            Interpreter tflite = getTfliteInterpreter("emotion_detect.tflite");
            tflite.run(input, output);
            Log.e(TAG, "output : " + output[0][0]);

            // drawable 리소스 객체 가져오기
            if(output[0][0] == 0) {
                e_drawable = getResources().getDrawable(R.drawable.blank);
                tv_result.setText("무표정");
            }else if(output[0][0] == 1){
                e_drawable = getResources().getDrawable(R.drawable.joy);
                tv_result.setText("기쁨");
            }else if(output[0][0] == 2){
                e_drawable = getResources().getDrawable(R.drawable.sad);
                tv_result.setText("슬픔");
            }else if(output[0][0] == 3){
                e_drawable = getResources().getDrawable(R.drawable.angry);
                tv_result.setText("화남");
            }else if(output[0][0] == 4){
                e_drawable = getResources().getDrawable(R.drawable.amazing);
                tv_result.setText("놀람");
            }else if(output[0][0] == 5){
                e_drawable = getResources().getDrawable(R.drawable.fear);
                tv_result.setText("두려움");
            }else{
                e_drawable = getResources().getDrawable(R.drawable.disgust);
                tv_result.setText("역겨움");
            }

            emotion_view.setImageDrawable(e_drawable);

            // XML 에 있는 ImageView 위젯에 이미지 셋팅
        } else { //검출된 얼굴의 갯수가 0개이거나 1개를 넘었을 경우
            if (dfn == 0) { //얼굴을 검출하지 못한 경우 팝업창 호출
                Intent intent = new Intent(this, PopupActivity.class);
                intent.putExtra("data", "얼굴을 검출하지 못하였습니다.\r\n다시 한번 촬영해 주세요!!");
                tv_result.setText("검출 실패");
                e_drawable = getResources().getDrawable(R.drawable.cross);
                emotion_view.setImageDrawable(e_drawable);
                startActivityForResult(intent, 3000);
            } else { //얼굴이 다중으로 검출된 경우
                Intent intent = new Intent(this, PopupActivity.class);
                intent.putExtra("data", "검출된 얼굴이 너무 많습니다.\r\n다시 한번 촬영해 주세요!!");
                tv_result.setText("검출 실패");
                e_drawable = getResources().getDrawable(R.drawable.cross);
                emotion_view.setImageDrawable(e_drawable);
                startActivityForResult(intent, 4000);
            }
        }




        //화면에 사각형이 들어간 사진 을 imageview 에 넣는다
        Utils.matToBitmap(matResult, bmp32);
        imageView.setImageBitmap(bmp32);

        matResult = null;
        matInput = null;
        tempFile = null;
    }


    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create().show();
    }

    /**
     * 권한 설정
     */
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d(TAG, "copyFile :: 다음 경로로 파일복사 " + pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 " + e.toString());
        }

    }

    private void read_cascade_file() {
        copyFile("haarcascade_frontalface_alt.xml");

        Log.e(TAG, "read_cascade_file:");


        cascadeClassifier_face = loadCascade("haarcascade_frontalface_alt.xml");
        Log.e(TAG, "cascadeClassifier_face = " + cascadeClassifier_face);
    }

    //모델 파일 인터프리터를 생성하는 공통 함수
    //loadModelFile 함수에 예외가 포함되어 있기 때문에 반드시 try, catch 블록이 필요하다.
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 모델을 읽어오는 함수로, 텐서플로 라이트 홈페이지에 있다.
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있다.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}