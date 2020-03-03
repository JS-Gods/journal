package ru.ok.journal.remote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.remoting.support.RemoteExporter;
import ru.ok.journal.remote.rmi.IRMIComment;
import ru.ok.journal.remote.rmi.RMIComment;
import ru.ok.journal.service.ICommentService;
import ru.ok.journal.service.IPostService;

@Configuration
public class RemoteConfig {
    private ICommentService commentService;
    private IPostService postService;

    public RemoteConfig(IPostService postService, ICommentService commentService){
        this.postService = postService;
        this.commentService = commentService;
    }

    @Bean
    RemoteExporter registerRMIExporter() {
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("rmi_comment");
        exporter.setServiceInterface(IRMIComment.class);
        exporter.setService(new RMIComment(this.postService, this.commentService));

        return exporter;
    }

}
