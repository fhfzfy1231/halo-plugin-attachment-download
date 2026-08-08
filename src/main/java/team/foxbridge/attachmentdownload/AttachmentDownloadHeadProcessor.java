package team.foxbridge.attachmentdownload;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * Loads the theme-side click guard that shows a friendly message for users
 * without the attachment download permission.
 */
@Component
public class AttachmentDownloadHeadProcessor implements TemplateHeadProcessor {

    private static final String SCRIPT_URL =
        "/plugins/attachment-download/assets/res/download-guard.js";

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        var factory = context.getModelFactory();
        model.add(factory.createText("<script src=\"" + SCRIPT_URL + "\"></script>"));
        return Mono.empty();
    }
}
