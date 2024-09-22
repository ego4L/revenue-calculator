package com.egoxide.finance.fileuploadservice.controller;

import com.egoxide.finance.fileuploadservice.exception.ParseTransactionDataException;
import com.egoxide.finance.fileuploadservice.message.ResponseMessage;
import com.egoxide.finance.fileuploadservice.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FilesController {

    final FileUploadService fileUploadService;

    public FilesController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/api/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) throws ParseTransactionDataException {

        String message;

        try {
            message = "Uploaded successfully: " + file.getOriginalFilename();
            fileUploadService.uploadFile(file);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }
}
