package org.caykhe.imageservice.services;

import ch.qos.logback.core.CoreConstants;
import lombok.RequiredArgsConstructor;
import org.caykhe.imageservice.dtos.ApiException;
import org.caykhe.imageservice.models.Image;
import org.caykhe.imageservice.repositories.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class
ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);
    final ImageRepository imageRepository;
    Path currentPath = Paths.get("");

    private final String uploadDir = currentPath.toAbsolutePath() + "/src/main/resources/public/uploads/";

    public String upload(MultipartFile file) {
        String extention = getFileExtension(file);
        if(!extention.equals("jpg") && !extention.equals("jpeg") && !extention.equals("png"))
            throw new ApiException("File không đúng định dạng. Chỉ nhận các file: jpg, jpeg, png!", HttpStatus.BAD_REQUEST);
        try {
            Image image = Image.builder()
                    .extension(extention)
                    .status(false)
                    .build();
            image = imageRepository.save(image);
            // Tạo thư mục uploads nếu nó không tồn tại
            Files.createDirectories(Paths.get(uploadDir));

            // Sao chép hình ảnh vào thư mục uploads
            Path path = Paths.get(uploadDir + image.getId() + '.' + image.getExtension());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return image.getId().toString() + '.' + image.getExtension();
        } catch (Exception e) {
            throw new ApiException("Có lỗi xảy ra. Vui lòng thử lại sau!", HttpStatus.BAD_REQUEST);
        }
    }

    public String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Could not determine file type");
        }

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0) {
            throw new RuntimeException("Could not determine file type");
        }

        return originalFilename.substring(dotIndex + 1);
    }

    public InputStreamResource getImnage(String imageName) {
        try {
            Path imagePath = Paths.get(uploadDir + imageName);
            return new InputStreamResource(Files.newInputStream(imagePath));
        } catch (IOException e) {
            throw new ApiException("Có lỗi xảy ra. Vui lòng thử lại sau!", HttpStatus.BAD_REQUEST);
        }
    }

    public void saveImagesInContent(String content) {
        List<Integer> ids = regexIdInContext(content);
        System.out.println(ids.size());
        if(ids.isEmpty())
            return;
        try {
            List<Image> images = imageRepository.findByIdIn(ids);
            images.forEach(image -> image.setStatus(true));
            imageRepository.saveAll(images);
        }
        catch (Exception e) {
            throw new ApiException("Có lỗi xảy ra. Hình ảnh không tồn tại!", HttpStatus.BAD_REQUEST);
        }
    }

    public void removeImagesInContent(String content) {
        List<Integer> ids = regexIdInContext(content);
        System.out.println(ids.size());
        if(ids.isEmpty())
            return;
        try {
            List<Image> images = imageRepository.findByIdIn(ids);
            images.forEach(image -> image.setStatus(false));
            imageRepository.saveAll(images);
        }
        catch (Exception e) {
            throw new ApiException("Có lỗi xảy ra. Hình ảnh không tồn tại!", HttpStatus.BAD_REQUEST);
        }
    }

    public List<Integer> regexIdInContext(String content) {
        Pattern pattern = Pattern.compile("\\(http://localhost:8892/api/images/(.*?)\\.(png|jpeg|jpg)\\)");
        Matcher matcher = pattern.matcher(content);
        List<Integer> ids = new ArrayList<>();

        while (matcher.find()) {
            try {
                ids.add(Integer.valueOf(matcher.group(1)));
            } catch (NumberFormatException e) {
                LOGGER.error("Đã xảy ra lỗi", e);
            }
        }

        return ids;
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void cleanupImages() {
        try {
            Date now = new Date();
            Date twoHoursBefore = new Date(now.getTime() - 24 * 60 * 60 * 1000);
            List<Image> images = imageRepository.findImagesByStatusAndCreatedAt(false, twoHoursBefore.toInstant());
            for (Image i: images) {
                Path pathImage = Paths.get(uploadDir + i.getId() + "." + i.getExtension());
                Files.delete(pathImage);
            }
            imageRepository.deleteAll(images);
        }
        catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
