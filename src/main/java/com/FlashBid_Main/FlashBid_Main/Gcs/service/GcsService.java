package com.FlashBid_Main.FlashBid_Main.Gcs.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
public class GcsService {

  private final String bucketName = "junyeong_buket";

  public String uploadImage(MultipartFile file) throws IOException {
    Storage storage = StorageOptions.getDefaultInstance().getService();

    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

    BlobId blobId = BlobId.of(bucketName, fileName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(file.getContentType())
        .build();

    storage.create(blobInfo, file.getBytes());

    return fileName;
  }
}