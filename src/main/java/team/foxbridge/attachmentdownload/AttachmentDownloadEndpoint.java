package team.foxbridge.attachmentdownload;

import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Serves attachment bytes only after Halo's RBAC layer authorizes the request.
 */
@Component
public class AttachmentDownloadEndpoint implements CustomEndpoint {

    private static final GroupVersion GROUP_VERSION =
        new GroupVersion("api.attachment-download.foxbridge.team", "v1alpha1");

    private final ReactiveExtensionClient client;
    private final WebClient webClient;

    public AttachmentDownloadEndpoint(ReactiveExtensionClient client) {
        this.client = client;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
            .GET("/downloads/{name}", this::handleDownload)
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GROUP_VERSION;
    }

    private Mono<ServerResponse> handleDownload(ServerRequest request) {
        // Fast-path for permission check from client guard (download-guard.js)
        if (request.queryParam("check").filter("1"::equals).isPresent()) {
            return ServerResponse.noContent().build();
        }

        String name = request.pathVariable("name");
        return client.fetch(Attachment.class, name)
            .flatMap(attachment -> downloadAttachment(attachment))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> downloadAttachment(Attachment attachment) {
        Attachment.AttachmentStatus status = attachment.getStatus();
        if (status == null || status.getPermalink() == null) {
            return ServerResponse.notFound().build();
        }

        URI uri = URI.create(status.getPermalink());
        String filename = attachment.getSpec().getDisplayName();
        if (filename == null || filename.isBlank()) {
            filename = attachment.getMetadata().getName();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (status.getMediaType() != null) {
            try {
                mediaType = MediaType.parseMediaType(status.getMediaType());
            } catch (Exception ignored) {
            }
        }

        ContentDisposition contentDisposition = ContentDisposition.attachment()
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        return webClient.get()
            .uri(uri)
            .exchangeToMono(response -> {
                Flux<DataBuffer> body = response.bodyToFlux(DataBuffer.class);
                return ServerResponse.status(response.statusCode())
                    .contentType(mediaType)
                    .header(CONTENT_DISPOSITION, contentDisposition.toString())
                    .header(CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header("X-Content-Type-Options", "nosniff")
                    .body(body, DataBuffer.class);
            });
    }
}
