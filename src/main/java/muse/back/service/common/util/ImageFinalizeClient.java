package muse.back.service.common.util;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Component
public class ImageFinalizeClient {
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-File-Token";

    private final RestClient restClient;
    private final String internalToken;

    public ImageFinalizeClient(
            @Value("${integration.image.base-url:http://localhost:8081}") String imageBaseUrl,
            @Value("${integration.image.internal-token}") String internalToken,
            @Value("${integration.image.connect-timeout:3s}") Duration connectTimeout,
            @Value("${integration.image.read-timeout:15s}") Duration readTimeout
    ) {
        if (internalToken == null || internalToken.isBlank()) {
            throw new IllegalStateException("integration.image.internal-token must be configured");
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(imageBaseUrl)
                .requestFactory(requestFactory)
                .build();
        this.internalToken = internalToken;
    }

    public FinalizedImage finalizeImage(String fileName, String targetDir) {
        try {
            FinalizedImage response = restClient.post()
                    .uri("/files/finalize")
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
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

    public void deleteImage(String fileName) {
        try {
            restClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/images/finalized")
                            .queryParam("fileName", fileName)
                            .build())
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw new GeneralException(Code.INTERNAL_SERVER_ERROR, "Failed to delete finalized image");
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
