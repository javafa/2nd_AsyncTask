package com.veryworks.android.asynctask;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    boolean flag = false;

    TextView result;
    Button btnStart,btnStop;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        result = (TextView) findViewById(R.id.textResult);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag){
                    Toast.makeText(MainActivity.this,"실행중입니다",Toast.LENGTH_SHORT).show();
                }else {
                    String filename = "big.avi";
                    new TestAsync().execute(filename);
                }
            }
        });
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delFile("big.avi");
            }
        });
    }


    // 파일삭제
    public void delFile(String filename){
        // 파일의 전체경로를 가져온다
        String fullPath = getFullPath(filename);
        File file = new File(fullPath);
        // 파일이 존재하면 삭제
        if(file.exists()){
            file.delete();
        }
    }

    public class TestAsync extends AsyncTask<String, Integer, Boolean> {
        // AsyncTask Generic 이 가르키는것
        // 1. doInBackground 의 파라미터
        // 2. onProgressUpdate 파라미터

        // AsyncTask 의 doInBackground 함수 전에 호출되는 함수
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag = true;
            progressBar.setProgress(0);
        }

        // sub thread 에서 실행되는 함수
        @Override
        protected Boolean doInBackground(String... params) {

            String filename = params[0];

            assetToDisk(filename);

            return true;
        }

        // doInBackground 가 종료된 후에 호출되는 함수
        //        "       의 return 값을 받는다
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                result.setText("완료되었습니다");
            }
        }

        // main thread 에서 실행되는 함수
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int sec = values[0];
            result.setText(sec + " sec");
            progressBar.setProgress(sec);
        }

        // assets 에 있는 파일을 쓰기가능한 internal Storage 로 복사한다
        // - Internal Storage 의 경로구조
        //   /data/data/패키지명
        public void assetToDisk(String filename){ // 파일이름

            // 스트림 선언
            // try 문안에 선언을 하게되면 Exception 발생시 close 함수를 호출할 방법이 없다
            InputStream is = null;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            try {
                // 1. assets 에 있는 파일을 filename 으로 읽어온다.
                AssetManager manager = getAssets();
                // 2. 파일 스트림 생성
                is = manager.open(filename);
                // 3. 버퍼 스트림으로 래핑 ( 한번에 여러개의 데이터를 가져오기 위한 래핑)
                bis = new BufferedInputStream(is);

                // 쓰기위한 준비작업
                // 4. 저장할 위치에 파일이 없으면 생성
                String targetFile = getFullPath(filename);
                File file = new File(targetFile);
                if (!file.exists()) {
                    file.createNewFile();
                }

                // 5. 쓰기 스트림을 생성
                fos = new FileOutputStream(file);
                // 6. 버퍼 스트림으로 동시에 여러개의 데이터를 쓰기위한 래핑
                bos = new BufferedOutputStream(fos);

                // 읽어올 데이터의 길이를 담아둘 변수
                int read = -1; // 모두 읽어오면 -1이 저장된다
                // 한번에 읽을 버퍼의 크기를 지정
                byte buffer[] = new byte[1024];

                while ((read = bis.read(buffer, 0, 1024)) != -1) {
                    bos.write(buffer, 0, read);
                }

                // 남아있는 데이터를 다 흘려보낸다
                bos.flush();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try {
                    // 열었던 역순으로 스트림을 닫아준다. 물론 스트림만 닫아도 같이 닫히긴 한다...
                    if(bos != null) bos.close();
                    if(fos != null) fos.close();
                    if(bis != null) bis.close();
                    if(is != null) is.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    // Internal Storage 내의 파일의 전체경로를 만들어주는 함수
    private String getFullPath(String filename){
        // /data/data/패키지명/files + / + 파일명
        return getFilesDir().getAbsolutePath() + File.separator + filename;
    }
}





