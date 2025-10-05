package com.gjh.xiaozi.config;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class VectorStoreConfig {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @PostConstruct
    public void init() {
        try {
            // 读取knowledge目录下所有txt文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:knowledge/*.*");
            for (Resource resource : resources) {
                TextReader reader = new TextReader(resource);
                reader.setCharset(Charset.defaultCharset());
                List<Document> documents = new TokenTextSplitter().transform(reader.read());
                String sourceMetadata =(String) reader.getCustomMetadata().get("source");
                String textHash = SecureUtil.md5(sourceMetadata);
                String redisKey = "vector-store:" + textHash;
                if(Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(redisKey, "0"))) {
                    vectorStore.add(documents);
                }
                else {
                    System.out.println("数据已存在，跳过");
                }
            }
        } catch (IOException e) {
            System.err.println("初始化数据失败");
        }
        System.out.println("初始化数据成功");
    }

    @Bean
    public DocumentRetriever documentRetriever(VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).build();
    }
}
