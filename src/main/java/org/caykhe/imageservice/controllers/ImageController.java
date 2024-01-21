package org.caykhe.imageservice.controllers;

import lombok.RequiredArgsConstructor;
import org.caykhe.imageservice.dtos.ApiException;
import org.caykhe.imageservice.services.ImageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if(file == null)
            throw new ApiException("Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST);
        String url = imageService.upload(file);
        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @GetMapping(value = "/{imageName}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String imageName) {
        InputStreamResource resource = imageService.getImnage(imageName);

        return ResponseEntity.ok()
                .body(resource);
    }

    @PostMapping("/save/bycotent")
    public ResponseEntity<?> saveByContent(@RequestBody String content) {
        imageService.saveImagesInContent(content);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/bycotent")
    public ResponseEntity<?> deleteByContent(@RequestBody String content) {
        imageService.removeImagesInContent(content);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
