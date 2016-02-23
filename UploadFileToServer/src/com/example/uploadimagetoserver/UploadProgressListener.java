package com.example.uploadimagetoserver;
/**
 * Upload Listener
 *
 */
public interface UploadProgressListener {
	/**
	 * This method updated how much data size uploaded to server
	 * @param num
	 */
	void transferred(long num);
}
