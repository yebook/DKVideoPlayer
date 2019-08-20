package com.dueeeke.dkplayer.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.dueeeke.dkplayer.R;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videoplayer.player.VideoView;

import java.util.ArrayList;

// Created by kermitye on 2019/8/20 16:24
public class MultiPlayerActivity extends AppCompatActivity {

    private VideoView mVideoView1;
    private VideoView mVideoView2;
    private VideoView mVideoView3;
    private VideoView mVideoView4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);
        initView();
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView1.pause();
        mVideoView2.pause();
        mVideoView3.pause();
        mVideoView4.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView1.resume();
        mVideoView2.resume();
        mVideoView3.resume();
        mVideoView4.resume();
    }

    @Override
    protected void onDestroy() {
        mVideoView1.release();
        mVideoView2.release();
        mVideoView3.release();
        mVideoView4.release();
        super.onDestroy();

    }


    public void initView() {
        mVideoView1 = findViewById(R.id.mVideoView1);
        mVideoView2 = findViewById(R.id.mVideoView2);
        mVideoView3 = findViewById(R.id.mVideoView3);
        mVideoView4 = findViewById(R.id.mVideoView4);
    }

    public void init() {

        String sourceUrl1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/single/aaa.mp4";
        String sourceUrl2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/11.mp4";
        String sourceUrl3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/single/ccc.mp4";
        String sourceUrl4 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/single/ddd.mp4";


        ArrayList urls = new ArrayList<String>();
        urls.add(Uri.parse(sourceUrl1).toString());
        urls.add(Uri.parse(sourceUrl2).toString());
        urls.add(Uri.parse(sourceUrl3).toString());
        urls.add(Uri.parse(sourceUrl4).toString());


        mVideoView1.setEnableAudioFocus(false);
        mVideoView1.setEnableParallelPlay(true);
//        mVideoView1.setListUrl(urls); //设置视频地址
        mVideoView1.setUrl(Uri.parse(sourceUrl1).toString());

        //SCREEN_SCALE_DEFAULT	默认的比例，按照视频宽高等比缩放并填充视频框，视频比例和视频框比例不同时有黑边
        //SCREEN_SCALE_16_9	16:9，画面可能变形
        //SCREEN_SCALE_4_3	4:3，画面可能变形
        //SCREEN_SCALE_MATCH_PARENT	填充视频框，画面可能变形
        //SCREEN_SCALE_ORIGINAL	以视频原始宽高显示，可能出现裁切画面，显示不全
        //SCREEN_SCALE_CENTER_CROP	和ImageView的CenterCrop类似，可能出现裁切画面，显示不全
        mVideoView1.setScreenScale(VideoView.SCREEN_SCALE_MATCH_PARENT);  //设置视频画面比例
        mVideoView1.setLooping(true);
//        mVideoView1.setVideoController(new StandardVideoController(this));


        /*mVideoView1.addOnVideoViewStateChangeListener(object : OnVideoViewStateChangeListener{
            override fun onPlayStateChanged(playState: Int) {
                Log.e("DKPlayer", "======onPlayStateChanged: $playState")
                when(playState) {
                    VideoView.STATE_PLAYBACK_COMPLETED -> {}
                }

            }

            override fun onPlayerStateChanged(playerState: Int) {
                Log.e("DKPlayer", "======onPlayerStateChanged: $playerState")
            }
        })*/


        mVideoView1.start(); //开始播放，不调用则不自动播放


        mVideoView2.setEnableAudioFocus(false);
        mVideoView2.setEnableParallelPlay(true);

        mVideoView2.setUrl(Uri.parse(sourceUrl2).toString()); //设置视频地址
//        mVideoView2.setListUrl(urls)
        mVideoView2.setLooping(true);
//        mVideoView2.setVideoController(new StandardVideoController(this));
        mVideoView2.start(); //开始播放，不调用则不自动播放

        mVideoView3.setEnableAudioFocus(false);
        mVideoView3.setEnableParallelPlay(true);
        mVideoView3.setUrl(Uri.parse(sourceUrl3).toString()); //设置视频地址
        mVideoView3.setLooping(true);
//        mVideoView3.setVideoController(new StandardVideoController(this));
        mVideoView3.start(); //开始播放，不调用则不自动播放

        /*mVideoView4.setEnableAudioFocus(false);
        mVideoView4.setEnableParallelPlay(true);
        mVideoView4.setUrl(Uri.parse(sourceUrl4).toString()); //设置视频地址
        mVideoView4.setLooping(true);
//        mVideoView4.setVideoController(new StandardVideoController(this));
        mVideoView4.start(); //开始播放，不调用则不自动播放*/
    }

}
