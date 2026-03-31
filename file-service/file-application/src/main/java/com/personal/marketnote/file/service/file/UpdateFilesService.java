package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.exception.InvalidFileCountLimitException;
import com.personal.marketnote.file.mapper.FileCommandToDomainMapper;
import com.personal.marketnote.file.port.in.command.UpdateFileCommand;
import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;
import com.personal.marketnote.file.port.in.usecase.file.GetFileUseCase;
import com.personal.marketnote.file.port.in.usecase.file.UpdateFileUseCase;
import com.personal.marketnote.file.port.out.event.ImageEventCommand;
import com.personal.marketnote.file.port.out.event.PublishImageEventPort;
import com.personal.marketnote.file.port.out.file.SaveFilesPort;
import com.personal.marketnote.file.port.out.file.UpdateFilesPort;
import com.personal.marketnote.file.port.out.resized.SaveResizedFilesPort;
import com.personal.marketnote.file.port.out.storage.UploadFilesPort;
import lombok.RequiredArgsConstructor;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateFilesService implements UpdateFileUseCase {
    private final GetFileUseCase getFileUseCase;
    private final UploadFilesPort uploadFilesPort;
    private final SaveFilesPort saveFilesPort;
    private final SaveResizedFilesPort saveResizedFilesPort;
    private final UpdateFilesPort updateFilesPort;
    private final PublishImageEventPort publishImageEventPort;

    public void updateFiles(UpdateFilesCommand updateFilesCommand) {
        String ownerType = updateFilesCommand.ownerType();
        Long ownerId = updateFilesCommand.ownerId();
        String sort = updateFilesCommand.fileInfo().getFirst().sort();

        FileSort fileSort = FileSort.from(sort);
        int maxCount = fileSort.getMaxCount();
        int requestedCount = updateFilesCommand.fileInfo().size();
        if (requestedCount > maxCount) {
            throw new InvalidFileCountLimitException(maxCount, requestedCount);
        }

        // 기존 파일 목록이 존재하는 경우 비활성화
        List<FileDomain> currentFiles = getFileUseCase.getFiles(OwnerType.from(ownerType), ownerId, sort);
        if (FormatValidator.hasValue(currentFiles)) {
            List<ImageEventCommand> deletedEvents = buildImageEventCommands(currentFiles);
            currentFiles.forEach(FileDomain::delete);
            List<ResizedFile> resizedFiles = getFileUseCase.getResizedFiles(currentFiles);
            resizedFiles.forEach(ResizedFile::delete);
            updateFilesPort.update(currentFiles, resizedFiles);
            publishImageEventPort.publishImageDeletedEvents(deletedEvents);
        }

        List<FileDomain> newFiles = FileCommandToDomainMapper.mapToDomain(updateFilesCommand);

        List<MultipartFile> originals = updateFilesCommand.fileInfo()
                .stream()
                .map(UpdateFileCommand::file)
                .toList();

        List<String> storageUrls = uploadFilesPort.uploadFiles(
                originals,
                OwnerType.from(updateFilesCommand.ownerType()),
                updateFilesCommand.ownerId()
        );

        List<FileDomain> savedFiles = saveFilesPort.saveAll(newFiles, storageUrls);

        List<MultipartFile> resizedToUpload = new ArrayList<>();
        List<ResizedFile> resizedToSave = new ArrayList<>();

        for (int i = 0; i < savedFiles.size(); i++) {
            resize(savedFiles.get(i), originals.get(i), resizedToUpload, resizedToSave);
        }

        if (FormatValidator.hasValue(resizedToUpload)) {
            List<String> resizedStorageUrls = uploadFilesPort.uploadFiles(
                    resizedToUpload,
                    OwnerType.from(updateFilesCommand.ownerType()),
                    updateFilesCommand.ownerId()
            );

            if (FormatValidator.hasValue(resizedToSave)) {
                List<ResizedFile> resizedWithUrls = new ArrayList<>(resizedToSave.size());
                for (int i = 0; i < resizedToSave.size(); i++) {
                    ResizedFile base = resizedToSave.get(i);
                    resizedWithUrls.add(ResizedFile.of(base.getFileId(), base.getSize(), resizedStorageUrls.get(i)));
                }
                saveResizedFilesPort.saveAll(resizedWithUrls);
            }
        }

        List<ImageEventCommand> createdEvents = buildImageEventCommands(savedFiles);
        publishImageEventPort.publishImageCreatedEvents(createdEvents);
    }

    private List<ImageEventCommand> buildImageEventCommands(List<FileDomain> files) {
        return files.stream()
                .map(ImageEventCommand::from)
                .toList();
    }

    private void resize(
            FileDomain savedFile, MultipartFile originalFile,
            List<MultipartFile> resizedToUpload, List<ResizedFile> resizedToSave
    ) {
        FileSort sort = savedFile.getSort();

        if (sort.isCatalogImage()) {
            int size = 500;

            try {
                MultipartFile resizedFile = resizeSquare(
                        originalFile, size, appendSizeToFilename(originalFile.getOriginalFilename(), size + "x" + size)
                );
                resizedToUpload.add(resizedFile);
                resizedToSave.add(ResizedFile.of(savedFile.getId(), size + "x" + size));
            } catch (IOException ignored) {
            }

            return;
        }

        if (sort.isRepresentativeImage()) {
            List<Integer> widths = List.of(600, 800);
            for (int width : widths) {
                try {
                    MultipartFile resized = resizeByWidth(
                            originalFile, width,
                            appendSizeToFilename(originalFile.getOriginalFilename(), String.valueOf(width))
                    );
                    resizedToUpload.add(resized);
                    resizedToSave.add(ResizedFile.of(savedFile.getId(), String.valueOf(width)));
                } catch (IOException ignored) {
                }
            }
        }
    }

    private MultipartFile resizeSquare(MultipartFile original, int targetWidth, String newOriginalFilename) throws IOException {
        BufferedImage source = readImageOrThrow(original);

        return resize(targetWidth, targetWidth, source, newOriginalFilename);
    }

    private MultipartFile resizeByWidth(MultipartFile original, int targetWidth, String newOriginalFilename) throws IOException {
        BufferedImage source = readImageOrThrow(original);
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();
        int targetHeight = (int) Math.round((double) originalHeight * targetWidth / originalWidth);

        return resize(targetWidth, targetHeight, source, newOriginalFilename);
    }

    private MultipartFile resize(
            int targetWidth, int targetHeight, BufferedImage source, String newOriginalFilename
    ) throws IOException {
        String extension = getExtension(newOriginalFilename, "png");
        String format = normalizeFormat(extension);
        int imageType = "jpeg".equals(format) ? BufferedImage.TYPE_INT_RGB : java.awt.image.BufferedImage.TYPE_INT_ARGB;

        BufferedImage dst = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D graphics2D = dst.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if ("jpeg".equals(format)) {
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillRect(0, 0, targetWidth, targetHeight);
        }
        graphics2D.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean written = ImageIO.write(dst, format, byteArrayOutputStream);
        if (!written) {
            throw new IOException("No ImageIO writer found for format: " + format);
        }

        return new InMemoryMultipartFile(
                "file",
                newOriginalFilename,
                contentTypeFor(format),
                byteArrayOutputStream.toByteArray()
        );
    }

    private BufferedImage readImageOrThrow(MultipartFile original) throws IOException {
        if (isSvg(original)) {
            return readSvgToBufferedImage(original);
        }

        try (InputStream inputStream = original.getInputStream()) {
            BufferedImage source = ImageIO.read(inputStream);
            if (FormatValidator.hasNoValue(source)) {
                throw new IOException("Unsupported image format.");
            }
            return source;
        }
    }

    private boolean isSvg(MultipartFile original) {
        if (FormatValidator.hasNoValue(original)) {
            return false;
        }

        String contentType = original.getContentType();
        if (FormatValidator.hasValue(contentType) && contentType.toLowerCase().contains("svg")) {
            return true;
        }

        String extension = getExtension(original.getOriginalFilename(), "");
        return FormatValidator.equalsIgnoreCase(extension, "svg");
    }

    private BufferedImage readSvgToBufferedImage(MultipartFile original) throws IOException {
        try (InputStream inputStream = original.getInputStream()) {
            TranscoderInput input = new TranscoderInput(inputStream);
            BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
            transcoder.transcode(input, null);
            BufferedImage image = transcoder.getBufferedImage();
            if (FormatValidator.hasNoValue(image)) {
                throw new IOException("Unsupported SVG image.");
            }
            return image;
        } catch (TranscoderException e) {
            throw new IOException("Failed to read SVG image.", e);
        }
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        @Override
        public BufferedImage createImage(int w, int h) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage image, TranscoderOutput out) {
            this.image = image;
        }

        private BufferedImage getBufferedImage() {
            return image;
        }
    }

    private String getExtension(String filename, String defaultExtension) {
        if (FormatValidator.hasNoValue(filename)) {
            return defaultExtension;
        }

        int idx = filename.lastIndexOf('.');
        if (idx < 0) {
            return defaultExtension;
        }

        return filename.substring(idx + 1).toLowerCase();
    }

    private String appendSizeToFilename(String originalFilename, String sizeTag) {
        if (FormatValidator.hasNoValue(originalFilename)) {
            return sizeTag;
        }

        int idx = originalFilename.lastIndexOf('.');
        if (idx < 0) {
            return originalFilename + "_" + sizeTag;
        }

        String name = originalFilename.substring(0, idx);
        String ext = originalFilename.substring(idx + 1);

        return name + "_" + sizeTag + "." + ext;
    }

    private String normalizeFormat(String extension) {
        if (FormatValidator.hasNoValue(extension)) {
            return "png";
        }

        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return "jpeg";
        }

        return extension;
    }

    private String contentTypeFor(String format) {
        if ("jpeg".equals(format)) {
            return "image/jpeg";
        }

        return "image/" + format;
    }
}
