package com.ecnu.smartmarketing.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import cn.hutool.core.lang.Console;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class BuildMappingIndex {

    public static void main(String[] args) throws IOException {
        // 1. 构建分词器（IKAnalyzer）
        IKAnalyzer ikAnalyzer = new IKAnalyzer();

        // 2. 构建文档写入器配置（IndexWriterConfig）
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ikAnalyzer);

        // 3. 构建文档写入器（IndexWriter）
        IndexWriter indexWriter = new IndexWriter(
                FSDirectory.open(Paths.get("D:\\Learning\\MasterCourses\\SmartMarketing\\index")), indexWriterConfig);

        // 4. 读取所有文件构建文档
        File articleDir = new File("D:\\Learning\\MasterCourses\\SmartMarketing\\data");
        File[] fileList = articleDir.listFiles();


        for (File file : Objects.requireNonNull(fileList)) {

            // 5. 文档中添加字段
            CsvReader reader = CsvUtil.getReader();
            //从文件中读取CSV数据
            CsvData data = reader.read(file);
            List<CsvRow> rows = data.getRows();
            //遍历行
            for (CsvRow csvRow : rows) {
                //getRawList返回一个List列表，列表的每一项为CSV中的一个单元格（既逗号分隔部分）
                Console.log(csvRow.getRawList());

                // 6. 写入文档
                Document document = new Document();
                document.add(new TextField("url", csvRow.get(0), Field.Store.YES));
                document.add(new TextField("keyWord", csvRow.toString(), Field.Store.NO));
                document.add(new StoredField("path", file.getAbsolutePath() + "/" + file.getName()));
                indexWriter.addDocument(document);
            }


        }

        // 7. 关闭写入器
        indexWriter.close();
    }
}