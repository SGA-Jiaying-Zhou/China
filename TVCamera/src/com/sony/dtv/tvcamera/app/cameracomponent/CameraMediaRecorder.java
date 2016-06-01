package com.sony.dtv.tvcamera.app.cameracomponent;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.SecurityCameraService;
import com.sony.dtv.tvcamera.app.TVCameraApp;
import com.sony.dtv.tvcamera.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraMediaRecorder {
    private static final String TAG = "CameraMediaRecorder";
    private MediaCodec mVideoEncoder = null;
    private MediaCodec mAudioEncoder = null;
    private MediaMuxer mMediaMuxer = null;
    private MediaFormat mMediaFormat = null;
    private boolean mIsAudioEncoding = false;
    private boolean mIsVideoEncoding = false;
    private boolean mIsAudioEncodingStrated = false;
    private boolean mIsVideoEncodingStrated = false;
    private final Object mNotifyAudioEndOfStream = new Object();
    private final Object mNotifyVideoEndOfStream = new Object();
    private int mWidth = Utils.CAMERA_SIZE_1080P_WIDTH;
    private int mHeight = Utils.CAMERA_SIZE_1080P_HEIGHT;
    private int mFrameRate = 30;                //default framerate 30
    private int mIFrameInterval = 1;            //default i-frame-interval 1
    /* ColorFormat supported both "COLOR_FormatYUV420SemiPlanar" and "COLOR_FormatYUV420Planar"
     * default value is MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar */
    private int mColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
    private String mSaveFileAbsolutePath = null;
    private String mDestinationFileAbsolutePath = null;
    AudioThread mAudioOutputThread;
    DequeueOutputBufferThread mVideoOutputThread;
    private int mBitRate = 10 * 1000 * 1000;
    private Handler mHandler;
    private boolean mIsInformAudioStop = false;
    private boolean mIsInformVideoStop = false;
    private Context mContext;

    private ExecutorService mExecutorService;
    private final Object mLock = new Object();

    /* kasai */
    public static AudioRec mAudioRec = new AudioRec();
    /* kasai end */

    public CameraMediaRecorder(Handler handler, Context context) {
        mContext = context;
        mHandler = handler;
    }

    public boolean initMediaCodec(String mime, int width, int height, int bitRate) {
        mWidth = width;
        mHeight = height;
        mBitRate = bitRate;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateStr = sdf.format(new Date());

        SharedPreferences USBPath = TVCameraApp.getInstance().getSharedPreferences("usb", 0);
        String SaveFilePath = null;
        String UsbPath = null;

        Service securityCameraService = TVCameraApp.getSecurityCameraService();
        if (securityCameraService == null) {
            SaveFilePath = USBPath.getString("usb_current_path", "") + "/" + Utils.RECORD_PATH;
            UsbPath = SaveFilePath;
        } else {
            File file = mContext.getFilesDir();
            SaveFilePath = file.getAbsolutePath() + "/" + Utils.SECURITY_RECORD_PATH;
            mDestinationFileAbsolutePath = USBPath.getString("usb_current_path", "") + "/" + Utils.SECURITY_RECORD_PATH + "/" + dateStr + "_" + mWidth + "x" + mHeight + ".mp4";
            UsbPath = mDestinationFileAbsolutePath;
        }

        if (!Utils.checkUsbWritablity(UsbPath)) {
            if (null == securityCameraService) {
                mHandler.sendEmptyMessage(CameraEncoder.USB_ERROR);
            } else {
                if (!Utils.isErrorShowed()) {
                    Utils.setIsErrorShowed(true);
                    Log.d(TAG, "security_camera_usb_read_only_toast");
                    TVCameraApp.setSecurityCameraStringID(R.string.security_camera_usb_read_only_toast);
                }
                securityCameraService.stopSelf();
            }
            return false;
        }

        /* component name of camera "OMX.MTK.VIDEO.ENCODER.CAMERA" */
        try {
            mVideoEncoder = MediaCodec.createByCodecName("OMX.MTK.VIDEO.ENCODER.CAMERA");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        /*
         * @mime: One of the encoding type which the camera component supported. You must be used
         * "MediaFormat.MIMETYPE_VIDEO_VP8" or "MediaFormat.MIMETYPE_VIDEO_AVC"
         */
        mMediaFormat = MediaFormat.createVideoFormat(mime, width, height);

        /* bitrate */
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        Log.d(TAG, "bitRate=" + bitRate);

        /* framerate */
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);

        /* @mColorFormat: One of the encoding color-format type which the camera component supported.
         * You must used "MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar" or
         * "MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar"
         */
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);

        /* i-frame iinterval */
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mIFrameInterval);
        try {
            mVideoEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (MediaCodec.CodecException e) {
            e.printStackTrace();
            return false;
        }

        try {
            mAudioEncoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        MediaFormat audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 11025, 1);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);// AAC-HE // 64kbps
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);//AACObjectLC
        mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        File filePath = new File(SaveFilePath);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        try {
            if ((null != securityCameraService)
                    && ((Utils.getMemoryCapacity(mContext.getFilesDir().getAbsolutePath()) <= Utils.SECURITY_CAMERA_MIN_USB_SIZE))) {
                mSaveFileAbsolutePath = mDestinationFileAbsolutePath;
            } else {
                mSaveFileAbsolutePath = SaveFilePath + "/" + dateStr + "_" + mWidth + "x" + mHeight + ".mp4";
            }
            mMediaMuxer = new MediaMuxer(mSaveFileAbsolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            if (null == securityCameraService) {
                mHandler.sendEmptyMessage(CameraEncoder.USB_ERROR);
            } else {
                if (!Utils.isErrorShowed()) {
                    Utils.setIsErrorShowed(true);
                    Log.d(TAG, "security_camera_usb_read_only_toast");
                    TVCameraApp.setSecurityCameraStringID(R.string.security_camera_usb_read_only_toast);
                }
                securityCameraService.stopSelf();
            }

            if (mVideoEncoder != null) {
                mVideoEncoder.release();
                mVideoEncoder = null;
            }

            if (mAudioEncoder != null) {
                mAudioEncoder.release();
                mAudioEncoder = null;
            }
            e.printStackTrace();
            return false;
        }

        outputAudioTrack = -1;
        outputVideoTrack = -1;

        Utils.setSaveFileAbsolutePath(mSaveFileAbsolutePath);
        return true;
    }

    public void start() {
        Log.v(TAG, "start() enter");
        mIsInformAudioStop = false;
        mIsInformVideoStop = false;
        mAudioEncoder.start();
        mIsAudioEncodingStrated = true;
        mVideoEncoder.start();
        mIsVideoEncodingStrated = true;
        mIsAudioEncoding = true;
        mIsVideoEncoding = true;

        mAudioOutputThread = new AudioThread();
        new Thread(mAudioOutputThread).start();
        mVideoOutputThread = new DequeueOutputBufferThread();
        new Thread(mVideoOutputThread).start();
    }

    public void stop() {
        Log.v(TAG, "stop() enter");
        if (mIsAudioEncoding) {
            synchronized (mNotifyAudioEndOfStream) {
                notifyAudioEndOfStream();
                mIsInformAudioStop = true;
                try {
                    mNotifyAudioEndOfStream.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mIsInformAudioStop = false;
            }
        }

        if (mIsVideoEncoding) {
            synchronized (mNotifyVideoEndOfStream) {
                notifyVideoEndOfStream();
                mIsInformVideoStop = true;
                try {
                    mNotifyVideoEndOfStream.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mIsInformVideoStop = false;
            }
        }

        if (mIsMuxerStarted) {
            try {
                mMediaMuxer.stop();

                if (mSaveFileAbsolutePath.contains(Utils.SECURITY_RECORD_PATH)) {
                    Service securityCameraService = TVCameraApp.getSecurityCameraService();
                    File fromFile = new File(mSaveFileAbsolutePath);
                    try {
                        if (getFileSize(fromFile) <= 0) {
                            fromFile.delete();
                        } else {
                            if (null != securityCameraService) {
                                ((SecurityCameraService) securityCameraService).startNewLoop();
                            }
                            if (!mSaveFileAbsolutePath.equals(mDestinationFileAbsolutePath)) {
                                mExecutorService = Executors.newSingleThreadExecutor();
                                mExecutorService.execute(mCopyFileTask);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (null != securityCameraService) {
                            securityCameraService.stopSelf();
                        }
                    }
                } else {
                    Utils.scanFile(mSaveFileAbsolutePath);
                }

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        if (mMediaMuxer != null) {
            try {
                mMediaMuxer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMediaMuxer = null;
        }
        mIsMuxerStarted = false;

        if (mIsAudioEncodingStrated) {
            mIsAudioEncodingStrated = false;
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }

        if (mIsVideoEncodingStrated) {
            mIsVideoEncodingStrated = false;
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
		
		Utils.setSaveFileAbsolutePath(null);
        Log.v(TAG, "stop() exit");
    }

    public void release() {
        if (mIsAudioEncoding | mIsVideoEncoding) {
            stop();
        }

        if (mAudioEncoder != null) {
            mAudioEncoder.release();
        }

        if (mVideoEncoder != null) {
            mVideoEncoder.release();
        }
        mAudioEncoder = null;
        mVideoEncoder = null;
        mMediaFormat = null;

        if (mMediaMuxer != null) {
            mMediaMuxer.release();
            mMediaMuxer = null;
        }
    }

    private void notifyAudioEndOfStream() {
        //audio end notify
        int inputAudioBufferIndex = mAudioEncoder.dequeueInputBuffer(0);
        while (inputAudioBufferIndex < 0) {
            inputAudioBufferIndex = mAudioEncoder.dequeueInputBuffer(0);
        }

        long presentationTime = System.nanoTime() / 1000;
        mAudioEncoder.queueInputBuffer(inputAudioBufferIndex, 0, 0, presentationTime, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    }

    private void notifyVideoEndOfStream() {
        //video end notify
        ByteBuffer[] inputBuffers = mVideoEncoder.getInputBuffers();
        int inputBufferIndex = mVideoEncoder.dequeueInputBuffer(0);
        while (inputBufferIndex < 0) {
            inputBufferIndex = mVideoEncoder.dequeueInputBuffer(0);
        }

        long presentationTime = System.nanoTime() / 1000;
        mVideoEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTime, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    }

    private boolean mIsMuxerStarted = false;
    private int outputAudioTrack = -1;
    private int outputVideoTrack = -1;

    /* kasai */
    public static class AudioRec {
        private AudioRecord rec = null;

        synchronized public int start_AudioRecording() {
            if (rec != null) {
                Log.w(TAG, "AudioRecThread alrady running");
                return -1;
            }

            int frequency = 11025;
            int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
            int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding) * 2;
            rec = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
            rec.startRecording();
            Log.i(TAG, "start AudioRecording");
            return 0;
        }

        synchronized public int stop_AudioRecording() {
            Log.i(TAG, "stop_AudioRecording() Enter ");
            if (rec != null) {
                Log.i(TAG, "stop_AudioRecording() : call AudioRecord.stop()");
                rec.stop();
                rec.release();
                rec = null;
            }
            Log.i(TAG, "stop_AudioRecording() Exit ");
            return 0;
        }

        synchronized int read(byte[] buffer, int size) {
            if (rec == null) {
                return -1;
            }
            return rec.read(buffer, 0, size);
        }
    }
    /* kasai end */


    private class AudioThread implements Runnable {

        @Override
        public void run() {
            int frequency = 11025;
            int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
            int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

            ByteBuffer[] audioEncoderInputBuffers = mAudioEncoder.getInputBuffers();
            ByteBuffer[] audioEncoderOutputBuffers = mAudioEncoder.getOutputBuffers();

            MediaFormat audioEncoderOutputFormat = null;

            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding) * 2;
            /* kasai remove */
            /*
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

            audioRecord.startRecording();
            */
            /* kasai remove end */


            while (mIsAudioEncoding) {
                byte[] buffer = new byte[bufferSize];
                /* kasai */
                //int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                int bufferReadResult = mAudioRec.read(buffer, bufferSize);
                //Log.d(TAG, "bufferReadResult = "+ bufferReadResult);
                /* kasai end */

                if (bufferReadResult < 0) {
                    mIsAudioEncoding = false;
                    break;
                }

                while (bufferReadResult > 0) {
                    try {
                        int encoderInputBufferIndex = mAudioEncoder.dequeueInputBuffer(-1);
                        if (encoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            if (mIsInformAudioStop) {
                                Log.d(TAG, "Now stop audio encoder fiercely");
                                mIsAudioEncoding = false;
                                synchronized (mNotifyAudioEndOfStream) {
                                    mNotifyAudioEndOfStream.notify();
                                }
                            }
                            break;
                        }
                        ByteBuffer encoderInputBuffer = audioEncoderInputBuffers[encoderInputBufferIndex];
                        encoderInputBuffer.put(buffer);
                        long presentationTime = System.nanoTime() / 1000;
                        mAudioEncoder.queueInputBuffer(
                                encoderInputBufferIndex,
                                0,
                                buffer.length,
                                presentationTime,
                                0);
                        // We enqueued a pending frame, let's try something else next.
                        break;
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        mIsAudioEncoding = false;
                        if (mIsInformAudioStop) {
                            Log.d(TAG, "Now stop audio encoder fiercely");
                            synchronized (mNotifyAudioEndOfStream) {
                                mNotifyAudioEndOfStream.notify();
                            }
                        } else {
                            release();
                            /* kasai remove */
                            /*
                            audioRecord.stop();
                            audioRecord.release();
                            */
                            /* kasai remove end */
                            return;
                        }
                    }
                }

                while (mIsAudioEncoding) {
                    MediaCodec.BufferInfo audioEncoderOutputBufferInfo = new MediaCodec.BufferInfo();

                    try {
                        int encoderOutputBufferIndex = mAudioEncoder.dequeueOutputBuffer(
                                audioEncoderOutputBufferInfo, 10000);

                        if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            if (mIsInformAudioStop) {
                                Log.d(TAG, "Now stop audio encoder fiercely");
                                mIsAudioEncoding = false;
                                synchronized (mNotifyAudioEndOfStream) {
                                    mNotifyAudioEndOfStream.notify();
                                }
                            }
                            break;
                        }
                        if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                            audioEncoderOutputBuffers = mAudioEncoder.getOutputBuffers();
                            break;
                        }
                        if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            if (outputAudioTrack >= 0) {
//                            fail("audio encoder changed its output format again?");
                            }
                            audioEncoderOutputFormat = mAudioEncoder.getOutputFormat();

                            if (audioEncoderOutputFormat != null) {
                                outputAudioTrack = mMediaMuxer.addTrack(audioEncoderOutputFormat);
                            }
                            break;
                        }
                        ByteBuffer encoderOutputBuffer =
                                audioEncoderOutputBuffers[encoderOutputBufferIndex];
                        if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            // Simply ignore codec config buffers.
                            mAudioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                            continue;
                        }
                        if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            mIsAudioEncoding = false;
                            synchronized (mNotifyAudioEndOfStream) {
                                mNotifyAudioEndOfStream.notify();
                            }
                            break;
                        }
                        if (mIsMuxerStarted && outputAudioTrack >= 0 && audioEncoderOutputBufferInfo.size != 0) {
                            mMediaMuxer.writeSampleData(
                                    outputAudioTrack, encoderOutputBuffer, audioEncoderOutputBufferInfo);
                        }
                        mAudioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                        // We enqueued an encoded frame, let's try something else next.
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        mIsAudioEncoding = false;
                        if (mIsInformAudioStop) {
                            Log.d(TAG, "Now stop audio encoder fiercely");
                            synchronized (mNotifyAudioEndOfStream) {
                                mNotifyAudioEndOfStream.notify();
                            }
                        } else {
                            /* kasai remove */
                            /*
                            audioRecord.stop();
                            audioRecord.release();
                            */
                            /* kasai remove end */
                            release();
                        }
                        return;
                    }
                }
            }
            /* kasai remove */
            /*
            audioRecord.stop();
            audioRecord.release();
            */
            /* kasai remove end */
        }
    }

    private class DequeueOutputBufferThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            ByteBuffer[] outputBuffers = mVideoEncoder.getOutputBuffers();

            while (mIsVideoEncoding) {

                while (true) {
                    if (outputAudioTrack < 0) {
                        try {
                            Thread.sleep(1, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    try {
                        int outputBufferIndex = mVideoEncoder.dequeueOutputBuffer(bufferInfo, 33333);
                        if (outputBufferIndex >= 0) {
                            ByteBuffer outBuffer = outputBuffers[outputBufferIndex];
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                mVideoEncoder.releaseOutputBuffer(outputBufferIndex, false);
                                continue;
                            }
                            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                                mIsVideoEncoding = false;
                                synchronized (mNotifyVideoEndOfStream) {
                                    mNotifyVideoEndOfStream.notify();
                                }
                                mVideoEncoder.releaseOutputBuffer(outputBufferIndex, false);
                                break;
                            }
                            if (!mIsMuxerStarted && outputAudioTrack >= 0 && outputVideoTrack >= 0) {
                                mMediaMuxer.start();
                                mIsMuxerStarted = true;
                            }
                            if (mIsMuxerStarted) {
                                bufferInfo.presentationTimeUs = System.nanoTime() / 1000;
                                mMediaMuxer.writeSampleData(outputVideoTrack, outBuffer, bufferInfo);
                            }
                            mVideoEncoder.releaseOutputBuffer(outputBufferIndex, false);
                            break;
                        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                            outputBuffers = mVideoEncoder.getOutputBuffers();
                            continue;
                        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            if (outputVideoTrack == -1) {
                                mMediaFormat = mVideoEncoder.getOutputFormat();
                                outputVideoTrack = mMediaMuxer.addTrack(mMediaFormat);

                            }
                            continue;
                        } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            if (mIsInformVideoStop) {
                                Log.d(TAG, "Now stop video encoder fiercely");
                                mIsVideoEncoding = false;
                                synchronized (mNotifyVideoEndOfStream) {
                                    mNotifyVideoEndOfStream.notify();
                                }
                            }
                            break;
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        mIsVideoEncoding = false;
                        if (mIsInformVideoStop) {
                            Log.d(TAG, "Now stop video encoder fiercely");
                            synchronized (mNotifyVideoEndOfStream) {
                                mNotifyVideoEndOfStream.notify();
                            }
                        } else {
                            release();
                        }
                        return;
                    }

                }
            }
        }
    }

    Runnable mCopyFileTask = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                File fromFile = new File(mSaveFileAbsolutePath);
                File toFile = new File(mDestinationFileAbsolutePath);
                copyFile(fromFile, toFile, true);
            }
        }
    };

    private long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (IOException e) {
                e.fillInStackTrace();
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        }
        return size;
    }

    public void copyFile(File fromFile, File toFile, Boolean rewrite) {
        Log.d(TAG, "copy file fromFile = " + fromFile.getAbsolutePath() + " toFile = " + toFile.getAbsolutePath() + " start!");
        if (!fromFile.exists()) {
            return;
        }
        if (!fromFile.isFile()) {
            return;
        }
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        if (toFile.exists() && rewrite) {
            toFile.delete();
        }

        FileInputStream fisFrom = null;
        FileOutputStream fosTo = null;
        try {
            fisFrom = new FileInputStream(fromFile);
            fosTo = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fisFrom.read(bt)) > 0) {
                fosTo.write(bt, 0, c);
            }

            fisFrom.close();
            fosTo.close();
            fisFrom = null;
            fosTo = null;
            Utils.setIsErrorShowed(true);
            TVCameraApp.setSecurityCameraStringID(R.string.security_camera_success);
            Utils.scanFile(mDestinationFileAbsolutePath);
        } catch (Exception ex) {
            ex.printStackTrace();

            if (!Utils.isErrorShowed()) {
                Utils.setIsErrorShowed(true);
                Log.d(TAG, "security_camera_error_toast");
                TVCameraApp.setSecurityCameraStringID(R.string.security_camera_error_toast);
            }
            Service securityCameraService = TVCameraApp.getSecurityCameraService();
            if (null != securityCameraService) {
                securityCameraService.stopSelf();
            }
            toFile.delete();
        } finally {
            try {
                if (null != fisFrom) {
                    fisFrom.close();
                }
                if (null != fosTo) {
                    fosTo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "copy file fromFile = " + fromFile.getAbsolutePath() + " toFile = " + toFile.getAbsolutePath() + " end!");
        fromFile.delete();
    }
}
