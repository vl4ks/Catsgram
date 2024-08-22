package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// Указываем, что класс PostService - является бином и его
// нужно добавить в контекст приложения
@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private UserService userService;
    private static Long currentMaxId = 1L;

    @Autowired
    public PostService(UserService userService) {
        this.userService = userService;
    }

    public List<Post> findAll(int from, int size, SortOrder sortOrder) {
        Comparator<Post> comparator = sortOrder == SortOrder.ASCENDING ?
                Comparator.comparing(Post::getPostDate) :
                Comparator.comparing(Post::getPostDate).reversed();
        return posts.values().stream()
                .sorted(comparator)
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    public Post findPostById(long postId) {
        return posts.entrySet().stream()
                .filter(p -> p.getValue().getId().equals(postId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Пост № %d не найден", postId)));
    }

    public Post create(Post post) {
        //Integer authorId = post.getAuthorId().intValue();
        User postAuthor = userService.findUserById(post.getAuthorId());
        if (postAuthor == null) {
            throw new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден");
        }
        if (post.getDescription() == null || post.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    private Long getNextId() {
        return currentMaxId++;
    }
}