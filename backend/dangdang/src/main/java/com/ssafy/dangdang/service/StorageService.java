package com.ssafy.dangdang.service;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {

    Resource loadImageAsResource(String filename);

    void delete(String filename) throws FileNotFoundException;

    void store(String toString, MultipartFile file) throws IOException;

    void imageStore(String uuid, MultipartFile file) throws IOException;

    Path load(String filename);

    Resource loadAsResource(String filename);
}
