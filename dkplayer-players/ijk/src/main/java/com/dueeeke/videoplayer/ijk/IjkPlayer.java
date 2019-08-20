package com.dueeeke.videoplayer.ijk;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.dueeeke.videoplayer.player.AbstractPlayer;
import com.dueeeke.videoplayer.player.VideoViewManager;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkPlayer extends AbstractPlayer {

    protected IjkMediaPlayer mMediaPlayer;
    private boolean mIsLooping;
    private boolean mIsEnableMediaCodec;
    protected Context mAppContext;
    private int mBufferedPercent;
    private List<String> mListSorce;
    private ConcatBean mConcatBean;
    private Disposable mProgressDisposable;
    private int mCurrentIndex = 0;
    private int mLastIndex = -1;

    public IjkPlayer(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public void initPlayer() {
        mMediaPlayer = new IjkMediaPlayer();
        //native日志
        IjkMediaPlayer.native_setLogLevel(VideoViewManager.getConfig().mIsEnableLog ? IjkMediaPlayer.IJK_LOG_DEBUG : IjkMediaPlayer.IJK_LOG_SILENT);
        setOptions();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mMediaPlayer.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(int i, Bundle bundle) {
                return true;
            }
        });
    }

    @Override
    public void setOptions() {
//        是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        //开启硬解
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        //指定最大宽度
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox-max-frame-width", 960);
        //跳帧处理,放CPU处理较慢时，进行跳帧处理，保证播放流程，画面和声音同步
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 0);
        //最大fps(帧速率)
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fps", 30);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 3);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "tart-on-prepared", 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);


        //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);

        //播放前的探测Size，默认是1M, 改小一点会出画面更快
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probsize", "4096");
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 1024L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 2000000);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            Uri uri = Uri.parse(path);
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
                RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(mAppContext, uri);
                mMediaPlayer.setDataSource(rawDataSourceProvider);
            } else {
                mMediaPlayer.setDataSource(mAppContext, uri, headers);
            }

        } catch (Exception e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        try {
            mMediaPlayer.setDataSource(new RawDataSourceProvider(fd));
        } catch (Exception e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void setListDataSource(List<String> listPath, Map<String, String> headers) {
        try {
            mListSorce = listPath;
            mConcatBean = FileUtil.createConcatFile(listPath);
            File concatFile = mConcatBean.mConcatFile;
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "rtmp,concat,ffconcat,file,subfile,http,https,tls,rtp,tcp,udp,crypto");
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0);
            mMediaPlayer.setDataSource(Uri.parse(concatFile.getAbsolutePath()).toString());

        } catch (Exception e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void start() {
        try {
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void stop() {
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void prepareAsync() {
        try {
            mMediaPlayer.prepareAsync();
            if (mListSorce != null && mListSorce.size() > 0) {
                mCurrentIndex = 0;
                startProgressTask();
            }

        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void reset() {
        mMediaPlayer.reset();
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mMediaPlayer.setLooping(mIsLooping);
        setOptions();
        setEnableMediaCodec(mIsEnableMediaCodec);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        try {
            mMediaPlayer.seekTo((int) time);
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void release() {
        stopProgressTask();
        new Thread() {
            @Override
            public void run() {
                try {
                    mMediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {
        mMediaPlayer.setVolume(v1, v2);
    }

    @Override
    public void setLooping(boolean isLooping) {
        this.mIsLooping = isLooping;
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public void setEnableMediaCodec(boolean isEnable) {
        mIsEnableMediaCodec = isEnable;
        int value = isEnable ? 1 : 0;
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", value);//开启硬解码
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", value);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", value);
    }

    @Override
    public void setSpeed(float speed) {
        mMediaPlayer.setSpeed(speed);
    }

    @Override
    public long getTcpSpeed() {
        return mMediaPlayer.getTcpSpeed();
    }

    private IMediaPlayer.OnErrorListener onErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
            mPlayerEventListener.onError();
            return true;
        }
    };

    private IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mPlayerEventListener.onCompletion();
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            mPlayerEventListener.onInfo(what, extra);
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
            mBufferedPercent = percent;
        }
    };


    private IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mPlayerEventListener.onPrepared();
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
            int videoWidth = iMediaPlayer.getVideoWidth();
            int videoHeight = iMediaPlayer.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                mPlayerEventListener.onVideoSizeChanged(videoWidth, videoHeight);
            }
        }
    };


    private void startProgressTask() {
        Observable.interval(0, 1, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                if (mProgressDisposable != null && !mProgressDisposable.isDisposed()) {
                    mProgressDisposable.dispose();
                }
                mProgressDisposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                long position = getCurrentPosition();
                long endTime = getEndTime(mCurrentIndex);
                if (mCurrentIndex == mListSorce.size() - 1) {
                    stopProgressTask();
                    return;
                }
                if (position >= endTime) {
                    mCurrentIndex++;
                    Log.e("kermitye", "======sourceChange:" + mCurrentIndex);
                    if(mPlayerEventListener != null) {
                        mPlayerEventListener.onSourceChange(mCurrentIndex);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private long getEndTime(int position) {
        long endTime = 0L;
        for (int i = 0; i <= position; i++) {
            endTime += mConcatBean.mVideos.get(i).duration;
        }
        return endTime;
    }

    private void stopProgressTask() {
        if (mProgressDisposable != null && !mProgressDisposable.isDisposed()) {
            mProgressDisposable.dispose();
        }
        mProgressDisposable = null;
    }

}
