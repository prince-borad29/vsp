package com.vsp.contentservice.model;

/**
 * IT TRACKS THE VIDEO PROCESSING LIFECYCLE
 * FLOW :
 * PENDING -> UPLOADED -> ENCODING -> ENCODED -> READY
 *                                            -> FAILED
 */

public enum VideoStatus {
    PENDING,   // movie added but not uploaded yet
    UPLOADED,  // raw video uploaded to S3
    ENCODING,  // ffmpeg encoding the video
    ENCODED,   // encoding complete and ready
    READY,     // hls playlist ready and can be streamed
    FAILED     // encoding failed
}
