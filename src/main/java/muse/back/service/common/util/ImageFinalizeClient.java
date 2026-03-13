package muse.back.service.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Component
public class ImageFinalizeClient {
    private final RestClient restClient;

    public ImageFinalizeClient(@Value("${integration.image.base-url:http://localhost:8081}") String imageBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(imageBaseUrl)
                .build();
    }

    public FinalizedImage finalizeImage(String fileName, String targetDir) {
        try {
            FinalizedImage response = restClient.post()
                    .uri("/files/finalize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new FinalizeImageRequest(fileName, targetDir))
                    .retrieve()
                    .body(FinalizedImage.class);

            if (response == null || response.fileName() == null || response.fileName().isBlank()) {
                throw new GeneralException(Code.VALIDATION_ERROR, "Image finalize response invalid");
            }
            return response;
        } catch (GeneralException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GeneralException(Code.INTERNAL_SERVER_ERROR, "Failed to finalize uploaded image");
        }
    }

    private record FinalizeImageRequest(String fileName, String targetDir) {
    }

    public record FinalizedImage(
            String fileName,
            String originalFileName,
            String imageUrl,
            String thumbnailUrl,
            boolean temporary
    ) {
    }
}
