package ru.ok.journal.artificial;

import org.springframework.stereotype.Service;
import ru.ok.journal.dto.CommentDto;
import ru.ok.journal.dto.NewPostDto;
import ru.ok.journal.model.Post;
import ru.ok.journal.model.User;
import ru.ok.journal.service.ICommentService;
import ru.ok.journal.service.IPostControllerService;
import ru.ok.journal.service.IPostService;
import ru.ok.journal.service.IUserServiceBack;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ArtificialLoader implements IArtificialLoader {

    private volatile boolean flag;
    private volatile List<Post> posts; //TODO: здесь оставить переменную или только в методах хранить отдельно (toAllPost, toPost);
    private volatile List<String> words;

    private Random random;
    private Integer maxSentenceLength;
    private final User user;

    private IPostControllerService postControllerService;
    private IPostService postService;
    private ICommentService commentService;


    public ArtificialLoader(IPostService postService, IPostControllerService postControllerService, ICommentService commentService, IUserServiceBack userServiceBack) throws IOException {
        this.postControllerService = postControllerService;
        this.postService = postService;
        this.commentService = commentService;
        List<User> users = new ArrayList<>();

        this.flag = false;
        posts = new ArrayList<>();
        words = new ArrayList<>();

        random = new Random();
        maxSentenceLength = 5;
        users = userServiceBack.findAll();
        user = users.get(0);//TODO: get <Artificial>

        //Заполняем список слов из файла
        this.refreshWordList();
    }

    /**
     * maxSentenceLength = 5 +1;
     * @return String (sentence)
     */
    private String getSentence(){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < random.nextInt(maxSentenceLength)+1; i++) {
            str.append(words.get(random.nextInt(words.size()))).append(" ");
        }
        return str.toString();
    }

    private void toAllPost(){
        this.posts = postService.getAllPosts();
        Integer numberOfPosts = posts.size();
        Post post = posts.get(random.nextInt(numberOfPosts));

        CommentDto comment = new CommentDto();
        comment.setData(this.getSentence());
        commentService.add(user, post, comment);
    }

    /**
     * When (flag == stop) all threads will be finished
     */
    @Override
    public void stopLoader(){
        flag = false;
        System.out.println("stop");
    }

    @Override
    public void startLoader(){
        flag = true;
        System.out.println("start");
        while(flag){
            toAllPost();
        }
    }

    @Override
    public void toPost(Integer postId){
        this.flag = true;
        while(this.flag){
            posts = postService.getAllPosts();
            Post post = posts.get(postId);
            if (post == null) return;
            CommentDto comment = new CommentDto();
            comment.setData(this.getSentence());
            commentService.add(user, post, comment);
        }
    }

    @Override
    public void createPost(){
        NewPostDto postDto = new NewPostDto();
        postDto.setName("<Artificial> " + words.get(random.nextInt(words.size())));
        postDto.setData(this.getSentence());
        postControllerService.createPost(postDto);
    }

    @Override
    public void setMaxSentenceLength(Integer maxSentenceLength){
        if (maxSentenceLength > 0) this.maxSentenceLength = maxSentenceLength;
    }

    @Override
    public void refreshWordList() throws IOException{
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream("src/main/java/ru/ok/journal/artificial/data/word_rus")
                )
        );
        String str;
        while((str = reader.readLine()) != null) {
            this.words.add(str);
        }
        reader.close();
    }

    @Override
    public boolean getStatus(){
        return flag;
    }
}