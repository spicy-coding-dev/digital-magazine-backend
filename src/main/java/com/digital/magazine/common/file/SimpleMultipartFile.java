package com.digital.magazine.common.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class SimpleMultipartFile implements MultipartFile {

	private final byte[] content;
	private final String filename;
	private final String contentType;

	public SimpleMultipartFile(byte[] content, String filename, String contentType) {
		this.content = content;
		this.filename = filename;
		this.contentType = contentType;
	}

	@Override
	public String getName() {
		return filename;
	}

	@Override
	public String getOriginalFilename() {
		return filename;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return content.length == 0;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return content;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(java.io.File dest) throws IOException {
		throw new UnsupportedOperationException("transferTo not supported");
	}
}
