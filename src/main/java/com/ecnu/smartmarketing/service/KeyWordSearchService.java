package com.ecnu.smartmarketing.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import cn.hutool.log.Log;
import jdk.nashorn.internal.runtime.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;

@Service
public class KeyWordSearchService {

    public String searchByKeyWord(String keyword) throws IOException {

        // 1. 构建索引读取器
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("D:\\Learning\\MasterCourses\\SmartMarketing\\index")));

        // 2. 构建索引查询器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        // 3. 执行查询，获取文档
        TermQuery termQuery = new TermQuery(new Term("keyWord", keyword));

        // 4. 获取搜索比重前2
        TopDocs topDocs = indexSearcher.search(termQuery, 10);
        Optional<ScoreDoc> scoreDoc = Arrays.stream(topDocs.scoreDocs).findFirst();
        int docId = scoreDoc.orElseThrow(NotFoundException::new).doc;
        Document document = indexSearcher.doc(docId);

        System.out.println("keyWord:" + keyword + " URL：" + document.get("url"));

        indexReader.close();

        return document.get("url");


    }
}

