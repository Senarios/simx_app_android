package com.hdev.common.datamodels;

import java.io.Serializable;

public class S3UploadRequest implements Serializable {
   private String path,key;
   private String S3_PATH;
   private Broadcasts broadcast;
   private UploadActions action;
   private String s3FileLink,message;
   private VideoCv videoCv;


    public VideoCv getVideoCv() {
        return videoCv;
    }

    public S3UploadRequest setVideoCv(VideoCv videoCv) {
        this.videoCv = videoCv;
        return this;
    }

    public enum UploadActions{
       BROADCAST,
       VIDEOCV
   }


    public S3UploadRequest() {
    }


    public Broadcasts getBroadcast() {
        return broadcast;
    }

    public S3UploadRequest setBroadcast(Broadcasts broadcast) {
        this.broadcast = broadcast;
        return this;
    }

    public String getPath() {
        return path;
    }

    public S3UploadRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public String getKey() {
        return key;
    }

    public S3UploadRequest setKey(String key) {
        this.key = key;
        return this;
    }

    public String getS3_PATH() {
        return S3_PATH;
    }

    public S3UploadRequest setS3_PATH(String s3_PATH) {
        S3_PATH = s3_PATH;
        return this;
    }

    public UploadActions getAction() {
        return action;
    }

    public S3UploadRequest setAction(UploadActions action) {
        this.action = action;
        return this;
    }

    public String getS3FileLink() {
        return s3FileLink;
    }

    public S3UploadRequest setS3FileLink(String s3FileLink) {
        this.s3FileLink = s3FileLink;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public S3UploadRequest setMessage(String message) {
        this.message = message;
        return this;
    }
}
